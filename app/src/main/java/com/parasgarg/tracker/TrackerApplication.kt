package com.parasgarg.tracker

import android.app.Application
import com.parasgarg.tracker.core.notifications.createNotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels(this)
    }
}
