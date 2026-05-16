package org.singhak.kubera.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN type = 'DEBIT' THEN amount ELSE 0 END), 0) AS totalExpenditure,
               COUNT(*) AS entryCount
        FROM transactions
        WHERE timestamp >= :fromTimestamp
    """
    )
    fun getMonthSummary(fromTimestamp: Long): Flow<MonthSummary>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query(
        """
        SELECT category, SUM(amount) AS total
        FROM transactions
        WHERE timestamp >= :fromTimestamp AND type = 'DEBIT'
        GROUP BY category
        ORDER BY total DESC
    """
    )
    fun getCategoryBreakdown(fromTimestamp: Long): Flow<List<CategorySpend>>

    @Query("UPDATE transactions SET category = :category WHERE LOWER(merchant) = LOWER(:keyword)")
    suspend fun recategorizeExact(keyword: String, category: TransactionCategory)

    @Query("SELECT * FROM transactions WHERE LOWER(merchant) = LOWER(:keyword)")
    suspend fun getByMerchantExact(keyword: String): List<Transaction>

    @Query("UPDATE transactions SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: TransactionCategory)

    @Update
    suspend fun update(transaction: Transaction)
}
