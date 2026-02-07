package com.mediationmixer.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MeditationMixerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackChannel = NotificationChannel(
                CHANNEL_PLAYBACK,
                getString(R.string.notification_channel_playback),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_playback_desc)
                setShowBadge(false)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                getString(R.string.notification_channel_reminder),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_reminder_desc)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(playbackChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        const val CHANNEL_PLAYBACK = "playback_channel"
        const val CHANNEL_REMINDER = "reminder_channel"
    }
}
