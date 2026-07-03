package com.parasgarg.tracker.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.parasgarg.tracker.data.local.dao.ReminderConfigDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun reminderConfigDao(): ReminderConfigDao
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = EntryPointAccessors
                    .fromApplication(context.applicationContext, BootReceiverEntryPoint::class.java)
                    .reminderConfigDao()
                val configs = dao.observeAll().first()
                val scheduler = ReminderScheduler(context)
                configs.filter { it.isEnabled }.forEach { config ->
                    scheduler.schedule(config.hour, config.minute, config.id.hashCode())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
