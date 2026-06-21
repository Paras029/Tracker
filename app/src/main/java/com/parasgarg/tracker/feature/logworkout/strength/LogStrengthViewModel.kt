package com.parasgarg.tracker.feature.logworkout.strength

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.WorkoutType
import dagger.hilt.android.lifecycle.HiltViewModel
import com.parasgarg.tracker.data.model.domain.StrengthSetInput
import com.parasgarg.tracker.data.model.domain.WorkoutDetail
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.repository.WorkoutRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StrengthSetRow(
    val exerciseName: String = "",
    val reps: String = "",
    val weightKg: String = "",
    val restSeconds: String = "",
)

data class LogStrengthUiState(
    val sets: List<StrengthSetRow> = listOf(StrengthSetRow()),
    val durationMinutes: String = "",
    val notes: String = "",
    val perceivedEffort: Int? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class LogStrengthViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogStrengthUiState())
    val uiState: StateFlow<LogStrengthUiState> = _uiState.asStateFlow()

    fun addSetRow() {
        _uiState.update { it.copy(sets = it.sets + StrengthSetRow()) }
    }

    fun removeSetRow(index: Int) {
        _uiState.update { state ->
            state.copy(sets = state.sets.filterIndexed { i, _ -> i != index })
        }
    }

    fun updateSetRow(index: Int, transform: (StrengthSetRow) -> StrengthSetRow) {
        _uiState.update { state ->
            state.copy(sets = state.sets.mapIndexed { i, row -> if (i == index) transform(row) else row })
        }
    }

    fun updateDuration(value: String) {
        _uiState.update { it.copy(durationMinutes = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun updatePerceivedEffort(value: Int?) {
        _uiState.update { it.copy(perceivedEffort = value) }
    }

    fun save() {
        val state = _uiState.value
        val sets = state.sets
            .filter { it.exerciseName.isNotBlank() }
            .mapIndexed { index, row ->
                StrengthSetInput(
                    exerciseName = row.exerciseName.trim(),
                    setOrder = index,
                    reps = row.reps.toIntOrNull() ?: 0,
                    weightKg = row.weightKg.toDoubleOrNull() ?: 0.0,
                    restSeconds = row.restSeconds.toIntOrNull(),
                )
            }
        if (sets.isEmpty()) return

        viewModelScope.launch {
            workoutRepository.saveSession(
                WorkoutSession(
                    id = UUID.randomUUID().toString(),
                    type = WorkoutType.STRENGTH,
                    startTime = Instant.now(),
                    durationMinutes = state.durationMinutes.toIntOrNull() ?: 0,
                    notes = state.notes.ifBlank { null },
                    perceivedEffort = state.perceivedEffort,
                    detail = WorkoutDetail.Strength(sets),
                ),
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
