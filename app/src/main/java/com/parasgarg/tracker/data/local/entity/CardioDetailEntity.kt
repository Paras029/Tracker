package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cardio_details",
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
data class CardioDetailEntity(
    @PrimaryKey val sessionId: String,
    val distanceMeters: Double,
    val avgPaceSecondsPerKm: Int?,
    val laps: Int?,
    val poolLengthMeters: Int?,
    val routeGeoJson: String?,
)
