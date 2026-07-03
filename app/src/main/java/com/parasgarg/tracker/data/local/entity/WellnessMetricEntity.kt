package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "wellness_metrics")
data class WellnessMetricEntity(
    @PrimaryKey val id: String,
    val recordedDate: LocalDate,
    val stepsCount: Int?,
    val activeCaloriesKcal: Double?,
    val sleepHours: Double?,
    val hrvMs: Double?,
    val recoveryScore: Int?,
    val restingHeartRate: Int?,
    val sourceName: String,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
