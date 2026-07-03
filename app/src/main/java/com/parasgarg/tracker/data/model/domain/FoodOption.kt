package com.parasgarg.tracker.data.model.domain

data class FoodOption(
    val id: String,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val isCustom: Boolean = false,
)
