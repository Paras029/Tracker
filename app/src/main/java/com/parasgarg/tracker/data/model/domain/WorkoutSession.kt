package com.parasgarg.tracker.data.model.domain

import com.parasgarg.tracker.data.model.WorkoutType
import java.time.Instant

data class WorkoutSession(
    val id: String,
    val type: WorkoutType,
    val startTime: Instant,
    val durationMinutes: Int,
    val notes: String?,
    val perceivedEffort: Int?,
    val detail: WorkoutDetail,
)

sealed interface WorkoutDetail {
    data class Strength(val sets: List<StrengthSetInput>) : WorkoutDetail
    data class Cardio(
        val distanceMeters: Double,
        val avgPaceSecondsPerKm: Int?,
        val laps: Int?,
        val poolLengthMeters: Int?,
    ) : WorkoutDetail
    data class Racquet(
        val opponentName: String?,
        val setsWon: Int,
        val setsLost: Int,
        val scoreSummary: String?,
    ) : WorkoutDetail
    data object None : WorkoutDetail
}

data class StrengthSetInput(
    val exerciseName: String,
    val setOrder: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int?,
)
