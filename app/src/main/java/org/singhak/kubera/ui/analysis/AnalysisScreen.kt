package org.singhak.kubera.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MerchantSpend
import org.singhak.kubera.model.MonthSummary
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.home.DateRangePickerSheet
import org.singhak.kubera.ui.shared.FilterPillChip
import org.singhak.kubera.ui.shared.FilterSectionLabel
import org.singhak.kubera.ui.theme.GreenColor
import org.singhak.kubera.ui.theme.RedColor

@Suppress("LongMethod", "MagicNumber")
@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val filter by viewModel.filter.collectAsState()
    val monthSummary by viewModel.monthSummary.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val dailySpend by viewModel.dailySpend.collectAsState()
    val topMerchants by viewModel.topMerchants.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val isDefault = remember(filter) { filter.activeCount == 0 || isCurrentMonthDefault(filter) }

    val filterLabel = remember(filter) {
        val fmt = SimpleDateFormat("d MMM", Locale.getDefault())
        when {
            filter.activeCount == 0 -> "ALL TIME"
            isCurrentMonthDefault(filter) -> "THIS MONTH"
            filter.dateFrom != null || filter.dateTo != null -> {
                val from = filter.dateFrom?.let { fmt.format(Date(it)) } ?: "—"
                val to = filter.dateTo?.let { fmt.format(Date(it)) } ?: "—"
                "$from → $to"
            }
            else -> "${filter.activeCount} FILTER${if (filter.activeCount > 1) "S" else ""}"
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding(),
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Analytics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = filterLabel,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { showFilterSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (!isDefault) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp),
                            )
                            if (!isDefault) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = filter.activeCount.toString(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight(700),
                                        color = MaterialTheme.colorScheme.background,
                                        lineHeight = 8.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (monthSummary.entryCount == 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (filter.activeCount > 0) "No transactions match these filters" else "No transactions yet",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // Summary Cards
                item {
                    SummaryCards(
                        summary = monthSummary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Category Donut
                if (categoryBreakdown.isNotEmpty()) {
                    item {
                        CategorySection(
                            breakdown = categoryBreakdown,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Daily Spend Bar Chart
                item {
                    DailySpendSection(
                        dailyData = dailySpend,
                        endTimestamp = filter.dateTo ?: System.currentTimeMillis(),
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Top Merchants
                if (topMerchants.isNotEmpty()) {
                    item {
                        TopMerchantsSection(
                            merchants = topMerchants,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        if (showFilterSheet) {
            FilterSheet(
                currentFilter = filter,
                onDismiss = { showFilterSheet = false },
                onApply = { newFilter ->
                    viewModel.applyFilter(newFilter)
                    showFilterSheet = false
                },
            )
        }
    }
}

// ── Summary ──────────────────────────────────────────────────────────────────

@Suppress("MagicNumber")
@Composable
private fun SummaryCards(summary: MonthSummary, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SummaryCard(label = "SPENT", amount = summary.totalExpenditure, borderColor = RedColor, modifier = Modifier.weight(1f))
        SummaryCard(label = "CREDITED", amount = summary.totalCredited, borderColor = GreenColor, modifier = Modifier.weight(1f))
    }
}

@Suppress("MagicNumber")
@Composable
private fun SummaryCard(label: String, amount: Double, borderColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(borderColor))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = formatAmount(amount),
            fontSize = 20.sp,
            fontWeight = FontWeight(600),
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Category ─────────────────────────────────────────────────────────────────

@Suppress("MagicNumber")
@Composable
private fun CategorySection(breakdown: List<CategorySpend>, modifier: Modifier = Modifier) {
    val totalSpent = remember(breakdown) { breakdown.sumOf { it.total }.coerceAtLeast(1.0) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "CATEGORIES", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DonutChart(breakdown = breakdown, totalSpent = totalSpent, modifier = Modifier.size(160.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Top) {
                breakdown.forEach { item -> CategoryLegendRow(item = item, totalSpent = totalSpent) }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun DonutChart(breakdown: List<CategorySpend>, totalSpent: Double, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidthPx = 22.dp.toPx()
            val radius = (size.minDimension / 2f) - strokeWidthPx / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            var startAngle = -90f
            breakdown.forEach { item ->
                val sweep = ((item.total / totalSpent) * 360f - 1f).toFloat().coerceAtLeast(0f)
                drawArc(
                    color = item.category.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                    topLeft = androidx.compose.ui.geometry.Offset(cx - radius, cy - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                )
                startAngle += sweep + 1f
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "TOTAL", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            Text(text = formatAmount(totalSpent), fontSize = 13.sp, fontWeight = FontWeight(600), color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun CategoryLegendRow(item: CategorySpend, totalSpent: Double) {
    val pct = remember(item.total, totalSpent) { ((item.total / totalSpent) * 100).toInt() }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(8.dp).background(item.category.color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = item.category.displayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "${formatAmount(item.total)} · $pct%", fontSize = 12.sp, fontWeight = FontWeight(600), color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

// ── Daily Spend ───────────────────────────────────────────────────────────────

@Suppress("MagicNumber")
@Composable
private fun DailySpendSection(
    dailyData: Map<String, Double>,
    endTimestamp: Long,
    modifier: Modifier = Modifier,
) {
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val labelFmt = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val todayStr = remember { dateFmt.format(Date()) }

    val last15 = remember(dailyData, endTimestamp) {
        (0..14).map { i ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = endTimestamp
                add(Calendar.DAY_OF_YEAR, i - 14)
            }
            val dayStr = dateFmt.format(cal.time)
            Triple(dayStr, dailyData[dayStr] ?: 0.0, cal.get(Calendar.DAY_OF_MONTH))
        }
    }
    val maxDayTotal = remember(last15) { last15.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0 }
    val rangeLabel = remember(endTimestamp) {
        val startCal = Calendar.getInstance().apply { timeInMillis = endTimestamp; add(Calendar.DAY_OF_YEAR, -14) }
        val endCal = Calendar.getInstance().apply { timeInMillis = endTimestamp }
        "${labelFmt.format(startCal.time)} – ${labelFmt.format(endCal.time)}"
    }

    val textPrimaryColor = MaterialTheme.colorScheme.onSurface
    val greenColor = GreenColor

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = rangeLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().height(80.dp), verticalAlignment = Alignment.Bottom) {
            last15.forEach { (dayStr, total, label) ->
                val isToday = dayStr == todayStr
                val fraction = if (total > 0) (total / maxDayTotal).toFloat().coerceIn(0f, 1f) else 0f
                val maxBarDp = 56.dp
                val minBarDp = 3.dp
                val barColor = when {
                    isToday -> textPrimaryColor
                    total > 0 -> Color(0xFF4a4a4a)
                    else -> Color(0xFF1e1e1e)
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val barHeight = if (total > 0) (maxBarDp * fraction).coerceAtLeast(minBarDp) else minBarDp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 1.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(barColor),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = label.toString(), fontSize = 8.sp, color = if (isToday) greenColor else MaterialTheme.colorScheme.outline, letterSpacing = 0.sp)
                }
            }
        }
    }
}

// ── Top Merchants ─────────────────────────────────────────────────────────────

@Suppress("MagicNumber")
@Composable
private fun TopMerchantsSection(merchants: List<MerchantSpend>, modifier: Modifier = Modifier) {
    val topFive = remember(merchants) { merchants.take(5) }
    val maxTotal = remember(topFive) { topFive.firstOrNull()?.total?.coerceAtLeast(1.0) ?: 1.0 }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "TOP MERCHANTS", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(16.dp))
        topFive.forEachIndexed { index, merchant ->
            MerchantRow(index = index + 1, merchant = merchant, maxTotal = maxTotal)
            if (index < topFive.lastIndex) Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun MerchantRow(index: Int, merchant: MerchantSpend, maxTotal: Double) {
    val fraction = remember(merchant.total, maxTotal) { (merchant.total / maxTotal).toFloat().coerceIn(0f, 1f) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = index.toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(20.dp))
            Text(text = merchant.merchant, fontSize = 13.sp, fontWeight = FontWeight(600), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = formatAmount(merchant.total), fontSize = 13.sp, fontWeight = FontWeight(600), color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFF1e1e1e))) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction).background(Color(0xFF4a4a4a)))
        }
    }
}

// ── Filter Sheet ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Suppress("LongMethod", "MagicNumber")
@Composable
private fun FilterSheet(
    currentFilter: AnalysisFilter,
    onDismiss: () -> Unit,
    onApply: (AnalysisFilter) -> Unit,
) {
    var draft by remember { mutableStateOf(currentFilter) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.88f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Filters", fontSize = 16.sp, fontWeight = FontWeight(600), color = MaterialTheme.colorScheme.onSurface)
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp).clickable { onDismiss() },
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Scrollable sections
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            ) {
                // Date Range
                item {
                    val dateFmt = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
                    val dateRangeText = when {
                        draft.dateFrom != null && draft.dateTo != null ->
                            "${dateFmt.format(Date(draft.dateFrom!!))} → ${dateFmt.format(Date(draft.dateTo!!))}"
                        draft.dateFrom != null -> "From ${dateFmt.format(Date(draft.dateFrom!!))}"
                        draft.dateTo != null -> "Until ${dateFmt.format(Date(draft.dateTo!!))}"
                        else -> null
                    }
                    FilterSectionLabel("DATE RANGE")
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                1.dp,
                                if (dateRangeText != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { showDateRangePicker = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = dateRangeText ?: "Select range",
                            fontSize = 13.sp,
                            fontWeight = if (dateRangeText != null) FontWeight(600) else FontWeight(400),
                            color = if (dateRangeText != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.outline,
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Debit / Credit
                item {
                    FilterSectionLabel("DEBIT / CREDIT")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowFilter.entries.forEach { ff ->
                            FilterPillChip(
                                label = ff.name.lowercase().replaceFirstChar { it.uppercase() },
                                selected = draft.flow == ff,
                                onClick ={ draft = draft.copy(flow = ff) },
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Category
                item {
                    FilterSectionLabel("CATEGORY")
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionCategory.entries.forEach { cat ->
                            FilterPillChip(
                                label = cat.displayName,
                                selected = cat in draft.categories,
                                dotColor = cat.color,
                                onClick ={
                                    draft = draft.copy(
                                        categories = if (cat in draft.categories) draft.categories - cat else draft.categories + cat,
                                    )
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Payment Type
                item {
                    FilterSectionLabel("PAYMENT TYPE")
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionChannel.entries.forEach { ch ->
                            FilterPillChip(
                                label = ch.displayName,
                                selected = ch in draft.channels,
                                onClick ={
                                    draft = draft.copy(
                                        channels = if (ch in draft.channels) draft.channels - ch else draft.channels + ch,
                                    )
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Bank
                item {
                    FilterSectionLabel("BANK")
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Bank.entries.forEach { bank ->
                            FilterPillChip(
                                label = bank.displayName,
                                selected = bank in draft.banks,
                                onClick ={
                                    draft = draft.copy(
                                        banks = if (bank in draft.banks) draft.banks - bank else draft.banks + bank,
                                    )
                                },
                            )
                        }
                    }
                }
            }

            // Footer
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { draft = AnalysisFilter() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Clear all", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                }
                val applyCount = draft.activeCount
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface)
                        .clickable { onApply(draft) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (applyCount > 0) "Apply ($applyCount)" else "Apply",
                        fontSize = 13.sp,
                        fontWeight = FontWeight(700),
                        color = MaterialTheme.colorScheme.background,
                    )
                }
            }
        }
    }

    if (showDateRangePicker) {
        DateRangePickerSheet(
            fromMillis = draft.dateFrom,
            toMillis = draft.dateTo,
            onDismiss = { showDateRangePicker = false },
            onApply = { from, to ->
                draft = draft.copy(dateFrom = from, dateTo = to)
                showDateRangePicker = false
            },
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun isCurrentMonthDefault(filter: AnalysisFilter): Boolean {
    if (filter.dateTo != null || filter.categories.isNotEmpty() ||
        filter.channels.isNotEmpty() || filter.banks.isNotEmpty() || filter.flow != FlowFilter.ALL
    ) return false
    val from = filter.dateFrom ?: return false
    val cal = Calendar.getInstance().apply { timeInMillis = from }
    val now = Calendar.getInstance()
    return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
        cal.get(Calendar.DAY_OF_MONTH) == 1
}

private fun formatAmount(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
    nf.maximumFractionDigits = 0
    nf.minimumFractionDigits = 0
    return "₹${nf.format(amount)}"
}
