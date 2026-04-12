package org.singhak.kubera.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CategoryRuleDao {
    @Query("SELECT * FROM category_rules")
    suspend fun getAllRules(): List<CategoryRule>
}
