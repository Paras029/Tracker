package com.parasgarg.tracker.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.core.navigation.Destination
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: WorkoutSession? = null,
    val isDeleted: Boolean = false,
)

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: String =
        checkNotNull(savedStateHandle.get<String>(Destination.SessionDetail.ARG_SESSION_ID))

    private val isDeletedFlow = MutableStateFlow(false)

    val uiState: StateFlow<SessionDetailUiState> = combine(
        workoutRepository.observeSession(sessionId),
        isDeletedFlow,
    ) { session, isDeleted ->
        SessionDetailUiState(session = session, isDeleted = isDeleted)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionDetailUiState())

    fun delete() {
        viewModelScope.launch {
            workoutRepository.deleteSession(sessionId)
            isDeletedFlow.update { true }
        }
    }
}
