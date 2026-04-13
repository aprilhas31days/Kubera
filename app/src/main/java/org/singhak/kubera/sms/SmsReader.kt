package org.singhak.kubera.sms

import android.content.ContentResolver
import android.provider.Telephony
import javax.inject.Inject
import org.singhak.kubera.model.Transaction

class SmsReader @Inject constructor(private val contentResolver: ContentResolver) {
    fun readTransactions(afterTimestamp: Long): List<Transaction> {
        val senderTags = registeredBanks
        if (senderTags.isEmpty()) return emptyList()

        val likeClauses = senderTags.joinToString(" OR ") {
            "${Telephony.Sms.Inbox.ADDRESS} LIKE ?"
        }
        val selection = "${Telephony.Sms.Inbox.DATE} > ? AND ($likeClauses)"
        val selectionArgs =
            arrayOf(afterTimestamp.toString()) + senderTags.map { "%$it%" }.toTypedArray()

        return querySmsInbox(selection, selectionArgs)
    }

    private fun querySmsInbox(selection: String, selectionArgs: Array<String>): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.BODY,
                Telephony.Sms.Inbox.DATE
            ),
            selection,
            selectionArgs,
            "${Telephony.Sms.Inbox.DATE} DESC"
        )?.use { cursor ->
            val addressIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS)
            val bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY)
            val dateIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE)

            while (cursor.moveToNext()) {
                parseSms(cursor.getString(addressIndex), cursor.getString(bodyIndex))
                    ?.copy(timestamp = cursor.getLong(dateIndex))
                    ?.let { transactions.add(it) }
            }
        }

        return transactions
    }
}
