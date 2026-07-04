package com.parasgarg.tracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.domain.BodyMetric
import com.parasgarg.tracker.data.model.domain.WellnessMetric
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import com.parasgarg.tracker.data.preferences.UserPreferencesRepository
import com.parasgarg.tracker.data.repository.BodyMetricRepository
import com.parasgarg.tracker.data.repository.WellnessRepository
import com.parasgarg.tracker.data.repository.WorkoutRepository
import com.parasgarg.tracker.data.source.WearableRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val recentSessions: List<WorkoutSession> = emptyList(),
    val weeklySessionCount: Int = 0,
    val weeklyDurationMinutes: Int = 0,
    val latestBodyMetric: BodyMetric? = null,
    val todayWellness: WellnessMetric? = null,
    val healthScore: Int? = null,
    val useMetric: Boolean = true,
)

private const val RECENT_SESSION_LIMIT = 5

@HiltViewModel
class DashboardViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    bodyMetricRepository: BodyMetricRepository,
    wellnessRepository: WellnessRepository,
    private val wearableRegistry: WearableRegistry,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    init {
        viewModelScope.launch {
            wearableRegistry.syncToday(wellnessRepository)
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        workoutRepository.observeSessions(),
        bodyMetricRepository.observeLatest(),
        wellnessRepository.observeForDate(LocalDate.now()),
        preferencesRepository.observeUseMetric(),
    ) { sessions, latestBodyMetric, todayWellness, useMetric ->
        val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
        val thisWeek = sessions.filter { it.startTime.isAfter(weekAgo) }
        DashboardUiState(
            recentSessions = sessions.take(RECENT_SESSION_LIMIT),
            weeklySessionCount = thisWeek.size,
            weeklyDurationMinutes = thisWeek.sumOf { it.durationMinutes },
            latestBodyMetric = latestBodyMetric,
            todayWellness = todayWellness,
            healthScore = computeHealthScore(thisWeek.size, todayWellness),
            useMetric = useMetric,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun computeHealthScore(weeklyWorkouts: Int, wellness: WellnessMetric?): Int {
        var score = 0
        if (weeklyWorkouts >= 3) score += 30
        else if (weeklyWorkouts >= 1) score += 15
        if (wellness != null) {
            if ((wellness.stepsCount ?: 0) >= 8000) score += 20
            if ((wellness.sleepHours ?: 0.0) >= 7.0) score += 20
            if ((wellness.recoveryScore ?: 0) >= 70) score += 15
            if ((wellness.restingHeartRate ?: 100) < 70) score += 15
        }
        return score
    }
}
