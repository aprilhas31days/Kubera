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
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.theme.KuberaTheme

@Composable
fun HomeScreen(
    hasPermission: Boolean,
    monthSummary: MonthSummary,
    transactions: List<Transaction>?,
    backfillState: BackfillState,
    onGrantAccess: () -> Unit,
    onBackfillFromDate: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            !hasPermission -> NoPermissionScreen(onGrantAccess = onGrantAccess)
            transactions == null -> Unit
            transactions.isEmpty() -> EmptyStateScreen(
                backfillState = backfillState,
                onLoadFromSms = { showDatePicker = true },
            )
            else -> TransactionList(
                monthSummary = monthSummary,
                transactions = transactions,
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismiss = { showDatePicker = false },
                onConfirm = { millis ->
                    showDatePicker = false
                    onBackfillFromDate(millis)
                },
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
        Transaction(amount = 1299.00, type = TransactionType.DEBIT, timestamp = now, bank = "Apple Store"),
        Transaction(amount = 265.00, type = TransactionType.DEBIT, timestamp = now - 3_600_000, bank = "Equinox Holdings"),
        Transaction(amount = 6.50, type = TransactionType.DEBIT, timestamp = now - 7_200_000, bank = "Blue Bottle Coffee"),
        Transaction(amount = 4200.00, type = TransactionType.DEBIT, timestamp = now - oneDay, bank = "Aman Resorts"),
        Transaction(amount = 15000.00, type = TransactionType.CREDIT, timestamp = now - oneDay * 3, bank = "Salary Credit"),
    )
    KuberaTheme {
        HomeScreen(
            hasPermission = true,
            monthSummary = MonthSummary(totalExpenditure = 5770.50, entryCount = 5),
            transactions = transactions,
            backfillState = BackfillState.Idle,
            onGrantAccess = {},
            onBackfillFromDate = {},
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
            backfillState = BackfillState.NoResults,
            onGrantAccess = {},
            onBackfillFromDate = {},
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
            backfillState = BackfillState.Idle,
            onGrantAccess = {},
            onBackfillFromDate = {},
        )
    }
}

// endregion
