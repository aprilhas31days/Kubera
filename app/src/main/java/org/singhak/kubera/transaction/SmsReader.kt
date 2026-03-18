package org.singhak.kubera.transaction

import android.content.ContentResolver
import android.provider.Telephony
import java.util.Calendar

fun readCurrentMonthTransactions(contentResolver: ContentResolver): List<Transaction> {
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val senderTags = knownSenderTags
    if (senderTags.isEmpty()) return emptyList()

    val likeClauses = senderTags.joinToString(" OR ") { "${Telephony.Sms.Inbox.ADDRESS} LIKE ?" }
    val selection = "${Telephony.Sms.Inbox.DATE} >= ? AND ($likeClauses)"
    val selectionArgs = arrayOf(monthStart.toString()) + senderTags.map { "%$it%" }.toTypedArray()

    val transactions = mutableListOf<Transaction>()

    contentResolver.query(
        Telephony.Sms.Inbox.CONTENT_URI,
        arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE),
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
