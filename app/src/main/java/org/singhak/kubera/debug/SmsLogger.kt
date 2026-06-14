package org.singhak.kubera.debug

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
import android.util.Log
import org.singhak.kubera.BuildConfig

@Suppress("MagicNumber")
object SmsLogger {
    private const val TAG = "SmsLogger"
    private const val INCOMING_PREVIEW_LEN = 120

    fun logIncomingSms(context: Context, sender: String, body: String) {
        if (!BuildConfig.DEBUG || !DebugPreferences.isSmsLoggingEnabled(context)) return
        Log.d(TAG, "incoming [$sender]: ${body.take(INCOMING_PREVIEW_LEN)}")
        val watched = DebugPreferences.getWatchedSenders(context)
        if (watched.any { sender.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "★ watched [$sender] full body:\n$body")
        }
    }

    @Suppress("LongMethod", "ReturnCount")
    fun scanInbox(context: Context, contentResolver: ContentResolver) {
        if (!BuildConfig.DEBUG || !DebugPreferences.isSmsLoggingEnabled(context)) return

        val senders = mutableSetOf<String>()
        contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.ADDRESS),
            null,
            null,
            "${Telephony.Sms.Inbox.DATE} DESC",
        )?.use { cursor ->
            val col = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            while (cursor.moveToNext()) cursor.getString(col)?.let { senders.add(it) }
        }

        Log.d(TAG, "=== SMS inbox senders (${senders.size}) ===\n${senders.sorted().joinToString("\n")}")

        val watchedList = DebugPreferences.getWatchedSenders(context)
        if (watchedList.isEmpty()) return

        Log.d(TAG, "=== messages matching ${watchedList.joinToString(", ") { "\"$it\"" }} ===")
        val selection = watchedList.joinToString(" OR ") { "${Telephony.Sms.Inbox.ADDRESS} LIKE ?" }
        val selectionArgs = watchedList.map { "%$it%" }.toTypedArray()
        var count = 0
        contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE),
            selection,
            selectionArgs,
            "${Telephony.Sms.Inbox.DATE} DESC",
        )?.use { cursor ->
            val addrCol = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            while (cursor.moveToNext()) {
                Log.d(TAG, "[${cursor.getString(addrCol)}]\n${cursor.getString(bodyCol)}\n---")
                count++
            }
        }
        Log.d(TAG, "=== $count message(s) found ===")
    }
}
