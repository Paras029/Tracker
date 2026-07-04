package com.parasgarg.tracker.core.ai

import java.time.LocalDate

data class BcaScanResult(
    val bodyFatPercent: Double?,
    val skeletalMuscleMassKg: Double?,
    val visceralFatLevel: Int?,
    val waterPercent: Double?,
    val boneMassKg: Double?,
    val bmi: Double?,
)

data class BloodTestScanResult(
    val hemoglobinGdL: Double?,
    val vitaminD3NgmL: Double?,
    val vitaminB12PgmL: Double?,
    val ldlMgdL: Double?,
    val hdlMgdL: Double?,
    val fastingGlucoseMgdL: Double?,
    val hba1cPercent: Double?,
    val testedDate: LocalDate?,
)

data class FoodLabelScanResult(
    val foodName: String?,
    val servingGrams: Double?,
    val caloriesKcal: Double?,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?,
)
