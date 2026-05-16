package org.singhak.kubera.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.theme.KuberaTheme

@Composable
fun HomeScreen(
    hasPermission: Boolean,
    monthSummary: MonthSummary,
    transactions: List<Transaction>?,
    categoryBreakdown: List<CategorySpend>,
    backfillState: BackfillState,
    onGrantAccess: () -> Unit,
    onBackfillFromDate: (Long) -> Unit,
    onManageRules: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> NoPermissionScreen(onGrantAccess = onGrantAccess)
            transactions == null -> Unit
            transactions.isEmpty() -> EmptyStateScreen(
                backfillState = backfillState,
                onLoadFromSms = { showDatePicker = true }
            )
            else -> TransactionList(
                monthSummary = monthSummary,
                categoryBreakdown = categoryBreakdown,
                transactions = transactions,
                onManageRules = onManageRules,
                onTransactionClick = onTransactionClick,
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismiss = { showDatePicker = false },
                onConfirm = { millis ->
                    showDatePicker = false
                    onBackfillFromDate(millis)
                }
            )
        }
    }
}

// region — Previews

@Suppress("MagicNumber", "UnusedPrivateMember")
@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
private fun TransactionListPreview() {
    val now = System.currentTimeMillis()
    val oneDay = 86_400_000L
    val transactions = listOf(
        Transaction(
            amount = 1299.00,
            type = TransactionType.DEBIT,
            channel = TransactionChannel.CREDIT_CARD,
            timestamp = now,
            bank = Bank.KOTAKB,
            merchant = "Apple Store"
        ),
        Transaction(
            amount = 265.00,
            type = TransactionType.DEBIT,
            channel = TransactionChannel.UPI,
            timestamp = now - 3_600_000,
            bank = Bank.INDBNK,
            merchant = "Equinox Holdings"
        ),
        Transaction(
            amount = 6.50,
            type = TransactionType.DEBIT,
            channel = TransactionChannel.UPI,
            timestamp = now - 7_200_000,
            bank = Bank.INDBNK,
            merchant = "Blue Bottle Coffee"
        ),
        Transaction(
            amount = 4200.00,
            type = TransactionType.DEBIT,
            channel = TransactionChannel.UPI,
            timestamp = now - oneDay,
            bank = Bank.INDBNK,
            merchant = "Aman Resorts"
        ),
        Transaction(
            amount = 15000.00,
            type = TransactionType.CREDIT,
            channel = TransactionChannel.NEFT,
            timestamp = now - oneDay * 3,
            bank = Bank.INDBNK,
            merchant = "Salary Credit"
        )
    )
    KuberaTheme {
        HomeScreen(
            hasPermission = true,
            monthSummary = MonthSummary(totalExpenditure = 5770.50, entryCount = 5),
            transactions = transactions,
            categoryBreakdown = listOf(
                CategorySpend(TransactionCategory.SHOPPING, 1299.00),
                CategorySpend(TransactionCategory.FOOD, 6.50),
                CategorySpend(TransactionCategory.TRAVEL, 4200.00),
                CategorySpend(TransactionCategory.OTHER, 265.00),
            ),
            backfillState = BackfillState.Idle,
            onGrantAccess = {},
            onBackfillFromDate = {},
            onManageRules = {},
            onTransactionClick = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
private fun EmptyStatePreview() {
    KuberaTheme {
        HomeScreen(
            hasPermission = true,
            monthSummary = MonthSummary(0.0, 0),
            transactions = emptyList(),
            categoryBreakdown = emptyList(),
            backfillState = BackfillState.NoResults,
            onGrantAccess = {},
            onBackfillFromDate = {},
            onManageRules = {},
            onTransactionClick = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
private fun NoPermissionPreview() {
    KuberaTheme {
        HomeScreen(
            hasPermission = false,
            monthSummary = MonthSummary(0.0, 0),
            transactions = emptyList(),
            categoryBreakdown = emptyList(),
            backfillState = BackfillState.Idle,
            onGrantAccess = {},
            onBackfillFromDate = {},
            onManageRules = {},
            onTransactionClick = {},
        )
    }
}

// endregion
