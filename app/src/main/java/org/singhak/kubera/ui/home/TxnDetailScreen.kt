package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
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
import java.util.Date
import java.util.Locale
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.theme.GreenColor
import org.singhak.kubera.ui.theme.RedColor

@Suppress("LongMethod", "MagicNumber")
@Composable
fun TxnDetailScreen(
    transaction: Transaction,
    onBack: () -> Unit,
    onEdit: (Transaction) -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val isCredit = transaction.type == TransactionType.CREDIT
    val amountColor = if (isCredit) GreenColor else RedColor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() },
                )
                Text(
                    text = transaction.merchant ?: transaction.channel.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(600),
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "Edit",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .clickable { onEdit(transaction) }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
            )
        }

        // Scrollable detail card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp),
                    ),
            ) {
                // Amount
                DetailRow(
                    label = "Amount",
                    value = "₹${"%,.0f".format(transaction.amount)}",
                    valueColor = amountColor,
                    showDivider = true,
                )

                // Type
                DetailRow(
                    label = "Type",
                    value = if (isCredit) "Credit" else "Debit",
                    showDivider = true,
                )

                // Category (with color dot)
                DetailRowWithDot(
                    label = "Category",
                    value = transaction.category.displayName,
                    dotColor = transaction.category.color,
                    showDivider = true,
                )

                // Payment
                DetailRow(
                    label = "Payment",
                    value = transaction.channel.displayName,
                    showDivider = true,
                )

                // Bank
                DetailRow(
                    label = "Bank",
                    value = transaction.bank.displayName,
                    showDivider = true,
                )

                // Date
                DetailRow(
                    label = "Date",
                    value = if (transaction.timestamp != 0L) {
                        dateFormat.format(Date(transaction.timestamp))
                    } else {
                        "—"
                    },
                    showDivider = true,
                )

                // Time
                DetailRow(
                    label = "Time",
                    value = if (transaction.timestamp != 0L) {
                        timeFormat.format(Date(transaction.timestamp))
                    } else {
                        "—"
                    },
                    showDivider = false,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    showDivider: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight(600),
            ),
            color = valueColor,
        )
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

@Composable
private fun DetailRowWithDot(
    label: String,
    value: String,
    dotColor: androidx.compose.ui.graphics.Color,
    showDivider: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.outline,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(600),
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}
