package com.parasgarg.tracker.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showWorkoutReminderNotification(context)
        // Re-schedule tomorrow's alarm via the scheduler
        ReminderScheduler(context).rescheduleDaily(
            hour = intent.getIntExtra(ReminderScheduler.EXTRA_HOUR, 8),
            minute = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTE, 0),
            requestCode = intent.getIntExtra(ReminderScheduler.EXTRA_REQUEST_CODE, 0),
        )
    }
}
