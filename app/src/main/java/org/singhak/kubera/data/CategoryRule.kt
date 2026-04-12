package org.singhak.kubera.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.singhak.kubera.model.TransactionCategory

enum class RuleSource { SYSTEM, USER }

@Entity(tableName = "category_rules")
data class CategoryRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val category: TransactionCategory,
    val source: RuleSource,
)
