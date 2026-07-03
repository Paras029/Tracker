package com.parasgarg.tracker.feature.clinical

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parasgarg.tracker.data.model.domain.BcaReport
import com.parasgarg.tracker.data.model.domain.BloodTestReport
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalScreen(
    onBack: () -> Unit,
    viewModel: ClinicalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
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
                SectionHeader("Blood Test Reports", onAdd = viewModel::openBloodTestDialog)
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
                SectionHeader("Body Composition (BCA)", onAdd = viewModel::openBcaDialog)
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
private fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onAdd) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
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
