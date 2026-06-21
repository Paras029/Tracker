package com.parasgarg.tracker.data.model.domain

import java.time.Instant

data class BodyMetric(
    val id: String,
    val recordedAt: Instant,
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val restingHeartRate: Int?,
    val notes: String?,
)
