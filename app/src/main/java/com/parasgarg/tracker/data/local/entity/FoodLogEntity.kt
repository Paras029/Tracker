package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "food_log")
data class FoodLogEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val mealType: String,
    val foodName: String,
    val servingGrams: Double,
    val caloriesKcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val loggedAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val updatedAt: Instant,
)
