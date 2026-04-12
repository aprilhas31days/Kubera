package org.singhak.kubera.data

import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction

class TransactionRepository @Inject constructor(
    private val smsReader: SmsReader,
    private val transactionDao: TransactionDao,
    private val categoryRuleDao: CategoryRuleDao,
) {

    suspend fun insert(transaction: Transaction) {
        val rules = categoryRuleDao.getAllRules()
        transactionDao.insert(transaction.copy(category = categorize(transaction.merchant, rules)))
    }

    fun getCurrentMonthSummary(): Flow<MonthSummary> {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return transactionDao.getMonthSummary(monthStart)
    }

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun backfillFromDate(fromDate: Long): Boolean {
        val rules = categoryRuleDao.getAllRules()
        val transactions = smsReader.readTransactions(afterTimestamp = fromDate)
            .map { it.copy(category = categorize(it.merchant, rules)) }
        if (transactions.isNotEmpty()) {
            transactionDao.insertAll(transactions)
        }
        return transactions.isNotEmpty()
    }
}
