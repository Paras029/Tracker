package com.parasgarg.tracker.core.strava

import com.parasgarg.tracker.data.model.WorkoutType
import com.parasgarg.tracker.data.model.domain.WorkoutDetail
import com.parasgarg.tracker.data.model.domain.WorkoutSession
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object StravaWorkoutMapper {

    fun map(activity: StravaActivity): WorkoutSession {
        val startTime = parseStartTime(activity.startDateLocal)
        val workoutType = mapType(activity.type)
        val distanceMeters = activity.distanceMeters
        val durationMinutes = maxOf(1, activity.elapsedSeconds / 60)
        val detail: WorkoutDetail = when (workoutType) {
            WorkoutType.RUNNING, WorkoutType.SWIMMING -> WorkoutDetail.Cardio(
                distanceMeters = distanceMeters,
                avgPaceSecondsPerKm = if (activity.averageSpeedMps > 0)
                    (1000.0 / activity.averageSpeedMps).toInt() else null,
                laps = null,
                poolLengthMeters = null,
            )
            else -> WorkoutDetail.None
        }
        return WorkoutSession(
            id = "strava_${activity.id}",
            type = workoutType,
            startTime = startTime,
            durationMinutes = durationMinutes,
            notes = activity.name,
            perceivedEffort = null,
            detail = detail,
        )
    }

    private fun mapType(stravaType: String): WorkoutType = when (stravaType.lowercase()) {
        "run", "virtualrun", "trailrun" -> WorkoutType.RUNNING
        "swim", "openwater" -> WorkoutType.SWIMMING
        "badminton" -> WorkoutType.BADMINTON
        "tabletennis" -> WorkoutType.TABLE_TENNIS
        "weighttraining", "workout" -> WorkoutType.STRENGTH
        else -> WorkoutType.OTHER
    }

    private fun parseStartTime(dateStr: String): Instant = runCatching {
        ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    }.getOrElse { Instant.now() }
}
