package org.singhak.kubera.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun EmptyStateScreen(backfillState: BackfillState, onLoadFromSms: () -> Unit) {
    val isLoading = backfillState is BackfillState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LEDGER\nEMPTY",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No transactions found. Synchronize your ledger by loading bank SMS messages. This process will index incoming financial data into your private journal.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (backfillState is BackfillState.NoResults) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO TRANSACTIONS FOUND IN THE SELECTED PERIOD.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = !isLoading, onClick = onLoadFromSms)
                .padding(horizontal = 40.dp, vertical = 20.dp)
        ) {
            Text(
                text = if (isLoading) "LOADING\u2026" else "LOAD FROM SMS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
