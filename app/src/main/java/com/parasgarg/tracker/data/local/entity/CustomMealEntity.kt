package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant

@Entity(tableName = "custom_meals")
data class CustomMealEntity(
    @PrimaryKey val id: String,
    val name: String,
    val servingGrams: Double,
    val caloriesKcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val createdAt: Instant,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val updatedAt: Instant,
)
