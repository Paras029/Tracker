package com.parasgarg.tracker.data.model.domain

data class UserProfile(
    val name: String = "",
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val activityLevel: String? = null,
    val fitnessGoal: String? = null,
    val targetCaloriesKcal: Double? = null,
    val targetProteinG: Double? = null,
    val targetCarbsG: Double? = null,
    val targetFatG: Double? = null,
    val geminiApiKey: String? = null,
    val isGeminiOfflineMode: Boolean = false,
)
