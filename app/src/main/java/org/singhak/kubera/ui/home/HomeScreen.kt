package org.singhak.kubera.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.theme.GreenColor
import org.singhak.kubera.ui.theme.RedColor

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private const val RECENT_LIMIT = 10

private fun Long.calendarDay(): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun dayLabel(millis: Long): String =
    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis)).uppercase(Locale.getDefault())

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

                val todayMidnight = remember {
                    Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }

                val recentGroups = remember(txns) {
                    val sorted = txns.sortedByDescending { it.timestamp }
                    val todayTxns = sorted.filter { it.timestamp.calendarDay() == todayMidnight }
                    if (todayTxns.isNotEmpty()) {
                        listOf("TODAY" to todayTxns)
                    } else {
                        sorted.take(RECENT_LIMIT)
                            .groupBy { it.timestamp.calendarDay() }
                            .entries
                            .sortedByDescending { it.key }
                            .map { (dayMs, dayTxns) -> dayLabel(dayMs) to dayTxns }
                    }
                }

                val uncatCount = remember(recentGroups) {
                    recentGroups.flatMap { it.second }.count { it.category == TransactionCategory.OTHER }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
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

                    // Recent header with "X to review" badge
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = recentGroups.firstOrNull()?.first ?: "RECENT",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        letterSpacing = 3.sp,
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                if (uncatCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RedColor.copy(alpha = 0.1f))
                                            .border(1.dp, RedColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = "$uncatCount to review",
                                            fontSize = 10.sp,
                                            color = RedColor,
                                        )
                                    }
                                }
                            }
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

                    if (recentGroups.isEmpty()) {
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
                        recentGroups.forEachIndexed { gi, (label, groupTxns) ->
                            // Date label for groups after the first (first label shown in header)
                            if (gi > 0) {
                                item(key = "label_$label") {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.sp,
                                            letterSpacing = 3.sp,
                                        ),
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(
                                            start = 20.dp,
                                            end = 20.dp,
                                            top = 16.dp,
                                            bottom = 8.dp,
                                        ),
                                    )
                                }
                            }
                            items(groupTxns, key = { it.id }) { txn ->
                                if (txn.category == TransactionCategory.OTHER) {
                                    UncatRow(
                                        transaction = txn,
                                        onCategorise = { cat, addRule ->
                                            viewModel.categorise(txn, cat, addRule)
                                        },
                                        onPress = { onTransactionClick(txn) },
                                    )
                                } else {
                                    TxnRow(
                                        transaction = txn,
                                        onPress = { onTransactionClick(txn) },
                                    )
                                }
                            }
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

@OptIn(ExperimentalLayoutApi::class)
@Suppress("LongMethod", "MagicNumber")
@Composable
private fun UncatRow(
    transaction: Transaction,
    onCategorise: (TransactionCategory, Boolean) -> Unit,
    onPress: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<TransactionCategory?>(null) }
    var stage by remember { mutableStateOf("pick") } // "pick" | "rule"

    val merchantName = transaction.merchant ?: transaction.channel.displayName
    val isCredit = transaction.type == TransactionType.CREDIT
    val amountColor = if (isCredit) GreenColor else RedColor
    val amountText = "₹${"%,.0f".format(transaction.amount)}"
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeText = if (transaction.timestamp != 0L) timeFormat.format(Date(transaction.timestamp)) else ""

    val categories = remember {
        TransactionCategory.entries.filter { it != TransactionCategory.OTHER } + TransactionCategory.OTHER
    }

    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RedColor.copy(alpha = 0.07f))
            .padding(horizontal = 20.dp),
    ) {
        // Main row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (stage == "pick") expanded = !expanded else onPress() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(
                    color = outlineColor,
                    radius = size.minDimension / 2 - 1.dp.toPx(),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)),
                    ),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = merchantName,
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
                    text = "${transaction.channel.displayName} · ${transaction.bank.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outline,
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

        // Stage 1: pick a category
        if (expanded && stage == "pick") {
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    categories.forEach { cat ->
                        val selected = chosen == cat
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    1.dp,
                                    if (selected) cat.color else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(20.dp),
                                )
                                .background(
                                    if (selected) cat.color.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surface,
                                )
                                .clickable { chosen = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(cat.color),
                            )
                            Text(
                                text = cat.displayName,
                                fontSize = 11.sp,
                                color = if (selected) cat.color else MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { expanded = false; chosen = null }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (chosen != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .clickable(enabled = chosen != null) { stage = "rule" }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (chosen != null) "Confirm as ${chosen!!.displayName}" else "Pick a category",
                            fontSize = 12.sp,
                            fontWeight = if (chosen != null) FontWeight(600) else FontWeight(400),
                            color = if (chosen != null) MaterialTheme.colorScheme.background
                                    else MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }

        // Stage 2: ask about adding a rule
        if (expanded && stage == "rule") {
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                Text(
                    text = "Always categorise $merchantName as ${chosen?.displayName}?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight(600),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = "This will add a rule in Settings. Future transactions will be auto-categorised.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                chosen?.let { onCategorise(it, false) }
                                expanded = false; stage = "pick"; chosen = null
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Just this once",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onSurface)
                            .clickable {
                                chosen?.let { onCategorise(it, true) }
                                expanded = false; stage = "pick"; chosen = null
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Yes, add rule",
                            fontSize = 12.sp,
                            fontWeight = FontWeight(600),
                            color = MaterialTheme.colorScheme.background,
                        )
                    }
                }
            }
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
