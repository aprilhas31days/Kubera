package org.singhak.kubera.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.singhak.kubera.model.Autopay

@Dao
interface AutopayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(autopay: Autopay)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(autopays: List<Autopay>)

    @Query("SELECT * FROM autopays ORDER BY nextDueDate ASC")
    fun getAll(): Flow<List<Autopay>>

    @Query("DELETE FROM autopays WHERE merchant = :merchant")
    suspend fun deleteByMerchant(merchant: String)
}
