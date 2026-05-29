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

    fun getCurrentMonthCategoryBreakdown(): Flow<List<CategorySpend>> {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return transactionDao.getCategoryBreakdown(monthStart)
    }

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
