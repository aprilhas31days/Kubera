package org.singhak.kubera.sms

import android.content.ContentResolver
import android.provider.Telephony
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import org.singhak.kubera.model.Autopay
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType

class SmsReader @Inject constructor(
    private val contentResolver: ContentResolver,
    private val smsParser: SmsParser,
) {
    fun readTransactions(afterTimestamp: Long): List<Transaction> =
        queryRaw(afterTimestamp).mapNotNull { (address, body, date) ->
            smsParser.parse(address, body)
                ?.takeIf { it.channel != TransactionChannel.AUTOPAY }
                ?.copy(timestamp = date)
        }

    fun readAutopays(afterTimestamp: Long): List<Autopay> {
        // Process oldest-first so revokes correctly cancel earlier pre-debit entries
        val active = mutableMapOf<String, Autopay>()
        for ((address, body, _) in queryRaw(afterTimestamp).sortedBy { it.third }) {
            val parsed = smsParser.parse(address, body)
                ?.takeIf { it.channel == TransactionChannel.AUTOPAY } ?: continue
            val merchant = parsed.merchant ?: continue
            when (parsed.type) {
                TransactionType.DEBIT -> {
                    val nextDue = extractAutopayDate(body) ?: continue
                    active[merchant] = Autopay(merchant, parsed.amount, parsed.bank, nextDue)
                }
                TransactionType.REVOKE -> active.remove(merchant)
                else -> {}
            }
        }
        return active.values.toList()
    }

    private fun queryRaw(afterTimestamp: Long): List<Triple<String, String, Long>> {
        val banks = smsParser.registeredBanks
        val likeClauses = banks.joinToString(" OR ") { "${Telephony.Sms.Inbox.ADDRESS} LIKE ?" }
        val selection = "${Telephony.Sms.Inbox.DATE} > ? AND ($likeClauses)"
        val selectionArgs = arrayOf(afterTimestamp.toString()) + banks.map { "%$it%" }.toTypedArray()

        val results = mutableListOf<Triple<String, String, Long>>()
        contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE),
            selection,
            selectionArgs,
            "${Telephony.Sms.Inbox.DATE} DESC",
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE)
            while (cursor.moveToNext()) {
                results.add(
                    Triple(
                        cursor.getString(addressIndex),
                        cursor.getString(bodyIndex),
                        cursor.getLong(dateIndex),
                    )
                )
            }
        }
        return results
    }
}

private val autopayDateRegex = Regex("""Autopay on (\d{2}-[A-Za-z]{3}-\d{2})""")
private val autopayDateFormat = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)

fun extractAutopayDate(sms: String): Long? {
    val dateStr = autopayDateRegex.find(sms)?.groupValues?.get(1) ?: return null
    return runCatching { autopayDateFormat.parse(dateStr)?.time }.getOrNull()
}
