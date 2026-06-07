package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.theme.GreenColor

// ---------------------------------------------------------------------------
// Date grouping — also used by AllTransactionsScreen
// ---------------------------------------------------------------------------

internal fun groupTransactionsByDate(
    transactions: List<Transaction>,
): List<Pair<String, List<Transaction>>> {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val todayStr = dateFormat.format(today.time)
    val yesterdayStr = dateFormat.format(yesterday.time)

    return transactions
        .sortedByDescending { it.timestamp }
        .groupBy { txn ->
            if (txn.timestamp == 0L) "UNKNOWN" else dateFormat.format(Date(txn.timestamp))
        }
        .map { (dateStr, txns) ->
            val label = when (dateStr) {
                todayStr -> "TODAY"
                yesterdayStr -> "YESTERDAY"
                else -> dateStr.uppercase(Locale.getDefault())
            }
            label to txns
        }
}

// ---------------------------------------------------------------------------
// Date section header — used by AllTransactionsScreen
// ---------------------------------------------------------------------------

@Composable
internal fun DateSectionHeader(label: String, dailyTotal: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "₹${"%,.0f".format(dailyTotal)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            )
        }
        @Suppress("MagicNumber")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ---------------------------------------------------------------------------
// Transaction row — used by AllTransactionsScreen
// ---------------------------------------------------------------------------

@Composable
internal fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = if (transaction.timestamp != 0L) {
        timeFormat.format(Date(transaction.timestamp)).uppercase(Locale.getDefault())
    } else {
        ""
    }

    val isCredit = transaction.type == TransactionType.CREDIT
    val dotColor = if (isCredit) GreenColor else MaterialTheme.colorScheme.outline
    val amountColor = if (isCredit) GreenColor else MaterialTheme.colorScheme.primary
    val amountText = "₹${"%,.0f".format(transaction.amount)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(dotColor.copy(alpha = if (isCredit) 1f else 0.4f)),
            )
            Column(modifier = Modifier.padding(start = 24.dp)) {
                Text(
                    text = transaction.merchant ?: transaction.channel.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                val subtitleText = remember(
                    transaction.bank,
                    transaction.account,
                    transaction.channel,
                    transaction.category,
                ) {
                    val accountSuffix = transaction.account?.takeLast(4)?.let { " · $it" } ?: ""
                    val channelSuffix = " · ${transaction.channel.displayName.uppercase()}"
                    val categorySuffix = if (transaction.category != TransactionCategory.OTHER) {
                        " · ${transaction.category.displayName.uppercase()}"
                    } else {
                        ""
                    }
                    "${transaction.bank.displayName}$accountSuffix$channelSuffix$categorySuffix"
                }
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.5).sp,
                ),
                color = amountColor,
            )
            if (timeText.isNotEmpty()) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}
