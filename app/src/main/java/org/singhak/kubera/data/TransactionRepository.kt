package org.singhak.kubera.data

import android.content.ContentResolver
import org.singhak.kubera.model.Transaction

class TransactionRepository(private val contentResolver: ContentResolver) {

    fun getCurrentMonthTransactions(): List<Transaction> =
        readCurrentMonthTransactions(contentResolver)
}
