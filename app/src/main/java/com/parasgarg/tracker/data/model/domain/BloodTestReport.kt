package com.parasgarg.tracker.data.model.domain

import java.time.LocalDate

data class BloodTestReport(
    val id: String,
    val testedAt: LocalDate,
    val labName: String?,
    val hemoglobinGdl: Double?,
    val vitaminD3NgMl: Double?,
    val vitaminB12PgMl: Double?,
    val ldlMgDl: Double?,
    val hdlMgDl: Double?,
    val fastingBloodSugarMgDl: Double?,
    val hba1cPercent: Double?,
    val notes: String?,
)
