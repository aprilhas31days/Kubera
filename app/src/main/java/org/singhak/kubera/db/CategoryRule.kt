package org.singhak.kubera.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionCategory.OTHER

enum class RuleSource { SYSTEM, USER }

@Entity(tableName = "category_rules")
data class CategoryRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val category: TransactionCategory,
    val source: RuleSource
)

/**
 * Resolves the [TransactionCategory] for a given [merchant] name against [rules].
 *
 * @param merchant the merchant name extracted from the SMS (e.g. "BLINKIT", "UBER INDIA S")
 * @param rules the full list of category rules to match against
 *
 * @return the matched [TransactionCategory], or [TransactionCategory.OTHER] if nothing matches
 */
fun categorize(merchant: String?, rules: List<CategoryRule>): TransactionCategory {
    if (merchant == null) return OTHER
    return rules.firstOrNull { rule ->
        when (rule.source) {
            RuleSource.USER -> merchant.equals(rule.keyword, ignoreCase = true)
            RuleSource.SYSTEM -> merchant.contains(rule.keyword, ignoreCase = true)
        }
    }?.category ?: OTHER
}
