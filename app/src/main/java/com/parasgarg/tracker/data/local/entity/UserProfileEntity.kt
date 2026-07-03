package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val heightCm: Double?,
    val weightKg: Double?,
    val activityLevel: String?,
    val fitnessGoal: String?,
    val targetCaloriesKcal: Double?,
    val targetProteinG: Double?,
    val targetCarbsG: Double?,
    val targetFatG: Double?,
    val geminiApiKey: String?,
    val isGeminiOfflineMode: Boolean,
    val updatedAt: Instant,
)
