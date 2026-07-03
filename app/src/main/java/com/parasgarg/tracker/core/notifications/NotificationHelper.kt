package com.parasgarg.tracker.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.parasgarg.tracker.MainActivity

const val WORKOUT_REMINDER_CHANNEL_ID = "workout_reminder"
const val WORKOUT_REMINDER_NOTIFICATION_ID = 1001

fun createNotificationChannels(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nm.createNotificationChannel(
        NotificationChannel(
            WORKOUT_REMINDER_CHANNEL_ID,
            "Workout Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Daily reminder to log your workout"
        },
    )
}

fun showWorkoutReminderNotification(context: Context) {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val tapIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val notification = NotificationCompat.Builder(context, WORKOUT_REMINDER_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Time to move!")
        .setContentText("Log today's workout to keep your streak alive.")
        .setAutoCancel(true)
        .setContentIntent(tapIntent)
        .build()
    nm.notify(WORKOUT_REMINDER_NOTIFICATION_ID, notification)
}
