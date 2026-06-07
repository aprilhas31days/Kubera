package org.singhak.kubera.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.shared.FilterPillChip
import org.singhak.kubera.ui.shared.FilterSectionLabel
import org.singhak.kubera.ui.theme.BorderColor
import org.singhak.kubera.ui.theme.GreenColor
import org.singhak.kubera.ui.theme.RedColor
import org.singhak.kubera.ui.theme.TextPrimary
import org.singhak.kubera.ui.theme.TextSecondary

@Suppress("LongMethod", "MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    onBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    viewModel: AllTransactionsViewModel = hiltViewModel(),
) {
    BackHandler { onBack() }

    val filter by viewModel.filter.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var sortByAmount by rememberSaveable { mutableStateOf(false) }
    var sortAscDate by rememberSaveable { mutableStateOf(false) }
    var sortAscAmount by rememberSaveable { mutableStateOf(false) }

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val sortedTransactions = remember(transactions, sortByAmount, sortAscDate, sortAscAmount) {
        if (sortByAmount) {
            if (sortAscAmount) transactions.sortedBy { it.amount }
            else transactions.sortedByDescending { it.amount }
        } else {
            if (sortAscDate) transactions.sortedBy { it.timestamp }
            else transactions.sortedByDescending { it.timestamp }
        }
    }

    val groupedByMonth = remember(sortedTransactions, sortByAmount) {
        if (sortByAmount) emptyList()
        else {
            sortedTransactions
                .groupBy { txn ->
                    if (txn.timestamp == 0L) "UNKNOWN"
                    else monthFormat.format(Date(txn.timestamp)).uppercase(Locale.getDefault())
                }
                .entries
                .toList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                )
            }
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                ),
                color = TextPrimary,
            )
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = if (filter.isActive) TextPrimary else TextSecondary,
                    )
                }
                if (filter.isActive) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(GreenColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = filter.activeCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White,
                        )
                    }
                }
            }
        }

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = filter.query,
                onValueChange = viewModel::updateQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = TextPrimary,
                    fontSize = 13.sp,
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (filter.query.isEmpty()) {
                        Text(
                            text = "Search transactions...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = TextSecondary,
                        )
                    }
                    inner()
                },
            )
            if (filter.query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Clear",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { viewModel.updateQuery("") },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sort buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val dateSortSelected = !sortByAmount
            val amountSortSelected = sortByAmount

            SortPillButton(
                label = "Date" + if (!sortByAmount) (if (sortAscDate) " ↑" else " ↓") else "",
                selected = dateSortSelected,
                onClick = {
                    if (!sortByAmount) sortAscDate = !sortAscDate
                    else sortByAmount = false
                },
            )
            SortPillButton(
                label = "Amount" + if (sortByAmount) (if (sortAscAmount) " ↑" else " ↓") else "",
                selected = amountSortSelected,
                onClick = {
                    if (sortByAmount) sortAscAmount = !sortAscAmount
                    else sortByAmount = true
                },
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction list
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (sortedTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = TextSecondary,
                        )
                    }
                }
            }

            if (sortByAmount) {
                items(sortedTransactions, key = { it.id }) { txn ->
                    AllTxnRow(
                        transaction = txn,
                        timeFormat = timeFormat,
                        showDate = true,
                        isLast = true,
                        onClick = { onTransactionClick(txn) },
                    )
                }
            } else {
                groupedByMonth.forEachIndexed { _, (monthLabel, monthTxns) ->
                    val monthDebitTotal = monthTxns
                        .filter { it.type == TransactionType.DEBIT }
                        .sumOf { it.amount }
                    item {
                        MonthHeader(label = monthLabel, debitTotal = monthDebitTotal)
                    }
                    itemsIndexed(monthTxns, key = { _, txn -> txn.id }) { index, txn ->
                        AllTxnRow(
                            transaction = txn,
                            timeFormat = timeFormat,
                            showDate = false,
                            isLast = index == monthTxns.lastIndex,
                            onClick = { onTransactionClick(txn) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filter = filter,
            onDismiss = { showFilterSheet = false },
            onShowDateRangePicker = { showDateRangePicker = true },
            onClearDateRange = { viewModel.updateFromDate(null); viewModel.updateToDate(null) },
            onToggleType = { viewModel.updateType(it) },
            onToggleChannel = { viewModel.toggleChannel(it) },
            onToggleCategory = { viewModel.toggleCategory(it) },
            onClear = { viewModel.clearFilters() },
            onApply = { showFilterSheet = false },
        )
    }

    if (showDateRangePicker) {
        DateRangePickerSheet(
            fromMillis = filter.fromDate,
            toMillis = filter.toDate,
            onDismiss = { showDateRangePicker = false },
            onApply = { from, to ->
                viewModel.updateFromDate(from)
                viewModel.updateToDate(to)
                showDateRangePicker = false
            },
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun SortPillButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) TextPrimary else BorderColor
    val textColor = if (selected) TextPrimary else TextSecondary
    val fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = fontWeight,
            ),
            color = textColor,
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun MonthHeader(label: String, debitTotal: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 3.sp,
            ),
            color = TextSecondary,
        )
        if (debitTotal > 0) {
            Text(
                text = "₹%,.0f".format(debitTotal),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = RedColor,
            )
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun AllTxnRow(
    transaction: Transaction,
    timeFormat: SimpleDateFormat,
    showDate: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val isCredit = transaction.type == TransactionType.CREDIT
    val amountColor = if (isCredit) GreenColor else RedColor
    val amountText = "₹%,.0f".format(transaction.amount)
    val timeText = if (transaction.timestamp != 0L) {
        if (showDate) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
        } else {
            timeFormat.format(Date(transaction.timestamp))
        }
    } else {
        ""
    }
    val subtitle = buildString {
        append(transaction.category.displayName)
        append(" · ")
        append(transaction.type.name.lowercase().replaceFirstChar { it.uppercase() })
        append(" · ")
        append(transaction.bank.displayName)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchant ?: transaction.channel.displayName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = amountColor,
                )
                if (timeText.isNotEmpty()) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary,
                    )
                }
            }
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Suppress("LongMethod", "MagicNumber")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    filter: TransactionFilter,
    onDismiss: () -> Unit,
    onShowDateRangePicker: () -> Unit,
    onClearDateRange: () -> Unit,
    onToggleType: (TransactionType?) -> Unit,
    onToggleChannel: (TransactionChannel) -> Unit,
    onToggleCategory: (TransactionCategory) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // DATE RANGE
            FilterSectionLabel("DATE RANGE")
            val dateRangeText = when {
                filter.fromDate != null && filter.toDate != null ->
                    "${dateFormat.format(Date(filter.fromDate))} → ${dateFormat.format(Date(filter.toDate))}"
                filter.fromDate != null -> "From ${dateFormat.format(Date(filter.fromDate))}"
                filter.toDate != null -> "Until ${dateFormat.format(Date(filter.toDate))}"
                else -> null
            }
            DateChipButton(
                label = dateRangeText ?: "Select range",
                hasValue = dateRangeText != null,
                onTap = onShowDateRangePicker,
                onClear = onClearDateRange,
            )

            // TYPE
            FilterSectionLabel("DEBIT / CREDIT")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPillChip(
                    label = "All",
                    selected = filter.type == null,
                    onClick = { onToggleType(null) },
                )
                FilterPillChip(
                    label = "Debit",
                    selected = filter.type == TransactionType.DEBIT,
                    onClick = { onToggleType(TransactionType.DEBIT) },
                )
                FilterPillChip(
                    label = "Credit",
                    selected = filter.type == TransactionType.CREDIT,
                    onClick = { onToggleType(TransactionType.CREDIT) },
                )
            }

            // CATEGORY
            FilterSectionLabel("CATEGORY")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionCategory.entries.forEach { cat ->
                    FilterPillChip(
                        label = cat.displayName,
                        selected = cat in filter.categories,
                        dotColor = cat.color,
                        onClick = { onToggleCategory(cat) },
                    )
                }
            }

            // PAYMENT TYPE
            FilterSectionLabel("PAYMENT TYPE")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionChannel.entries.forEach { ch ->
                    FilterPillChip(
                        label = ch.displayName,
                        selected = ch in filter.channels,
                        onClick = { onToggleChannel(ch) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClear(); onApply() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(TextPrimary, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onApply)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (filter.activeCount > 0) "Apply (${filter.activeCount})" else "Apply",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.background,
                    )
                }
            }
        }
    }
}


@Suppress("MagicNumber")
@Composable
private fun DateChipButton(
    label: String,
    hasValue: Boolean,
    onTap: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (hasValue) TextPrimary else BorderColor
    Row(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = if (hasValue) TextPrimary else TextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (hasValue) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = "Clear",
                tint = TextSecondary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onClear),
            )
        }
    }
}

