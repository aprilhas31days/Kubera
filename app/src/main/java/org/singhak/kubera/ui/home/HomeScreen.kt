package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.theme.KuberaTheme

@Composable
fun HomeScreen(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    val grouped = remember(transactions) { groupTransactionsByDate(transactions) }
    val totalExpenditure = remember(transactions) {
        transactions
            .filter { it.type == TransactionType.DEBIT }
            .sumOf { it.amount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        item { BalanceHeader(totalExpenditure = totalExpenditure) }

        // Transaction groups
        grouped.forEach { (dateLabel, dayTransactions) ->
            item { DateSectionHeader(label = dateLabel) }
            items(dayTransactions, key = { it.hashCode() }) { transaction ->
                TransactionItem(transaction = transaction)
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        // Footer
        item { LedgerFooter() }
    }
}

// region — Header

@Composable
private fun BalanceHeader(totalExpenditure: Double) {
    val monthLabel = remember {
        SimpleDateFormat("MMM", Locale.getDefault())
            .format(Date())
            .uppercase(Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 32.dp)
    ) {
        Text(
            text = "TOTAL EXPENDITURE \u00B7 $monthLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "\u20B9${"%, .2f".format(totalExpenditure)}",
            style = MaterialTheme.typography.displayMedium.copy(
                letterSpacing = (-1.5).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// endregion

// region — Date section

@Composable
private fun DateSectionHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.weight(1f))
        // Ghost divider at 20% opacity
        @Suppress("MagicNumber")
        Box(
            modifier = Modifier
                .weight(3f)
                .height(0.5.dp)
                .background(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
        )
    }
}

// endregion

// region — Transaction item

@Composable
private fun TransactionItem(transaction: Transaction) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = if (transaction.timestamp != 0L) {
        timeFormat.format(Date(transaction.timestamp))
    } else {
        ""
    }

    val dotColor = when (transaction.type) {
        TransactionType.DEBIT -> MaterialTheme.colorScheme.error
        TransactionType.CREDIT -> MaterialTheme.colorScheme.tertiary
    }

    val prefix = if (transaction.type == TransactionType.DEBIT) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Status orb
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = transaction.bank,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                if (timeText.isNotEmpty()) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        Text(
            text = "$prefix\u20B9${"%, .2f".format(transaction.amount)}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// endregion

// region — Footer

@Composable
private fun LedgerFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "END OF AUDIT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
        )
    }
}

// endregion

// region — Helpers

private fun groupTransactionsByDate(
    transactions: List<Transaction>
): List<Pair<String, List<Transaction>>> {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val todayStr = dateFormat.format(today.time)
    val yesterdayStr = dateFormat.format(yesterday.time)

    return transactions
        .sortedByDescending { it.timestamp }
        .groupBy { txn ->
            if (txn.timestamp == 0L) {
                "UNKNOWN"
            } else {
                dateFormat.format(Date(txn.timestamp))
            }
        }
        .map { (dateStr, txns) ->
            val label = when (dateStr) {
                todayStr -> "TODAY"
                yesterdayStr -> "YESTERDAY"
                else -> dateStr.uppercase(Locale.getDefault())
            }.uppercase(Locale.getDefault())
            label to txns
        }
}

// endregion

// region — Preview

@Suppress("MagicNumber", "UnusedPrivateMember")
@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
private fun HomeScreenPreview() {
    val now = System.currentTimeMillis()
    val oneDay = 86_400_000L

    KuberaTheme {
        HomeScreen(
            transactions = listOf(
                Transaction(
                    smsId = 1,
                    amount = 1299.00,
                    type = TransactionType.DEBIT,

                    timestamp = now,
                    bank = "Apple Store"
                ),
                Transaction(
                    smsId = 2,
                    amount = 265.00,
                    type = TransactionType.DEBIT,

                    timestamp = now - 3_600_000,
                    bank = "Equinox Holdings"
                ),
                Transaction(
                    smsId = 3,
                    amount = 6.50,
                    type = TransactionType.DEBIT,

                    timestamp = now - 7_200_000,
                    bank = "Blue Bottle Coffee"
                ),
                Transaction(
                    smsId = 4,
                    amount = 4200.00,
                    type = TransactionType.DEBIT,

                    timestamp = now - oneDay,
                    bank = "Aman Resorts"
                ),
                Transaction(
                    smsId = 5,
                    amount = 88.40,
                    type = TransactionType.DEBIT,

                    timestamp = now - oneDay - 3_600_000,
                    bank = "Shell Petrol"
                ),
                Transaction(
                    smsId = 6,
                    amount = 15000.00,
                    type = TransactionType.CREDIT,

                    timestamp = now - oneDay * 3,
                    bank = "Salary Credit"
                )
            )
        )
    }
}

// endregion
