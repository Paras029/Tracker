package com.parasgarg.tracker.data.model.domain

import java.time.LocalDate

data class BcaReport(
    val id: String,
    val scannedAt: LocalDate,
    val bodyFatPercent: Double?,
    val skeletalMuscleMassKg: Double?,
    val visceralFatLevel: Int?,
    val waterPercent: Double?,
    val boneMassKg: Double?,
    val bmi: Double?,
    val deviceName: String?,
)
