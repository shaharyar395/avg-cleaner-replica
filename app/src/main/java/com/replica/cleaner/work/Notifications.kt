package com.replica.cleaner.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.replica.cleaner.MainActivity
import com.replica.cleaner.R

/**
 * One channel per category in Settings > Notifications, so the user's choices
 * there map onto the system's own notification settings.
 */
object Notifications {

    const val CHANNEL_CLEANING = "junk_cleaning"
    const val CHANNEL_APPS = "applications"
    const val CHANNEL_PHOTOS = "photos"
    const val CHANNEL_OTHER = "other_files"
    const val CHANNEL_PROGRESS = "progress"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        listOf(
            Triple(CHANNEL_CLEANING, context.getString(R.string.chan_cleaning), NotificationManager.IMPORTANCE_DEFAULT),
            Triple(CHANNEL_APPS, context.getString(R.string.chan_apps), NotificationManager.IMPORTANCE_LOW),
            Triple(CHANNEL_PHOTOS, context.getString(R.string.chan_photos), NotificationManager.IMPORTANCE_LOW),
            Triple(CHANNEL_OTHER, context.getString(R.string.chan_other), NotificationManager.IMPORTANCE_LOW),
            Triple(CHANNEL_PROGRESS, context.getString(R.string.chan_progress), NotificationManager.IMPORTANCE_MIN)
        ).forEach { (id, name, importance) ->
            manager.createNotificationChannel(NotificationChannel(id, name, importance))
        }
    }

    fun notify(context: Context, channel: String, id: Int, title: String, body: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_cleaner)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(id, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS was revoked between the check and the call.
        }
    }

    fun progressNotification(context: Context, text: String): Notification =
        NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(R.drawable.ic_stat_cleaner)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()
}
