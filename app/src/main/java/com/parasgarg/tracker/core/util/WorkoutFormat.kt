package com.parasgarg.tracker.core.util

import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.model.domain.WorkoutDetail
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withZone(ZoneId.systemDefault())

fun workoutTypeLabel(type: WorkoutType): String = when (type) {
    WorkoutType.STRENGTH -> "Strength Training"
    WorkoutType.RUNNING -> "Running"
    WorkoutType.SWIMMING -> "Swimming"
    WorkoutType.BADMINTON -> "Badminton"
    WorkoutType.TABLE_TENNIS -> "Table Tennis"
    WorkoutType.OTHER -> "Other"
}

fun formatSessionTime(startTime: Instant): String = dateTimeFormatter.format(startTime)

fun workoutSummary(session: WorkoutSession): String = when (val detail = session.detail) {
    is WorkoutDetail.Strength -> {
        val exerciseCount = detail.sets.map { it.exerciseName }.distinct().size
        "$exerciseCount exercise${if (exerciseCount == 1) "" else "s"} · ${detail.sets.size} sets"
    }
    is WorkoutDetail.Cardio -> {
        val km = detail.distanceMeters / 1000.0
        "%.2f km".format(km)
    }
    is WorkoutDetail.Racquet -> "${detail.setsWon}-${detail.setsLost} sets" +
        (detail.opponentName?.let { " vs $it" } ?: "")
    WorkoutDetail.None -> "${session.durationMinutes} min"
}

fun formatPace(secondsPerKm: Int?): String? {
    if (secondsPerKm == null) return null
    val minutes = secondsPerKm / 60
    val seconds = secondsPerKm % 60
    return "%d:%02d /km".format(minutes, seconds)
}

fun formatDistance(distanceMeters: Double, useMetric: Boolean): String =
    if (useMetric) "%.2f km".format(distanceMeters / 1000.0)
    else "%.2f mi".format(distanceMeters / 1609.344)

fun formatWeight(weightKg: Double, useMetric: Boolean): String =
    if (useMetric) "%.1f kg".format(weightKg)
    else "%.1f lbs".format(weightKg * 2.20462)
