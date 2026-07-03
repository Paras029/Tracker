package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "blood_test_reports")
data class BloodTestReportEntity(
    @PrimaryKey val id: String,
    val testedAt: LocalDate,
    val labName: String?,
    val hemoglobinGdl: Double?,
    val vitaminD3NgMl: Double?,
    val vitaminB12PgMl: Double?,
    val ldlMgDl: Double?,
    val hdlMgDl: Double?,
    val fastingBloodSugarMgDl: Double?,
    val hba1cPercent: Double?,
    val notes: String?,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
