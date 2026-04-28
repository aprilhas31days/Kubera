package org.singhak.kubera.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.singhak.kubera.repository.TransactionRepository

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TransactionRepository
    @Inject lateinit var smsParser: SmsParser

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages.firstOrNull()?.originatingAddress ?: return

        val isRegisteredSender = smsParser.registeredBanks.any { bank ->
            sender.contains(bank, ignoreCase = true)
        }
        if (!isRegisteredSender) return

        val body = messages.joinToString("") { it.messageBody }
        val timestamp = messages.first().timestampMillis

        val transaction = smsParser.parse(sender = sender, sms = body)
            ?.copy(timestamp = timestamp) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insert(transaction)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
