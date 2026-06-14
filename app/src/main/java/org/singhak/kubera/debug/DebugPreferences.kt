package org.singhak.kubera.debug

import android.content.Context
import androidx.core.content.edit

object DebugPreferences {
    private const val PREFS = "kubera_debug"
    private const val KEY_SMS_LOGGING = "sms_logging_enabled"
    private const val KEY_WATCHED_SENDER = "watched_sender"

    fun isSmsLoggingEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SMS_LOGGING, false)

    fun setSmsLoggingEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit { putBoolean(KEY_SMS_LOGGING, enabled) }

    fun getWatchedSender(context: Context): String? =
        prefs(context).getString(KEY_WATCHED_SENDER, null).takeUnless { it.isNullOrBlank() }

    fun getWatchedSenders(context: Context): List<String> =
        getWatchedSender(context)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    fun setWatchedSender(context: Context, sender: String?) =
        prefs(context).edit { putString(KEY_WATCHED_SENDER, sender?.takeUnless { it.isBlank() }) }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
