package com.parasgarg.tracker.core.di

import com.parasgarg.tracker.data.source.FitbitConnector
import com.parasgarg.tracker.data.source.GarminConnector
import com.parasgarg.tracker.data.source.GoogleFitConnector
import com.parasgarg.tracker.data.source.OuraConnector
import com.parasgarg.tracker.data.source.SamsungHealthConnector
import com.parasgarg.tracker.data.source.WearableConnector
import com.parasgarg.tracker.data.source.WhoopConnector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class WearableModule {

    @Binds @IntoSet
    abstract fun bindSamsungHealth(impl: SamsungHealthConnector): WearableConnector

    @Binds @IntoSet
    abstract fun bindGoogleFit(impl: GoogleFitConnector): WearableConnector

    @Binds @IntoSet
    abstract fun bindOura(impl: OuraConnector): WearableConnector

    @Binds @IntoSet
    abstract fun bindFitbit(impl: FitbitConnector): WearableConnector

    @Binds @IntoSet
    abstract fun bindWhoop(impl: WhoopConnector): WearableConnector

    @Binds @IntoSet
    abstract fun bindGarmin(impl: GarminConnector): WearableConnector
}
