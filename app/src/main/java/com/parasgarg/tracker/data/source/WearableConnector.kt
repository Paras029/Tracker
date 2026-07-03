package com.parasgarg.tracker.data.source

import com.parasgarg.tracker.data.model.domain.WellnessMetric
import java.time.LocalDate
import java.util.UUID

interface WearableConnector {
    val sourceId: String
    val displayName: String
    suspend fun fetchTodayMetrics(): WellnessMetric?
}

class SamsungHealthConnector : WearableConnector {
    override val sourceId = "samsung_health"
    override val displayName = "Samsung Health"

    override suspend fun fetchTodayMetrics(): WellnessMetric {
        val date = LocalDate.now()
        val seed = (date.toEpochDay() % 10).toInt()
        return WellnessMetric(
            id = "samsung_${date.toEpochDay()}",
            recordedDate = date,
            stepsCount = 7200 + seed * 300,
            activeCaloriesKcal = 280.0 + seed * 15,
            sleepHours = 6.5 + (seed % 4) * 0.25,
            hrvMs = 52.0 + seed * 2.5,
            recoveryScore = 65 + (seed % 5) * 4,
            restingHeartRate = 63 - (seed % 4),
            sourceName = sourceId,
        )
    }
}

class GoogleFitConnector : WearableConnector {
    override val sourceId = "google_fit"
    override val displayName = "Google Fit"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class OuraConnector : WearableConnector {
    override val sourceId = "oura"
    override val displayName = "Oura Ring"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class FitbitConnector : WearableConnector {
    override val sourceId = "fitbit"
    override val displayName = "Fitbit"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class WhoopConnector : WearableConnector {
    override val sourceId = "whoop"
    override val displayName = "Whoop"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class GarminConnector : WearableConnector {
    override val sourceId = "garmin"
    override val displayName = "Garmin"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}
