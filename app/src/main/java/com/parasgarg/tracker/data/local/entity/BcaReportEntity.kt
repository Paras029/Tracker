package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "bca_reports")
data class BcaReportEntity(
    @PrimaryKey val id: String,
    val scannedAt: LocalDate,
    val bodyFatPercent: Double?,
    val skeletalMuscleMassKg: Double?,
    val visceralFatLevel: Int?,
    val waterPercent: Double?,
    val boneMassKg: Double?,
    val bmi: Double?,
    val deviceName: String?,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
