package com.parasgarg.tracker.core.health

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Health Connect SDK integration. This stub version does not depend on the
 * health-connect-client SDK (requires >= 1.1.0 when available). All reads return null
 * and isAvailable() returns false until the SDK is added to the build.
 *
 * To activate real HC integration:
 * 1. Uncomment `implementation(libs.health.connect.client)` in app/build.gradle.kts
 * 2. Replace this file with the full implementation in core/health/HealthConnectManager.kt
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Health manifest permission strings declared in AndroidManifest.xml */
    val requiredPermissions: Set<String> = setOf(
        "android.permission.health.READ_STEPS",
        "android.permission.health.READ_ACTIVE_CALORIES_BURNED",
        "android.permission.health.READ_SLEEP",
        "android.permission.health.READ_RESTING_HEART_RATE",
        "android.permission.health.READ_HEART_RATE_VARIABILITY",
        "android.permission.health.READ_EXERCISE",
    )

    /** Returns true when the Health Connect SDK is available on the device. */
    fun isAvailable(): Boolean = isHealthConnectInstalled()

    suspend fun hasPermissions(): Boolean = false

    suspend fun readTodaySteps(): Int? = null
    suspend fun readTodayActiveCalories(): Double? = null
    suspend fun readLastNightSleepHours(): Double? = null
    suspend fun readRestingHeartRate(): Int? = null
    suspend fun readHrv(): Double? = null

    /** Checks for Health Connect app package (Samsung Health / HC provider). */
    private fun isHealthConnectInstalled(): Boolean = runCatching {
        context.packageManager.getPackageInfo("com.google.android.apps.healthdata", 0)
        true
    }.getOrDefault(false)
}
