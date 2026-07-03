package com.parasgarg.tracker.core.di

import android.content.Context
import androidx.room.Room
import com.parasgarg.tracker.core.database.TrackerDatabase
import com.parasgarg.tracker.data.local.dao.BodyMetricDao
import com.parasgarg.tracker.data.local.dao.CardioDetailDao
import com.parasgarg.tracker.data.local.dao.NutritionDao
import com.parasgarg.tracker.data.local.dao.RacquetSportDetailDao
import com.parasgarg.tracker.data.local.dao.WellnessDao
import com.parasgarg.tracker.data.local.dao.ReminderConfigDao
import com.parasgarg.tracker.data.local.dao.StrengthSetDao
import com.parasgarg.tracker.data.local.dao.UserProfileDao
import com.parasgarg.tracker.data.local.dao.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTrackerDatabase(@ApplicationContext context: Context): TrackerDatabase =
        Room.databaseBuilder(context, TrackerDatabase::class.java, TrackerDatabase.DATABASE_NAME).build()

    @Provides
    fun provideWorkoutSessionDao(database: TrackerDatabase): WorkoutSessionDao =
        database.workoutSessionDao()

    @Provides
    fun provideStrengthSetDao(database: TrackerDatabase): StrengthSetDao =
        database.strengthSetDao()

    @Provides
    fun provideCardioDetailDao(database: TrackerDatabase): CardioDetailDao =
        database.cardioDetailDao()

    @Provides
    fun provideRacquetSportDetailDao(database: TrackerDatabase): RacquetSportDetailDao =
        database.racquetSportDetailDao()

    @Provides
    fun provideBodyMetricDao(database: TrackerDatabase): BodyMetricDao =
        database.bodyMetricDao()

    @Provides
    fun provideReminderConfigDao(database: TrackerDatabase): ReminderConfigDao =
        database.reminderConfigDao()

    @Provides
    fun provideUserProfileDao(database: TrackerDatabase): UserProfileDao =
        database.userProfileDao()

    @Provides
    fun provideNutritionDao(database: TrackerDatabase): NutritionDao =
        database.nutritionDao()

    @Provides
    fun provideWellnessDao(database: TrackerDatabase): WellnessDao =
        database.wellnessDao()
}
