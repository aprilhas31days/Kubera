package org.singhak.kubera.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.Transaction

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("SELECT * FROM transactions WHERE timestamp >= :fromTimestamp ORDER BY timestamp DESC")
    fun getTransactionsSince(fromTimestamp: Long): Flow<List<Transaction>>

    @Query("SELECT timestamp FROM transactions ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTimestamp(): Long?
}
