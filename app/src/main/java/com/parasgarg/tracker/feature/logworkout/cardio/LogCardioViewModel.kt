package com.parasgarg.tracker.feature.logworkout.cardio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.core.navigation.Destination
import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.model.domain.WorkoutDetail
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogCardioUiState(
    val type: WorkoutType = WorkoutType.RUNNING,
    val durationMinutes: String = "",
    val distanceMeters: String = "",
    val laps: String = "",
    val poolLengthMeters: String = "",
    val notes: String = "",
    val perceivedEffort: Int? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class LogCardioViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val type: WorkoutType = WorkoutType.valueOf(
        savedStateHandle.get<String>(Destination.LogCardio.ARG_TYPE) ?: WorkoutType.RUNNING.name,
    )

    private val _uiState = MutableStateFlow(LogCardioUiState(type = type))
    val uiState: StateFlow<LogCardioUiState> = _uiState.asStateFlow()

    fun updateDuration(value: String) = _uiState.update { it.copy(durationMinutes = value) }
    fun updateDistance(value: String) = _uiState.update { it.copy(distanceMeters = value) }
    fun updateLaps(value: String) = _uiState.update { it.copy(laps = value) }
    fun updatePoolLength(value: String) = _uiState.update { it.copy(poolLengthMeters = value) }
    fun updateNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun updatePerceivedEffort(value: Int?) = _uiState.update { it.copy(perceivedEffort = value) }

    fun save() {
        val state = _uiState.value
        val distanceMeters = state.distanceMeters.toDoubleOrNull() ?: 0.0
        val durationMinutes = state.durationMinutes.toIntOrNull() ?: 0
        val avgPaceSecondsPerKm = if (distanceMeters > 0 && durationMinutes > 0) {
            ((durationMinutes * 60) / (distanceMeters / 1000)).toInt()
        } else {
            null
        }

        viewModelScope.launch {
            workoutRepository.saveSession(
                WorkoutSession(
                    id = UUID.randomUUID().toString(),
                    type = state.type,
                    startTime = Instant.now(),
                    durationMinutes = durationMinutes,
                    notes = state.notes.ifBlank { null },
                    perceivedEffort = state.perceivedEffort,
                    detail = WorkoutDetail.Cardio(
                        distanceMeters = distanceMeters,
                        avgPaceSecondsPerKm = avgPaceSecondsPerKm,
                        laps = state.laps.toIntOrNull(),
                        poolLengthMeters = state.poolLengthMeters.toIntOrNull(),
                    ),
                ),
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
