package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.theme.GreenColor
import org.singhak.kubera.ui.theme.RedColor

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private const val RECENT_LIMIT = 10

@Suppress("LongMethod", "MagicNumber")
@Composable
fun HomeScreen(
    onTransactionClick: (Transaction) -> Unit,
    onViewAll: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val year by viewModel.year.collectAsState()
    val month by viewModel.month.collectAsState()
    val isCurrentMonth by viewModel.isCurrentMonth.collectAsState()
    val monthSummary by viewModel.monthSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val backfillState by viewModel.backfillState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            transactions == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
            transactions!!.isEmpty() -> EmptyStateScreen(
                backfillState = backfillState,
                onLoadFromSms = { showDatePicker = true },
                onAddManually = onAddTransaction,
            )
            else -> {
                val txns = transactions!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    // Status bar spacer
                    item { Spacer(Modifier.statusBarsPadding()) }

                    // Header row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Hey, Anuj",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight(600),
                                    letterSpacing = (-0.5).sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous month",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { viewModel.prevMonth() },
                                )
                                Text(
                                    text = "${MONTHS[month].take(3).uppercase(Locale.getDefault())} $year",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        letterSpacing = 2.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next month",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .alpha(if (isCurrentMonth) 0.3f else 1f)
                                        .clickable(enabled = !isCurrentMonth) { viewModel.nextMonth() },
                                )
                                Spacer(Modifier.size(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onOpenSettings() },
                                )
                            }
                        }
                    }

                    // Total Expenditure
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "TOTAL EXPENDITURE",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 3.sp,
                                ),
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "₹${"%,.0f".format(monthSummary.totalExpenditure)}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight(300),
                                    letterSpacing = (-2).sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    // Credited / Debited row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Credited
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(GreenColor),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "↑ CREDITED",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 2.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "₹${"%,.0f".format(monthSummary.totalCredited)}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight(600),
                                        letterSpacing = (-0.5).sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            // Debited
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(RedColor),
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "↓ DEBITED",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 2.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "₹${"%,.0f".format(monthSummary.totalExpenditure)}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight(600),
                                        letterSpacing = (-0.5).sp,
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    // Divider
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    // Recent header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "RECENT",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 3.sp,
                                ),
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Text(
                                text = "Show all",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .clickable { onViewAll() }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                            )
                        }
                    }

                    // Transaction rows (up to RECENT_LIMIT)
                    if (txns.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No transactions this month",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    } else {
                        val recent = txns
                            .sortedByDescending { it.timestamp }
                            .take(RECENT_LIMIT)
                        items(recent, key = { it.id }) { txn ->
                            TxnRow(
                                transaction = txn,
                                onPress = { onTransactionClick(txn) },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }

        if (showDatePicker) {
            SingleDatePickerSheet(
                onDismiss = { showDatePicker = false },
                onConfirm = { millis ->
                    showDatePicker = false
                    viewModel.backfillFromDate(millis)
                },
            )
        }
    }
}

@Suppress("MagicNumber")
@Composable
internal fun TxnRow(
    transaction: Transaction,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormat = remember { SimpleDateFormat("d MMM · h:mm a", Locale.getDefault()) }
    val timeText = if (transaction.timestamp != 0L) {
        timeFormat.format(Date(transaction.timestamp))
    } else {
        ""
    }
    val isCredit = transaction.type == TransactionType.CREDIT
    val amountColor = if (isCredit) GreenColor else RedColor
    val amountText = "₹${"%,.0f".format(transaction.amount)}"
    val subtitle = buildString {
        append(transaction.category.displayName)
        append(" · ")
        append(if (isCredit) "Credit" else "Debit")
        append(" · ")
        append(transaction.bank.displayName)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPress() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Category color dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(transaction.category.color),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = transaction.merchant ?: transaction.channel.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(600),
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(700),
                ),
                color = amountColor,
            )
            if (timeText.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
