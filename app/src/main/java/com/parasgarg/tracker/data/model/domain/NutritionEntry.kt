package com.parasgarg.tracker.data.model.domain

import java.time.Instant
import java.time.LocalDate

data class NutritionEntry(
    val id: String,
    val date: LocalDate,
    val mealType: MealType,
    val foodName: String,
    val servingGrams: Double,
    val caloriesKcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val loggedAt: Instant,
)
