package com.parasgarg.tracker.feature.clinical

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.core.util.renderPdfFirstPage
import com.parasgarg.tracker.data.model.domain.BcaReport
import com.parasgarg.tracker.data.model.domain.BloodTestReport
import java.io.File
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

private enum class ScanTarget { BCA, BLOOD_TEST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalScreen(
    onBack: () -> Unit,
    viewModel: ClinicalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeScanTarget by remember { mutableStateOf<ScanTarget?>(null) }
    var showScanPicker by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun onBitmapReady(bitmap: Bitmap) {
        when (activeScanTarget) {
            ScanTarget.BCA -> viewModel.scanAndOpenBcaDialog(bitmap)
            ScanTarget.BLOOD_TEST -> viewModel.scanAndOpenBloodTestDialog(bitmap)
            null -> {}
        }
        activeScanTarget = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri?.let { uri ->
                scope.launch {
                    val bmp = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    }
                    bmp?.let { onBitmapReady(it) }
                }
            }
        } else {
            activeScanTarget = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(it)?.use { s -> BitmapFactory.decodeStream(s) }
                }
                bmp?.let { b -> onBitmapReady(b) }
            }
        } ?: run { activeScanTarget = null }
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) { renderPdfFirstPage(context, it) }
                bmp?.let { b -> onBitmapReady(b) }
            }
        } ?: run { activeScanTarget = null }
    }

    LaunchedEffect(uiState.scanError) {
        uiState.scanError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearScanError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Clinical & BCA") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            item {
                SectionHeader(
                    title = "Blood Test Reports",
                    isScanning = uiState.isScanningBloodTest,
                    onAdd = viewModel::openBloodTestDialog,
                    onScan = { activeScanTarget = ScanTarget.BLOOD_TEST; showScanPicker = true },
                )
            }

            if (uiState.bloodTests.isEmpty()) {
                item {
                    Text(
                        "No blood tests logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(uiState.bloodTests, key = { it.id }) { report ->
                    BloodTestRow(report, onDelete = { viewModel.deleteBloodTest(report.id) })
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                SectionHeader(
                    title = "Body Composition (BCA)",
                    isScanning = uiState.isScanningBca,
                    onAdd = viewModel::openBcaDialog,
                    onScan = { activeScanTarget = ScanTarget.BCA; showScanPicker = true },
                )
            }

            if (uiState.bcaReports.isEmpty()) {
                item {
                    Text(
                        "No BCA scans logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(uiState.bcaReports, key = { it.id }) { report ->
                    BcaReportRow(report, onDelete = { viewModel.deleteBcaReport(report.id) })
                }
            }
        }
    }

    if (showScanPicker) {
        ScanSourcePickerDialog(
            onCamera = {
                showScanPicker = false
                val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "com.parasgarg.tracker.fileprovider", file)
                cameraUri = uri
                cameraLauncher.launch(uri)
            },
            onGallery = {
                showScanPicker = false
                galleryLauncher.launch("image/*")
            },
            onPdf = {
                showScanPicker = false
                pdfLauncher.launch(arrayOf("application/pdf"))
            },
            onDismiss = {
                showScanPicker = false
                activeScanTarget = null
            },
        )
    }

    if (uiState.showBloodTestDialog) {
        BloodTestDialog(
            form = uiState.bloodTestForm,
            onUpdate = viewModel::updateBloodTestForm,
            onSave = viewModel::saveBloodTest,
            onDismiss = viewModel::closeBloodTestDialog,
        )
    }

    if (uiState.showBcaDialog) {
        BcaDialog(
            form = uiState.bcaForm,
            onUpdate = viewModel::updateBcaForm,
            onSave = viewModel::saveBcaReport,
            onDismiss = viewModel::closeBcaDialog,
        )
    }
}

@Composable
private fun ScanSourcePickerDialog(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onPdf: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan report") },
        text = { Text("Choose how to import the report image.") },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = onCamera, modifier = Modifier.fillMaxWidth()) { Text("Take photo") }
                Button(onClick = onGallery, modifier = Modifier.fillMaxWidth()) { Text("Choose from gallery") }
                Button(onClick = onPdf, modifier = Modifier.fillMaxWidth()) { Text("Import PDF") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SectionHeader(
    title: String,
    isScanning: Boolean,
    onAdd: () -> Unit,
    onScan: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (isScanning) {
            Box(modifier = Modifier.padding(12.dp)) {
                CircularProgressIndicator(modifier = Modifier.padding(2.dp))
            }
        } else {
            IconButton(onClick = onScan) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = "Scan report")
            }
        }
        IconButton(onClick = onAdd) {
            Icon(Icons.Filled.Add, contentDescription = "Add manually")
        }
    }
}

