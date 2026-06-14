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
import org.singhak.kubera.model.Autopay
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.repository.AutopayRepository
import org.singhak.kubera.repository.TransactionRepository

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var transactionRepository: TransactionRepository
    @Inject lateinit var autopayRepository: AutopayRepository
    @Inject lateinit var smsParser: SmsParser

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages.firstOrNull()?.originatingAddress ?: return

        val isRegisteredSender = smsParser.registeredBanks.any { sender.contains(it, ignoreCase = true) }
        if (!isRegisteredSender) return

        val body = messages.joinToString("") { it.messageBody }
        val timestamp = messages.first().timestampMillis
        val parsed = smsParser.parse(sender = sender, sms = body) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (parsed.channel == TransactionChannel.AUTOPAY) {
                    val merchant = parsed.merchant ?: return@launch
                    when (parsed.type) {
                        TransactionType.DEBIT -> {
                            val nextDue = extractAutopayDate(body) ?: return@launch
                            autopayRepository.upsert(Autopay(merchant, parsed.amount, parsed.bank, nextDue))
                        }
                        TransactionType.REVOKE -> autopayRepository.revoke(merchant)
                        else -> {}
                    }
                } else {
                    transactionRepository.insert(parsed.copy(timestamp = timestamp))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
