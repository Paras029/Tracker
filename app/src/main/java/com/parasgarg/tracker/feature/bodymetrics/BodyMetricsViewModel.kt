package com.parasgarg.tracker.feature.bodymetrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.domain.BodyMetric
import com.parasgarg.tracker.data.repository.BodyMetricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BodyMetricFormState(
    val weightKg: String = "",
    val bodyFatPercent: String = "",
    val restingHeartRate: String = "",
    val notes: String = "",
)

data class BodyMetricsUiState(
    val metrics: List<BodyMetric> = emptyList(),
    val form: BodyMetricFormState = BodyMetricFormState(),
)

@HiltViewModel
class BodyMetricsViewModel @Inject constructor(
    private val bodyMetricRepository: BodyMetricRepository,
) : ViewModel() {

    private val form = MutableStateFlow(BodyMetricFormState())

    val uiState: StateFlow<BodyMetricsUiState> = combine(
        bodyMetricRepository.observeAll(),
        form,
    ) { metrics, formState ->
        BodyMetricsUiState(metrics = metrics, form = formState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BodyMetricsUiState())

    fun updateWeight(value: String) = form.update { it.copy(weightKg = value) }
    fun updateBodyFat(value: String) = form.update { it.copy(bodyFatPercent = value) }
    fun updateRestingHeartRate(value: String) = form.update { it.copy(restingHeartRate = value) }
    fun updateNotes(value: String) = form.update { it.copy(notes = value) }

    fun save() {
        val state = form.value
        val weightKg = state.weightKg.toDoubleOrNull()
        val bodyFatPercent = state.bodyFatPercent.toDoubleOrNull()
        val restingHeartRate = state.restingHeartRate.toIntOrNull()
        if (weightKg == null && bodyFatPercent == null && restingHeartRate == null) return

        viewModelScope.launch {
            bodyMetricRepository.save(
                BodyMetric(
                    id = UUID.randomUUID().toString(),
                    recordedAt = Instant.now(),
                    weightKg = weightKg,
                    bodyFatPercent = bodyFatPercent,
                    restingHeartRate = restingHeartRate,
                    notes = state.notes.ifBlank { null },
                ),
            )
            form.value = BodyMetricFormState()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { bodyMetricRepository.delete(id) }
    }
}
