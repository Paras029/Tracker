package com.parasgarg.tracker.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.parasgarg.tracker.data.local.dao.BodyMetricDao
import com.parasgarg.tracker.data.local.dao.CardioDetailDao
import com.parasgarg.tracker.data.local.dao.NutritionDao
import com.parasgarg.tracker.data.local.dao.RacquetSportDetailDao
import com.parasgarg.tracker.data.local.dao.ReminderConfigDao
import com.parasgarg.tracker.data.local.dao.StrengthSetDao
import com.parasgarg.tracker.data.local.dao.UserProfileDao
import com.parasgarg.tracker.data.local.dao.WorkoutSessionDao
import com.parasgarg.tracker.data.local.entity.BodyMetricEntity
import com.parasgarg.tracker.data.local.entity.CardioDetailEntity
import com.parasgarg.tracker.data.local.entity.CustomMealEntity
import com.parasgarg.tracker.data.local.entity.FoodLogEntity
import com.parasgarg.tracker.data.local.entity.RacquetSportDetailEntity
import com.parasgarg.tracker.data.local.entity.ReminderConfigEntity
import com.parasgarg.tracker.data.local.entity.StrengthSetEntity
import com.parasgarg.tracker.data.local.entity.UserProfileEntity
import com.parasgarg.tracker.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        WorkoutSessionEntity::class,
        StrengthSetEntity::class,
        CardioDetailEntity::class,
        RacquetSportDetailEntity::class,
        BodyMetricEntity::class,
        ReminderConfigEntity::class,
        UserProfileEntity::class,
        FoodLogEntity::class,
        CustomMealEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
@TypeConverters(Converters::class)
abstract class TrackerDatabase : RoomDatabase() {
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun strengthSetDao(): StrengthSetDao
    abstract fun cardioDetailDao(): CardioDetailDao
    abstract fun racquetSportDetailDao(): RacquetSportDetailDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun nutritionDao(): NutritionDao

    companion object {
        const val DATABASE_NAME = "tracker.db"
    }
}
