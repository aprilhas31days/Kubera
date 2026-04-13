package org.singhak.kubera.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction

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
}
