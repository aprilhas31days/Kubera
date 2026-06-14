package org.singhak.kubera.ui.autopay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import org.singhak.kubera.model.Autopay

private val accentColors = listOf(
    Color(0xFFec4899),
    Color(0xFF06b6d4),
    Color(0xFFf97316),
    Color(0xFF8b5cf6),
    Color(0xFF10b981),
    Color(0xFFf59e0b),
)

private fun merchantColor(merchant: String) = accentColors[abs(merchant.hashCode()) % accentColors.size]

private fun nextOccurrence(nextDueDate: Long): Long {
    var date = nextDueDate
    val now = System.currentTimeMillis()
    while (date < now) {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        cal.add(Calendar.MONTH, 1)
        date = cal.timeInMillis
    }
    return date
}

private fun dueDateLabel(nextDueDate: Long): String {
    val due = nextOccurrence(nextDueDate)
    val daysUntil = ((due - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
    return when {
        daysUntil == 0 -> "Today"
        daysUntil == 1 -> "Tomorrow"
        daysUntil <= 6 -> "in $daysUntil days"
        else -> SimpleDateFormat("d MMM", Locale.ENGLISH).format(Date(due))
    }
}

@Composable
fun AutopayScreen(
    modifier: Modifier = Modifier,
    viewModel: AutopayViewModel = hiltViewModel(),
) {
    val autopays by viewModel.autopays.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                Text(
                    text = "Autopay",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(600),
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recurring payments & subscriptions",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        if (autopays.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No autopays detected yet",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "Upcoming debit notifications from your bank will appear here.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = (12 * 1.7).sp,
                        ),
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        } else {
            item {
                Text(
                    text = "UPCOMING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                ) {
                    autopays.forEachIndexed { index, autopay ->
                        AutopayRow(autopay = autopay)
                        if (index < autopays.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun AutopayRow(autopay: Autopay) {
    val color = remember(autopay.merchant) { merchantColor(autopay.merchant) }
    val dateLabel = remember(autopay.nextDueDate) { dueDateLabel(autopay.nextDueDate) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.13f))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = autopay.merchant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(600),
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = autopay.bank.displayName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${"%,.0f".format(autopay.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(700),
                ),
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
