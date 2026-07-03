package com.parasgarg.tracker.core.di

import com.parasgarg.tracker.data.repository.BodyMetricRepository
import com.parasgarg.tracker.data.repository.BodyMetricRepositoryImpl
import com.parasgarg.tracker.data.repository.ClinicalRepository
import com.parasgarg.tracker.data.repository.ClinicalRepositoryImpl
import com.parasgarg.tracker.data.repository.NutritionRepository
import com.parasgarg.tracker.data.repository.NutritionRepositoryImpl
import com.parasgarg.tracker.data.repository.WellnessRepository
import com.parasgarg.tracker.data.repository.WellnessRepositoryImpl
import com.parasgarg.tracker.data.repository.UserProfileRepository
import com.parasgarg.tracker.data.repository.UserProfileRepositoryImpl
import com.parasgarg.tracker.data.repository.WorkoutRepository
import com.parasgarg.tracker.data.repository.WorkoutRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindBodyMetricRepository(impl: BodyMetricRepositoryImpl): BodyMetricRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository

    @Binds
    @Singleton
    abstract fun bindWellnessRepository(impl: WellnessRepositoryImpl): WellnessRepository

    @Binds
    @Singleton
    abstract fun bindClinicalRepository(impl: ClinicalRepositoryImpl): ClinicalRepository
}
