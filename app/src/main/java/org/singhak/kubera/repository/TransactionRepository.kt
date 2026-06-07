package org.singhak.kubera.repository

import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.db.CategoryRule
import org.singhak.kubera.db.CategoryRuleDao
import org.singhak.kubera.db.RuleSource
import org.singhak.kubera.db.TransactionDao
import org.singhak.kubera.db.categorize
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.DailySpend
import org.singhak.kubera.model.MerchantSpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.MonthlySpend
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.sms.SmsReader

class TransactionRepository @Inject constructor(
    private val smsReader: SmsReader,
    private val transactionDao: TransactionDao,
    private val categoryRuleDao: CategoryRuleDao
) {
    suspend fun insert(transaction: Transaction) {
        val rules = categoryRuleDao.getAllRules()
        transactionDao.insert(transaction.copy(category = categorize(transaction.merchant, rules)))
    }

    fun getMonthSummary(fromTimestamp: Long, toTimestamp: Long): Flow<MonthSummary> =
        transactionDao.getMonthSummary(fromTimestamp, toTimestamp)

    fun getTransactionsForMonth(fromTimestamp: Long, toTimestamp: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetweenFlow(fromTimestamp, toTimestamp)

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getCategoryBreakdown(fromTimestamp: Long, toTimestamp: Long): Flow<List<CategorySpend>> =
        transactionDao.getCategoryBreakdown(fromTimestamp, toTimestamp)

    fun getUserRules(): Flow<List<CategoryRule>> = categoryRuleDao.getUserRules()

    suspend fun addUserRule(keyword: String, category: TransactionCategory) {
        val rule = CategoryRule(keyword = keyword.trim(), category = category, source = RuleSource.USER)
        categoryRuleDao.insert(rule)
        transactionDao.recategorizeExact(keyword.trim(), category)
    }

    suspend fun deleteUserRule(rule: CategoryRule) {
        categoryRuleDao.delete(rule)
        val remainingRules = categoryRuleDao.getAllRules()
        transactionDao.getByMerchantExact(rule.keyword).forEach { txn ->
            transactionDao.updateCategory(txn.id, categorize(txn.merchant, remainingRules))
        }
    }

    suspend fun updateTransaction(transaction: Transaction, applyRule: Boolean) {
        transactionDao.update(transaction)
        if (applyRule && !transaction.merchant.isNullOrBlank()) {
            addUserRule(transaction.merchant.trim(), transaction.category)
        }
    }

    suspend fun insertManual(transaction: Transaction, applyRule: Boolean) {
        transactionDao.insert(transaction)
        if (applyRule && !transaction.merchant.isNullOrBlank()) {
            addUserRule(transaction.merchant.trim(), transaction.category)
        }
    }

    fun getMonthlySpend(fromTimestamp: Long): Flow<List<MonthlySpend>> =
        transactionDao.getMonthlySpend(fromTimestamp)

    fun getDailySpend(fromTimestamp: Long): Flow<List<DailySpend>> =
        transactionDao.getDailySpend(fromTimestamp)

    fun getTopMerchants(fromTimestamp: Long, limit: Int = 5): Flow<List<MerchantSpend>> =
        transactionDao.getTopMerchants(fromTimestamp, limit)

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

fun monthRange(year: Int, month: Int): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val from = cal.timeInMillis
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val to = cal.timeInMillis
    return from to to
}
