package org.singhak.kubera.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryRuleDao {
    @Query("SELECT * FROM category_rules")
    suspend fun getAllRules(): List<CategoryRule>

    @Query("SELECT * FROM category_rules WHERE source = 'USER' ORDER BY keyword ASC")
    fun getUserRules(): Flow<List<CategoryRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: CategoryRule): Long

    @Delete
    suspend fun delete(rule: CategoryRule)
}
