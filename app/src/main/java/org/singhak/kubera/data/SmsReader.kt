package org.singhak.kubera.data

import android.content.ContentResolver
import android.provider.Telephony
import javax.inject.Inject
import org.singhak.kubera.model.Transaction

class SmsReader @Inject constructor(
    private val contentResolver: ContentResolver
) {

    /**
     * Reads bank SMS messages from the inbox and parses them into transactions.
     *
     * Queries SMS from registered banks since [afterTimestamp] or the start of
     * the current month, whichever is later.
     *
     * @param afterTimestamp only read SMS newer than this epoch millis
     *
     * @return parsed transactions sorted by date descending
     */
    fun readTransactions(afterTimestamp: Long): List<Transaction> {

        val senderTags = registeredBanks
        if (senderTags.isEmpty()) return emptyList()

        val likeClauses = senderTags.joinToString(" OR ") {
            "${Telephony.Sms.Inbox.ADDRESS} LIKE ?"
        }
        val selection = "${Telephony.Sms.Inbox.DATE} > ? AND ($likeClauses)"
        val selectionArgs =
            arrayOf(afterTimestamp.toString()) + senderTags.map { "%$it%" }.toTypedArray()

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
                val address = cursor.getString(addressIndex)
                val body = cursor.getString(bodyIndex)
                val date = cursor.getLong(dateIndex)

                val transaction = parseSms(address, body)?.copy(timestamp = date)
                if (transaction != null) {
                    transactions.add(transaction)
                }
            }
        }

        return transactions
    }
}
