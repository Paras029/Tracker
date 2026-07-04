package com.parasgarg.tracker.core.di

import com.parasgarg.tracker.data.repository.AuthRepository
import com.parasgarg.tracker.data.repository.FirebaseAuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepositoryImpl): AuthRepository
}
