package org.singhak.kubera.data

import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.Transaction

class TransactionRepository @Inject constructor(
    private val smsReader: SmsReader,
    private val transactionDao: TransactionDao,
) {

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

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

    suspend fun backfillFromSms() {
        if (transactionDao.getLastTimestamp() != null) return
        val transactions = smsReader.readTransactions()
        if (transactions.isNotEmpty()) {
            transactionDao.insertAll(transactions)
        }
    }
}
