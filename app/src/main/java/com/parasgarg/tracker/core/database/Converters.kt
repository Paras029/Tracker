package com.parasgarg.tracker.core.database

import androidx.room.TypeConverter
import com.parasgarg.tracker.data.model.SyncStatus
import com.parasgarg.tracker.data.model.WorkoutType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String = value.name

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType = WorkoutType.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromDaysOfWeek(value: Set<DayOfWeek>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toDaysOfWeek(value: String): Set<DayOfWeek> =
        if (value.isBlank()) emptySet() else value.split(",").map(DayOfWeek::valueOf).toSet()
}
