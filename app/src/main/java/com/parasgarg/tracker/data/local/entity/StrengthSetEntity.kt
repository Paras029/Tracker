package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "strength_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class StrengthSetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseName: String,
    val setOrder: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int?,
)
