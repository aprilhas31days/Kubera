package org.singhak.kubera

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp
import org.singhak.kubera.notification.AlarmScheduler
import org.singhak.kubera.notification.DailySummaryReceiver

@HiltAndroidApp
class KuberaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        AlarmScheduler.schedule(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            DailySummaryReceiver.CHANNEL_ID,
            "Daily Summary",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Nightly transaction summary at 11 PM"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
