package org.singhak.kubera.db

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.singhak.kubera.db.RuleSource.SYSTEM
import org.singhak.kubera.db.RuleSource.USER
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionCategory.BILLS
import org.singhak.kubera.model.TransactionCategory.GROCERIES
import org.singhak.kubera.model.TransactionCategory.OTHER
import org.singhak.kubera.model.TransactionCategory.RIDES

class CategorizeTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `categorizes merchant correctly`(
        description: String,
        merchant: String?,
        rules: List<CategoryRule>,
        expected: TransactionCategory
    ) {
        assertEquals(expected, categorize(merchant, rules))
    }

    companion object {
        @JvmStatic
        fun cases() = listOf(
            Arguments.of("null merchant returns OTHER", null, emptyList<CategoryRule>(), OTHER),
            Arguments.of(
                "no matching rule returns OTHER",
                "BLINKIT",
                listOf(rule("amazon", GROCERIES, SYSTEM)),
                OTHER
            ),
            Arguments.of(
                "SYSTEM rule matches by substring",
                "UBER INDIA S",
                listOf(rule("uber", RIDES, SYSTEM)),
                RIDES
            ),
            Arguments.of(
                "USER rule matches exactly",
                "blinkit",
                listOf(rule("BLINKIT", GROCERIES, USER)),
                GROCERIES
            ),
            Arguments.of(
                "USER takes priority over SYSTEM",
                "uber",
                listOf(rule("uber", RIDES, USER), rule("uber", BILLS, SYSTEM)),
                RIDES
            )
        )

        private fun rule(keyword: String, category: TransactionCategory, source: RuleSource) =
            CategoryRule(keyword = keyword, category = category, source = source)
    }
}
