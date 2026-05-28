package org.singhak.kubera.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.singhak.kubera.MainActivity
import org.singhak.kubera.R
import org.singhak.kubera.db.TransactionDao
import org.singhak.kubera.model.TransactionCategory

@AndroidEntryPoint
class DailySummaryReceiver : BroadcastReceiver() {

    @Inject lateinit var transactionDao: TransactionDao

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                postSummaryNotification(context)
            } finally {
                pendingResult.finish()
                AlarmScheduler.schedule(context)
            }
        }
    }

    @Suppress("MagicNumber")
    private suspend fun postSummaryNotification(context: Context) {
        Log.d(TAG, "postSummaryNotification: notificationsAllowed=${notificationsAllowed(context)}")
        if (!notificationsAllowed(context)) return

        val (todayStart, todayEnd) = todayRange()
        val transactions = transactionDao.getTransactionsBetween(todayStart, todayEnd)
        Log.d(TAG, "todayRange=$todayStart..$todayEnd  found=${transactions.size}")
        if (transactions.isEmpty()) return

        val categorised = transactions.count { it.category != TransactionCategory.OTHER }
        val breakdown = transactions
            .groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }

        val tapPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val style = NotificationCompat.InboxStyle()
            .setSummaryText("$categorised of ${transactions.size} categorised")
        breakdown.forEach { (category, total) ->
            style.addLine("${category.displayName} · ₹${"%.2f".format(total)}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${transactions.size} transactions today")
            .setContentText("$categorised auto-categorised")
            .setStyle(style)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun notificationsAllowed(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    @Suppress("MagicNumber")
    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = start + 24L * 60 * 60 * 1000
        return start to end
    }

    companion object {
        const val CHANNEL_ID = "daily_summary"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "DailySummaryReceiver"
    }
}
