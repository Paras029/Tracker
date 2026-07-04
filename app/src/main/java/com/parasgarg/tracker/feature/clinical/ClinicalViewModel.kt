package com.parasgarg.tracker.feature.clinical

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.core.ai.ImageScanService
import com.parasgarg.tracker.data.model.domain.BcaReport
import com.parasgarg.tracker.data.model.domain.BloodTestReport
import com.parasgarg.tracker.data.repository.ClinicalRepository
import com.parasgarg.tracker.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BloodTestForm(
    val labName: String = "",
    val hemoglobin: String = "",
    val vitaminD3: String = "",
    val vitaminB12: String = "",
    val ldl: String = "",
    val hdl: String = "",
    val fastingBloodSugar: String = "",
    val hba1c: String = "",
    val notes: String = "",
)

data class BcaForm(
    val bodyFatPercent: String = "",
    val skeletalMuscleMassKg: String = "",
    val visceralFatLevel: String = "",
    val waterPercent: String = "",
    val boneMassKg: String = "",
    val bmi: String = "",
    val deviceName: String = "",
)

data class ClinicalUiState(
    val bloodTests: List<BloodTestReport> = emptyList(),
    val bcaReports: List<BcaReport> = emptyList(),
    val showBloodTestDialog: Boolean = false,
    val bloodTestForm: BloodTestForm = BloodTestForm(),
    val showBcaDialog: Boolean = false,
    val bcaForm: BcaForm = BcaForm(),
    val isScanningBca: Boolean = false,
    val isScanningBloodTest: Boolean = false,
    val scanError: String? = null,
)

