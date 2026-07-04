package com.parasgarg.tracker.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.core.ai.GeminiService
import com.parasgarg.tracker.core.navigation.Destination
import com.parasgarg.tracker.core.util.workoutTypeLabel
import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.model.domain.WorkoutDetail
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.repository.UserProfileRepository
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: WorkoutSession? = null,
    val isDeleted: Boolean = false,
    val coachingTip: String? = null,
    val isTipLoading: Boolean = false,
    val useMetric: Boolean = true,
)

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: UserProfileRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val geminiService: GeminiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val sessionId: String =
        checkNotNull(savedStateHandle.get<String>(Destination.SessionDetail.ARG_SESSION_ID))

    private val isDeletedFlow = MutableStateFlow(false)
    private val _tipState = MutableStateFlow(Pair<String?, Boolean>(null, false))

    val uiState: StateFlow<SessionDetailUiState> = combine(
        workoutRepository.observeSession(sessionId),
        isDeletedFlow,
        _tipState,
        preferencesRepository.observeUseMetric(),
    ) { session, isDeleted, (tip, loading), useMetric ->
        SessionDetailUiState(
            session = session,
            isDeleted = isDeleted,
            coachingTip = tip,
            isTipLoading = loading,
            useMetric = useMetric,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionDetailUiState())

    fun delete() {
        viewModelScope.launch {
            workoutRepository.deleteSession(sessionId)
            isDeletedFlow.update { true }
        }
    }

    fun requestWorkoutTip() {
        if (_tipState.value.second) return
        val session = uiState.value.session ?: return
        viewModelScope.launch {
            _tipState.value = Pair(null, true)
            val profile = profileRepository.observe().first()
            val apiKey = profile?.geminiApiKey.orEmpty()
            val distanceKm = (session.detail as? WorkoutDetail.Cardio)
                ?.distanceMeters?.div(1000.0)
            val tip = geminiService.getWorkoutTip(
                apiKey = apiKey,
                workoutType = workoutTypeLabel(session.type),
                durationMinutes = session.durationMinutes,
                distanceKm = distanceKm,
                perceivedEffort = session.perceivedEffort,
                fitnessGoal = profile?.fitnessGoal,
                startTime = session.startTime,
            )
            _tipState.value = Pair(tip, false)
        }
    }
}