@Composable
private fun BloodTestRow(report: BloodTestReport, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.testedAt.format(dateFormatter), style = MaterialTheme.typography.labelMedium)
                Row {
                    report.labName?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            val parts = listOfNotNull(
                report.hemoglobinGdl?.let { "Hb: ${"%.1f".format(it)} g/dL" },
                report.vitaminD3NgMl?.let { "Vit D3: ${"%.1f".format(it)} ng/mL" },
                report.vitaminB12PgMl?.let { "Vit B12: ${it.toInt()} pg/mL" },
                report.ldlMgDl?.let { "LDL: ${it.toInt()} mg/dL" },
                report.hdlMgDl?.let { "HDL: ${it.toInt()} mg/dL" },
                report.fastingBloodSugarMgDl?.let { "FBS: ${it.toInt()} mg/dL" },
                report.hba1cPercent?.let { "HbA1c: ${"%.1f".format(it)}%" },
            )
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BcaReportRow(report: BcaReport, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(report.scannedAt.format(dateFormatter), style = MaterialTheme.typography.labelMedium)
                Row {
                    report.deviceName?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            val parts = listOfNotNull(
                report.bodyFatPercent?.let { "Fat: ${"%.1f".format(it)}%" },
                report.skeletalMuscleMassKg?.let { "Muscle: ${"%.1f".format(it)} kg" },
                report.visceralFatLevel?.let { "Visceral: $it" },
                report.waterPercent?.let { "Water: ${"%.1f".format(it)}%" },
                report.bmi?.let { "BMI: ${"%.1f".format(it)}" },
            )
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BloodTestDialog(
    form: BloodTestForm,
    onUpdate: (BloodTestForm.() -> BloodTestForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Blood Test") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FormField("Lab name (optional)", form.labName) { v -> onUpdate { copy(labName = v) } }
                FormField("Hemoglobin (g/dL)", form.hemoglobin, KeyboardType.Decimal) { v -> onUpdate { copy(hemoglobin = v) } }
                FormField("Vitamin D3 (ng/mL)", form.vitaminD3, KeyboardType.Decimal) { v -> onUpdate { copy(vitaminD3 = v) } }
                FormField("Vitamin B12 (pg/mL)", form.vitaminB12, KeyboardType.Decimal) { v -> onUpdate { copy(vitaminB12 = v) } }
                FormField("LDL (mg/dL)", form.ldl, KeyboardType.Decimal) { v -> onUpdate { copy(ldl = v) } }
                FormField("HDL (mg/dL)", form.hdl, KeyboardType.Decimal) { v -> onUpdate { copy(hdl = v) } }
                FormField("Fasting blood sugar (mg/dL)", form.fastingBloodSugar, KeyboardType.Decimal) { v -> onUpdate { copy(fastingBloodSugar = v) } }
                FormField("HbA1c (%)", form.hba1c, KeyboardType.Decimal) { v -> onUpdate { copy(hba1c = v) } }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun BcaDialog(
    form: BcaForm,
    onUpdate: (BcaForm.() -> BcaForm) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log BCA Scan") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FormField("Body fat (%)", form.bodyFatPercent, KeyboardType.Decimal) { v -> onUpdate { copy(bodyFatPercent = v) } }
                FormField("Skeletal muscle mass (kg)", form.skeletalMuscleMassKg, KeyboardType.Decimal) { v -> onUpdate { copy(skeletalMuscleMassKg = v) } }
                FormField("Visceral fat level (1–20)", form.visceralFatLevel, KeyboardType.Number) { v -> onUpdate { copy(visceralFatLevel = v) } }
                FormField("Water (%)", form.waterPercent, KeyboardType.Decimal) { v -> onUpdate { copy(waterPercent = v) } }
                FormField("Bone mass (kg)", form.boneMassKg, KeyboardType.Decimal) { v -> onUpdate { copy(boneMassKg = v) } }
                FormField("BMI", form.bmi, KeyboardType.Decimal) { v -> onUpdate { copy(bmi = v) } }
                FormField("Device / Brand (optional)", form.deviceName) { v -> onUpdate { copy(deviceName = v) } }
            }
        },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}