@HiltViewModel
class ClinicalViewModel @Inject constructor(
    private val repository: ClinicalRepository,
    private val profileRepository: UserProfileRepository,
    private val imageScanService: ImageScanService,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ClinicalUiState())

    val uiState: StateFlow<ClinicalUiState> = combine(
        repository.observeBloodTests(),
        repository.observeBcaReports(),
        _dialogState,
    ) { bloodTests, bcaReports, dialogs ->
        dialogs.copy(bloodTests = bloodTests, bcaReports = bcaReports)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClinicalUiState())

    fun openBloodTestDialog() = _dialogState.update { it.copy(showBloodTestDialog = true, bloodTestForm = BloodTestForm()) }
    fun closeBloodTestDialog() = _dialogState.update { it.copy(showBloodTestDialog = false) }

    fun updateBloodTestForm(update: BloodTestForm.() -> BloodTestForm) =
        _dialogState.update { it.copy(bloodTestForm = update(it.bloodTestForm)) }

    fun saveBloodTest() {
        val form = _dialogState.value.bloodTestForm
        viewModelScope.launch {
            repository.saveBloodTest(
                BloodTestReport(
                    id = UUID.randomUUID().toString(),
                    testedAt = LocalDate.now(),
                    labName = form.labName.trim().ifBlank { null },
                    hemoglobinGdl = form.hemoglobin.toDoubleOrNull(),
                    vitaminD3NgMl = form.vitaminD3.toDoubleOrNull(),
                    vitaminB12PgMl = form.vitaminB12.toDoubleOrNull(),
                    ldlMgDl = form.ldl.toDoubleOrNull(),
                    hdlMgDl = form.hdl.toDoubleOrNull(),
                    fastingBloodSugarMgDl = form.fastingBloodSugar.toDoubleOrNull(),
                    hba1cPercent = form.hba1c.toDoubleOrNull(),
                    notes = form.notes.trim().ifBlank { null },
                ),
            )
            closeBloodTestDialog()
        }
    }

    fun deleteBloodTest(id: String) = viewModelScope.launch { repository.deleteBloodTest(id) }

    fun openBcaDialog() = _dialogState.update { it.copy(showBcaDialog = true, bcaForm = BcaForm()) }
    fun closeBcaDialog() = _dialogState.update { it.copy(showBcaDialog = false) }

    fun updateBcaForm(update: BcaForm.() -> BcaForm) =
        _dialogState.update { it.copy(bcaForm = update(it.bcaForm)) }

    fun saveBcaReport() {
        val form = _dialogState.value.bcaForm
        viewModelScope.launch {
            repository.saveBcaReport(
                BcaReport(
                    id = UUID.randomUUID().toString(),
                    scannedAt = LocalDate.now(),
                    bodyFatPercent = form.bodyFatPercent.toDoubleOrNull(),
                    skeletalMuscleMassKg = form.skeletalMuscleMassKg.toDoubleOrNull(),
                    visceralFatLevel = form.visceralFatLevel.toIntOrNull(),
                    waterPercent = form.waterPercent.toDoubleOrNull(),
                    boneMassKg = form.boneMassKg.toDoubleOrNull(),
                    bmi = form.bmi.toDoubleOrNull(),
                    deviceName = form.deviceName.trim().ifBlank { null },
                ),
            )
            closeBcaDialog()
        }
    }

    fun deleteBcaReport(id: String) = viewModelScope.launch { repository.deleteBcaReport(id) }

    fun scanAndOpenBcaDialog(bitmap: Bitmap) {
        if (_dialogState.value.isScanningBca) return
        viewModelScope.launch {
            _dialogState.update { it.copy(isScanningBca = true, scanError = null) }
            val apiKey = profileRepository.observe().first()?.geminiApiKey.orEmpty()
            val result = imageScanService.scanBcaReport(bitmap, apiKey)
            if (result != null) {
                _dialogState.update {
                    it.copy(
                        isScanningBca = false,
                        showBcaDialog = true,
                        bcaForm = BcaForm(
                            bodyFatPercent = result.bodyFatPercent?.let { v -> "%.1f".format(v) } ?: "",
                            skeletalMuscleMassKg = result.skeletalMuscleMassKg?.let { v -> "%.1f".format(v) } ?: "",
                            visceralFatLevel = result.visceralFatLevel?.toString() ?: "",
                            waterPercent = result.waterPercent?.let { v -> "%.1f".format(v) } ?: "",
                            boneMassKg = result.boneMassKg?.let { v -> "%.1f".format(v) } ?: "",
                            bmi = result.bmi?.let { v -> "%.1f".format(v) } ?: "",
                        ),
                    )
                }
            } else {
                _dialogState.update {
                    it.copy(isScanningBca = false, scanError = "Could not extract BCA data from the image. Check the image quality or enter values manually.")
                }
            }
        }
    }

    fun scanAndOpenBloodTestDialog(bitmap: Bitmap) {
        if (_dialogState.value.isScanningBloodTest) return
        viewModelScope.launch {
            _dialogState.update { it.copy(isScanningBloodTest = true, scanError = null) }
            val apiKey = profileRepository.observe().first()?.geminiApiKey.orEmpty()
            val result = imageScanService.scanBloodTestReport(bitmap, apiKey)
            if (result != null) {
                _dialogState.update {
                    it.copy(
                        isScanningBloodTest = false,
                        showBloodTestDialog = true,
                        bloodTestForm = BloodTestForm(
                            hemoglobin = result.hemoglobinGdL?.let { v -> "%.1f".format(v) } ?: "",
                            vitaminD3 = result.vitaminD3NgmL?.let { v -> "%.1f".format(v) } ?: "",
                            vitaminB12 = result.vitaminB12PgmL?.let { v -> "${v.toInt()}" } ?: "",
                            ldl = result.ldlMgdL?.let { v -> "${v.toInt()}" } ?: "",
                            hdl = result.hdlMgdL?.let { v -> "${v.toInt()}" } ?: "",
                            fastingBloodSugar = result.fastingGlucoseMgdL?.let { v -> "${v.toInt()}" } ?: "",
                            hba1c = result.hba1cPercent?.let { v -> "%.1f".format(v) } ?: "",
                        ),
                    )
                }
            } else {
                _dialogState.update {
                    it.copy(isScanningBloodTest = false, scanError = "Could not extract blood test data from the image. Check the image quality or enter values manually.")
                }
            }
        }
    }

    fun clearScanError() = _dialogState.update { it.copy(scanError = null) }
}
