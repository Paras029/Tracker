package com.parasgarg.tracker.core.health

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val requiredPermissions: Set<String> = setOf(
        "android.permission.health.READ_STEPS",
        "android.permission.health.READ_ACTIVE_CALORIES_BURNED",
        "android.permission.health.READ_SLEEP",
        "android.permission.health.READ_RESTING_HEART_RATE",
        "android.permission.health.READ_HEART_RATE_VARIABILITY",
        "android.permission.health.READ_EXERCISE",
    )

    /**
     * On Android 14+ (API 34) Health Connect is built into the OS — no standalone app needed.
     * On older versions, check for the Play Store app package.
     */
    fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        return isHealthConnectInstalled()
    }

    suspend fun hasPermissions(): Boolean = false

    suspend fun readTodaySteps(): Int? = null
    suspend fun readTodayActiveCalories(): Double? = null
    suspend fun readLastNightSleepHours(): Double? = null
    suspend fun readRestingHeartRate(): Int? = null
    suspend fun readHrv(): Double? = null

    private fun isHealthConnectInstalled(): Boolean = runCatching {
        context.packageManager.getPackageInfo("com.google.android.apps.healthdata", 0)
        true
    }.getOrDefault(false)
}
