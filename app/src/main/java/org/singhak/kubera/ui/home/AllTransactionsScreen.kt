package org.singhak.kubera.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType

@Suppress("LongMethod")
@Composable
fun AllTransactionsScreen(
    onBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    viewModel: AllTransactionsViewModel = hiltViewModel(),
) {
    BackHandler { onBack() }

    val filter by viewModel.filter.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val grouped = remember(transactions) { groupTransactionsByDate(transactions) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "ALL TRANSACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "← BACK",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            )
        }

        // Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = viewModel::updateQuery,
                placeholder = {
                    Text(
                        "SEARCH MERCHANTS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            FilterRow(label = "TYPE") {
                FilterChip("ALL", filter.type == null) { viewModel.updateType(null) }
                TransactionType.entries.forEach { t ->
                    FilterChip(t.name, filter.type == t) { viewModel.updateType(t) }
                }
            }

            FilterRow(label = "CHANNEL") {
                FilterChip("ALL", filter.channel == null) { viewModel.updateChannel(null) }
                TransactionChannel.entries.forEach { c ->
                    FilterChip(
                        c.displayName.uppercase(Locale.getDefault()),
                        filter.channel == c,
                    ) { viewModel.updateChannel(c) }
                }
            }

            FilterRow(label = "CATEGORY") {
                FilterChip("ALL", filter.category == null) { viewModel.updateCategory(null) }
                TransactionCategory.entries.forEach { cat ->
                    FilterChip(
                        cat.displayName.uppercase(Locale.getDefault()),
                        filter.category == cat,
                    ) { viewModel.updateCategory(cat) }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DateFilterChip(
                    label = "FROM",
                    date = filter.fromDate,
                    dateFormat = dateFormat,
                    onClick = { showFromPicker = true },
                    onClear = { viewModel.updateFromDate(null) },
                    modifier = Modifier.weight(1f),
                )
                DateFilterChip(
                    label = "TO",
                    date = filter.toDate,
                    dateFormat = dateFormat,
                    onClick = { showToPicker = true },
                    onClear = { viewModel.updateToDate(null) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result count row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${transactions.size} TRANSACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.outline,
            )
            if (filter.isActive) {
                Text(
                    text = "CLEAR FILTERS",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .clickable { viewModel.clearFilters() }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                )
            }
        }

        @Suppress("MagicNumber")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .padding(horizontal = 24.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        )

        // Transaction list
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "NO TRANSACTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }

            grouped.forEach { (dateLabel, dayTransactions) ->
                val dailyTotal = dayTransactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }
                item { DateSectionHeader(label = dateLabel, dailyTotal = dailyTotal) }
                items(dayTransactions, key = { it.hashCode() }) { txn ->
                    TransactionItem(transaction = txn, onClick = { onTransactionClick(txn) })
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }

    if (showFromPicker) {
        val cal = Calendar.getInstance().apply { filter.fromDate?.let { timeInMillis = it } }
        DatePickerDialog(
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH) + 1,
            initialDay = cal.get(Calendar.DAY_OF_MONTH),
            onDismiss = { showFromPicker = false },
            onConfirm = { millis -> showFromPicker = false; viewModel.updateFromDate(millis) },
        )
    }

    if (showToPicker) {
        val cal = Calendar.getInstance().apply { filter.toDate?.let { timeInMillis = it } }
        DatePickerDialog(
            initialYear = cal.get(Calendar.YEAR),
            initialMonth = cal.get(Calendar.MONTH) + 1,
            initialDay = cal.get(Calendar.DAY_OF_MONTH),
            onDismiss = { showToPicker = false },
            onConfirm = { millis -> showToPicker = false; viewModel.updateToDate(millis) },
        )
    }
}

@Composable
private fun FilterRow(label: String, chips: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(end = 4.dp),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            chips()
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
        color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
            )
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (selected) 0f else 0.4f),
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
    )
}

@Composable
private fun DateFilterChip(
    label: String,
    date: Long?,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontSize = 8.sp,
                ),
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = if (date != null) {
                    dateFormat.format(java.util.Date(date)).uppercase(Locale.getDefault())
                } else {
                    "ANY"
                },
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = if (date != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
        }
        if (date != null) {
            Text(
                text = "×",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .clickable(onClick = onClear)
                    .padding(start = 8.dp),
            )
        }
    }
}
