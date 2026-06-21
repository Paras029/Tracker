package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.WorkoutType
import java.time.Instant

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val type: WorkoutType,
    val startTime: Instant,
    val durationMinutes: Int,
    val notes: String?,
    val perceivedEffort: Int?,
    val sourceId: String,
    val externalId: String?,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
