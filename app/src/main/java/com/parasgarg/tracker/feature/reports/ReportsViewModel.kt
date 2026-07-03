package com.parasgarg.tracker.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.repository.BodyMetricRepository
import com.parasgarg.tracker.data.repository.ClinicalRepository
import com.parasgarg.tracker.data.repository.NutritionRepository
import com.parasgarg.tracker.data.repository.UserProfileRepository
import com.parasgarg.tracker.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ReportsUiState(
    val selectedTab: Int = 0,
    val last7DaysCounts: List<Float> = List(7) { 0f },
    val sessionsByType: List<Pair<String, Int>> = emptyList(),
    val totalWorkoutsThisMonth: Int = 0,
    val totalMinutesThisMonth: Int = 0,
    val weightHistory: List<Pair<LocalDate, Float>> = emptyList(),
    val bodyFatHistory: List<Pair<LocalDate, Float>> = emptyList(),
    val last7DaysCalories: List<Float> = List(7) { 0f },
    val caloriesTarget: Float = 0f,
    val avgCaloriesLast7: Float = 0f,
    val avgProteinLast7: Float = 0f,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    workoutRepository: WorkoutRepository,
    bodyMetricRepository: BodyMetricRepository,
    clinicalRepository: ClinicalRepository,
    nutritionRepository: NutritionRepository,
    profileRepository: UserProfileRepository,
) : ViewModel() {

    private val _tabState = MutableStateFlow(0)

    private val today = LocalDate.now()
    private val sevenDaysAgo = today.minusDays(6)
    private val thirtyDaysAgo = today.minusDays(29)

    val uiState: StateFlow<ReportsUiState> = combine(
        workoutRepository.observeSessions(),
        bodyMetricRepository.observeAll(),
        clinicalRepository.observeBcaReports(),
        nutritionRepository.observeEntriesForDateRange(sevenDaysAgo, today),
        combine(profileRepository.observe(), _tabState) { profile, tab ->
            Pair(profile?.targetCaloriesKcal?.toFloat() ?: 0f, tab)
        },
    ) { sessions, bodyMetrics, bcaReports, nutritionEntries, (caloriesTarget, tab) ->
        val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
        val monthAgo = Instant.now().minus(30, ChronoUnit.DAYS)

        val last7DaysCounts = last7Days.map { date ->
            sessions.count { s ->
                s.startTime.atZone(ZoneId.systemDefault()).toLocalDate() == date
            }.toFloat()
        }

        val sessionsByType = WorkoutType.entries.mapNotNull { type ->
            val count = sessions.count { it.type == type }
            if (count > 0) Pair(typeLabel(type), count) else null
        }.sortedByDescending { it.second }

        val thisMonthSessions = sessions.filter { it.startTime.isAfter(monthAgo) }

        val weightHistory = bodyMetrics
            .filter { it.weightKg != null }
            .takeLast(20)
            .map { Pair(it.recordedAt.atZone(ZoneId.systemDefault()).toLocalDate(), it.weightKg!!.toFloat()) }

        val bodyFatHistory = bcaReports
            .filter { it.bodyFatPercent != null }
            .takeLast(20)
            .map { Pair(it.scannedAt, it.bodyFatPercent!!.toFloat()) }

        val last7DaysCalories = last7Days.map { date ->
            nutritionEntries.filter { it.date == date }.sumOf { it.caloriesKcal }.toFloat()
        }
        val avgCalories = last7DaysCalories.average().toFloat()
        val avgProtein = last7Days.map { date ->
            nutritionEntries.filter { it.date == date }.sumOf { it.proteinG }.toFloat()
        }.average().toFloat()

        ReportsUiState(
            selectedTab = tab,
            last7DaysCounts = last7DaysCounts,
            sessionsByType = sessionsByType,
            totalWorkoutsThisMonth = thisMonthSessions.size,
            totalMinutesThisMonth = thisMonthSessions.sumOf { it.durationMinutes },
            weightHistory = weightHistory,
            bodyFatHistory = bodyFatHistory,
            last7DaysCalories = last7DaysCalories,
            caloriesTarget = caloriesTarget,
            avgCaloriesLast7 = avgCalories,
            avgProteinLast7 = avgProtein,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun selectTab(index: Int) = _tabState.update { index }

    private fun typeLabel(type: WorkoutType) = when (type) {
        WorkoutType.STRENGTH -> "Strength"
        WorkoutType.RUNNING -> "Running"
        WorkoutType.SWIMMING -> "Swimming"
        WorkoutType.BADMINTON -> "Badminton"
        WorkoutType.TABLE_TENNIS -> "Table Tennis"
        WorkoutType.OTHER -> "Other"
    }
}
