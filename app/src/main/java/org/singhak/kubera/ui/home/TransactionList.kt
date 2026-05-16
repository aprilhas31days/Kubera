package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.theme.Credit

@Composable
internal fun TransactionList(
    monthSummary: MonthSummary,
    categoryBreakdown: List<CategorySpend>,
    transactions: List<Transaction>,
    onManageRules: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val grouped = remember(transactions) { groupTransactionsByDate(transactions) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            BalanceHeader(
                totalExpenditure = monthSummary.totalExpenditure,
                entryCount = monthSummary.entryCount,
                onManageRules = onManageRules
            )
        }

        if (categoryBreakdown.isNotEmpty()) {
            item { CategoryBreakdownSection(breakdown = categoryBreakdown) }
        }

        grouped.forEach { (dateLabel, dayTransactions) ->
            val dailyTotal = dayTransactions
                .filter { it.type == TransactionType.DEBIT }
                .sumOf { it.amount }
            item { DateSectionHeader(label = dateLabel, dailyTotal = dailyTotal) }
            items(dayTransactions, key = { it.hashCode() }) { transaction ->
                TransactionItem(transaction = transaction, onClick = { onTransactionClick(transaction) })
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        item { LedgerFooter() }
    }
}

@Composable
private fun BalanceHeader(totalExpenditure: Double, entryCount: Int, onManageRules: () -> Unit) {
    val monthLabel = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Date())
            .uppercase(Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL EXPENDITURE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "RULES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .clickable { onManageRules() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "\u20B9${"%, .2f".format(totalExpenditure)}",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1.5).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "\u2022",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "$entryCount ENTRIES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun DateSectionHeader(label: String, dailyTotal: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "\u20B9${"%, .2f".format(dailyTotal)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        }
        @Suppress("MagicNumber")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = if (transaction.timestamp != 0L) {
        timeFormat.format(Date(transaction.timestamp)).uppercase(Locale.getDefault())
    } else {
        ""
    }

    val isCredit = transaction.type == TransactionType.CREDIT
    val dotColor = if (isCredit) Credit else MaterialTheme.colorScheme.outline
    val amountColor = if (isCredit) Credit else MaterialTheme.colorScheme.primary
    val amountText = if (isCredit) {
        "+ \u20B9${"%, .2f".format(transaction.amount)}"
    } else {
        "\u20B9${"%, .2f".format(transaction.amount)}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(dotColor.copy(alpha = if (isCredit) 1f else 0.4f))
            )
            Column(modifier = Modifier.padding(start = 24.dp)) {
                Text(
                    text = transaction.merchant ?: transaction.channel.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                val subtitleText = remember(transaction.bank, transaction.account, transaction.channel, transaction.category) {
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
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.5).sp
                ),
                color = amountColor
            )
            if (timeText.isNotEmpty()) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(breakdown: List<CategorySpend>) {
    val maxTotal = remember(breakdown) { breakdown.maxOf { it.total } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "BY CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(6.dp))
        @Suppress("MagicNumber")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(16.dp))

        breakdown.forEach { item ->
            CategoryBreakdownRow(item = item, fraction = (item.total / maxTotal).toFloat())
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryBreakdownRow(item: CategorySpend, fraction: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = item.category.displayName.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "₹${"%, .2f".format(item.total)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            )
        }
    }
}

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
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    }
}

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
