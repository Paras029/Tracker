package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "racquet_sport_details",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId", unique = true)],
)
data class RacquetSportDetailEntity(
    @PrimaryKey val sessionId: String,
    val opponentName: String?,
    val setsWon: Int,
    val setsLost: Int,
    val scoreSummary: String?,
)
