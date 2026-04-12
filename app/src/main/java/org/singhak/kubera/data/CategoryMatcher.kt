package org.singhak.kubera.data

import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionCategory.OTHER

/**
 * Resolves a [TransactionCategory] for a given merchant name.
 *
 * Resolution order:
 * 1. USER rules — exact match (case-insensitive), highest priority
 * 2. SYSTEM rules — substring match (case-insensitive), fallback
 * 3. [OTHER] if nothing matches
 */
fun categorize(merchant: String?, rules: List<CategoryRule>): TransactionCategory {
    if (merchant == null) return OTHER
    return rules.firstOrNull { it.source == RuleSource.USER && merchant.equals(it.keyword, ignoreCase = true) }?.category
        ?: rules.firstOrNull { it.source == RuleSource.SYSTEM && merchant.contains(it.keyword, ignoreCase = true) }?.category
        ?: OTHER
}
