package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey val id: String,
    val recordedAt: Instant,
    val weightKg: Double?,
    val bodyFatPercent: Double?,
    val restingHeartRate: Int?,
    val notes: String?,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
