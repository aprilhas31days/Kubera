package org.singhak.kubera.data

import android.content.ContentResolver
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.Transaction

class TransactionRepository(
    private val contentResolver: ContentResolver,
    private val transactionDao: TransactionDao
) {

    fun getCurrentMonthTransactions(): Flow<List<Transaction>> {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return transactionDao.getTransactionsSince(monthStart)
    }

    suspend fun syncFromSms() {
        val lastTimestamp = transactionDao.getLastTimestamp()
        val newTransactions = readSmsTransactions(contentResolver, lastTimestamp)
        if (newTransactions.isNotEmpty()) {
            transactionDao.insertAll(newTransactions)
        }
    }
}
