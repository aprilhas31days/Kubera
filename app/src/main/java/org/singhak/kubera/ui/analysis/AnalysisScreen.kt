package org.singhak.kubera.ui.analysis

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import org.singhak.kubera.model.CategorySpend
import org.singhak.kubera.model.MerchantSpend
import org.singhak.kubera.model.MonthlySpend

@Composable
fun AnalysisScreen(
    categoryBreakdown: List<CategorySpend>,
    monthlyTrend: List<MonthlySpend>,
    topMerchants: List<MerchantSpend>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 40.dp)
            ) {
                Text(
                    text = "ANALYSIS",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-1.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (categoryBreakdown.isNotEmpty()) {
            item { SectionHeader("THIS MONTH · BY CATEGORY") }
            item {
                CategoryBreakdownSection(
                    breakdown = categoryBreakdown,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }

        if (monthlyTrend.isNotEmpty()) {
            item { SectionHeader("6-MONTH TREND") }
            item {
                MonthlyTrendSection(
                    trend = monthlyTrend,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }

        if (topMerchants.isNotEmpty()) {
            item { SectionHeader("TOP MERCHANTS") }
            items(topMerchants) { merchant ->
                MerchantRow(
                    merchant = merchant,
                    maxTotal = topMerchants.first().total,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(6.dp))
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
private fun CategoryBreakdownSection(
    breakdown: List<CategorySpend>,
    modifier: Modifier = Modifier
) {
    val maxTotal = remember(breakdown) { breakdown.maxOf { it.total } }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        breakdown.forEach { item ->
            CategoryRow(item = item, fraction = (item.total / maxTotal).toFloat())
        }
    }
}

@Composable
private fun CategoryRow(item: CategorySpend, fraction: Float) {
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
private fun MonthlyTrendSection(
    trend: List<MonthlySpend>,
    modifier: Modifier = Modifier
) {
    val maxTotal = remember(trend) { trend.maxOf { it.total }.coerceAtLeast(1.0) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        trend.forEach { item ->
            MonthRow(item = item, fraction = (item.total / maxTotal).toFloat())
        }
    }
}

private const val MonthLabelWeight = 0.22f
private const val MonthAmountWeight = 0.38f

@Composable
private fun MonthRow(item: MonthlySpend, fraction: Float) {
    val label = remember(item.month) { item.month.toShortMonthLabel() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(MonthLabelWeight)
        )
        Column(modifier = Modifier.weight(1f)) {
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
        Text(
            text = "₹${"%, .2f".format(item.total)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(MonthAmountWeight)
        )
    }
}

@Composable
private fun MerchantRow(
    merchant: MerchantSpend,
    maxTotal: Double,
    modifier: Modifier = Modifier
) {
    val fraction = remember(merchant.total, maxTotal) {
        (merchant.total / maxTotal.coerceAtLeast(1.0)).toFloat()
    }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = merchant.merchant.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "₹${"%, .2f".format(merchant.total)}",
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

private fun String.toShortMonthLabel(): String {
    val parts = split("-")
    if (parts.size != 2) return this
    val year = parts[0].takeLast(2)
    val monthIndex = parts[1].toIntOrNull()?.minus(1)
    val names = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
    return if (monthIndex != null) "${names.getOrElse(monthIndex) { this }} $year" else this
}
