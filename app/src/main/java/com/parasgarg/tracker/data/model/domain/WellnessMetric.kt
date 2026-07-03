package com.parasgarg.tracker.data.model.domain

import java.time.LocalDate

data class WellnessMetric(
    val id: String,
    val recordedDate: LocalDate,
    val stepsCount: Int?,
    val activeCaloriesKcal: Double?,
    val sleepHours: Double?,
    val hrvMs: Double?,
    val recoveryScore: Int?,
    val restingHeartRate: Int?,
    val sourceName: String,
)
