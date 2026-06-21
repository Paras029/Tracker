package com.parasgarg.tracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.domain.BodyMetric
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.repository.BodyMetricRepository
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val recentSessions: List<WorkoutSession> = emptyList(),
    val weeklySessionCount: Int = 0,
    val weeklyDurationMinutes: Int = 0,
    val latestBodyMetric: BodyMetric? = null,
)

private const val RECENT_SESSION_LIMIT = 5

@HiltViewModel
class DashboardViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    bodyMetricRepository: BodyMetricRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        workoutRepository.observeSessions(),
        bodyMetricRepository.observeLatest(),
    ) { sessions, latestBodyMetric ->
        val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        val thisWeek = sessions.filter { it.startTime.isAfter(weekAgo) }
        DashboardUiState(
            recentSessions = sessions.take(RECENT_SESSION_LIMIT),
            weeklySessionCount = thisWeek.size,
            weeklyDurationMinutes = thisWeek.sumOf { it.durationMinutes },
            latestBodyMetric = latestBodyMetric,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
