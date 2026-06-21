package com.parasgarg.tracker.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val selectedFilter: WorkoutType? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val selectedFilter = MutableStateFlow<WorkoutType?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        workoutRepository.observeSessions(),
        selectedFilter,
    ) { sessions, filter ->
        HistoryUiState(
            sessions = if (filter == null) sessions else sessions.filter { it.type == filter },
            selectedFilter = filter,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun setFilter(type: WorkoutType?) {
        selectedFilter.value = type
    }
}
