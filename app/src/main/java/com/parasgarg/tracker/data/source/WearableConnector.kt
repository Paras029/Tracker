package com.parasgarg.tracker.data.source

import com.parasgarg.tracker.core.health.HealthConnectManager
import com.parasgarg.tracker.data.model.domain.WellnessMetric
import java.time.LocalDate
import javax.inject.Inject

interface WearableConnector {
    val sourceId: String
    val displayName: String
    suspend fun fetchTodayMetrics(): WellnessMetric?
}

class SamsungHealthConnector @Inject constructor(
    private val hcManager: HealthConnectManager,
) : WearableConnector {
    override val sourceId = "samsung_health"
    override val displayName = "Samsung Health"

    override suspend fun fetchTodayMetrics(): WellnessMetric? {
        if (!hcManager.isAvailable()) return simulatedMetrics()
        val date = LocalDate.now()
        val steps = hcManager.readTodaySteps()
        val calories = hcManager.readTodayActiveCalories()
        val sleep = hcManager.readLastNightSleepHours()
        val hrv = hcManager.readHrv()
        val restingHr = hcManager.readRestingHeartRate()
        if (steps == null && calories == null && sleep == null) return null
        return WellnessMetric(
            id = "samsung_hc_${date.toEpochDay()}",
            recordedDate = date,
            stepsCount = steps,
            activeCaloriesKcal = calories,
            sleepHours = sleep,
            hrvMs = hrv,
            recoveryScore = null,
            restingHeartRate = restingHr,
            sourceName = sourceId,
        )
    }

    private fun simulatedMetrics(): WellnessMetric {
        val date = LocalDate.now()
        val seed = (date.toEpochDay() % 10).toInt()
        return WellnessMetric(
            id = "samsung_sim_${date.toEpochDay()}",
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

class GoogleFitConnector @Inject constructor(
    private val hcManager: HealthConnectManager,
) : WearableConnector {
    override val sourceId = "google_fit"
    override val displayName = "Google Fit"

    override suspend fun fetchTodayMetrics(): WellnessMetric? {
        if (!hcManager.isAvailable()) return null
        val date = LocalDate.now()
        val steps = hcManager.readTodaySteps()
        val calories = hcManager.readTodayActiveCalories()
        if (steps == null && calories == null) return null
        return WellnessMetric(
            id = "googlefit_hc_${date.toEpochDay()}",
            recordedDate = date,
            stepsCount = steps,
            activeCaloriesKcal = calories,
            sleepHours = null,
            hrvMs = null,
            recoveryScore = null,
            restingHeartRate = hcManager.readRestingHeartRate(),
            sourceName = sourceId,
        )
    }
}

class OuraConnector @Inject constructor() : WearableConnector {
    override val sourceId = "oura"
    override val displayName = "Oura Ring"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class FitbitConnector @Inject constructor() : WearableConnector {
    override val sourceId = "fitbit"
    override val displayName = "Fitbit"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class WhoopConnector @Inject constructor() : WearableConnector {
    override val sourceId = "whoop"
    override val displayName = "Whoop"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}

class GarminConnector @Inject constructor() : WearableConnector {
    override val sourceId = "garmin"
    override val displayName = "Garmin"
    override suspend fun fetchTodayMetrics(): WellnessMetric? = null
}
