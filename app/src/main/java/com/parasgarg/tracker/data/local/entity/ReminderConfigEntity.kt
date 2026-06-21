package com.parasgarg.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "reminder_configs")
data class ReminderConfigEntity(
    @PrimaryKey val id: String,
    val reminderType: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val daysOfWeek: Set<DayOfWeek>,
)
