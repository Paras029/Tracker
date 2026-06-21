package com.parasgarg.tracker.feature.logworkout.racquet

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

data class LogRacquetUiState(
    val type: WorkoutType = WorkoutType.BADMINTON,
    val durationMinutes: String = "",
    val opponentName: String = "",
    val setsWon: String = "",
    val setsLost: String = "",
    val scoreSummary: String = "",
    val notes: String = "",
    val perceivedEffort: Int? = null,
    val isSaved: Boolean = false,
)

@HiltViewModel
class LogRacquetViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val type: WorkoutType = WorkoutType.valueOf(
        savedStateHandle.get<String>(Destination.LogRacquet.ARG_TYPE) ?: WorkoutType.BADMINTON.name,
    )

    private val _uiState = MutableStateFlow(LogRacquetUiState(type = type))
    val uiState: StateFlow<LogRacquetUiState> = _uiState.asStateFlow()

    fun updateDuration(value: String) = _uiState.update { it.copy(durationMinutes = value) }
    fun updateOpponentName(value: String) = _uiState.update { it.copy(opponentName = value) }
    fun updateSetsWon(value: String) = _uiState.update { it.copy(setsWon = value) }
    fun updateSetsLost(value: String) = _uiState.update { it.copy(setsLost = value) }
    fun updateScoreSummary(value: String) = _uiState.update { it.copy(scoreSummary = value) }
    fun updateNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun updatePerceivedEffort(value: Int?) = _uiState.update { it.copy(perceivedEffort = value) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            workoutRepository.saveSession(
                WorkoutSession(
                    id = UUID.randomUUID().toString(),
                    type = state.type,
                    startTime = Instant.now(),
                    durationMinutes = state.durationMinutes.toIntOrNull() ?: 0,
                    notes = state.notes.ifBlank { null },
                    perceivedEffort = state.perceivedEffort,
                    detail = WorkoutDetail.Racquet(
                        opponentName = state.opponentName.ifBlank { null },
                        setsWon = state.setsWon.toIntOrNull() ?: 0,
                        setsLost = state.setsLost.toIntOrNull() ?: 0,
                        scoreSummary = state.scoreSummary.ifBlank { null },
                    ),
                ),
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
