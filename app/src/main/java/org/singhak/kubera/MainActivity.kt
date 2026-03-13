package org.singhak.kubera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.parser.parseSms
import org.singhak.kubera.ui.theme.KuberaTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SENDER_INDIAN_BANK = "IN-INDBNK"

private val DEBIT_MESSAGES = listOf(
    SENDER_INDIAN_BANK to "A/c *5949 debited Rs. 1060.82 on 06-03-26 to JIO. UPI:643163976386. Not you? SMS BLOCK to 9289592895, Dial 1930 for Cyber Fraud - Indian Bank"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val transactions = DEBIT_MESSAGES.mapNotNull { (senderId, sms) ->
            parseSms(senderId, sms)
        }

        setContent {
            KuberaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TransactionList(
                        transactions = transactions,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionList(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionCard(transaction)
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction) {
    val amountColor = if (transaction.type == TransactionType.CREDIT) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
    }
    val amountPrefix = if (transaction.type == TransactionType.CREDIT) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.type.name,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "A/c ${transaction.accountNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = transaction.bank,
                    style = MaterialTheme.typography.bodySmall
                )
                if (transaction.timestamp != 0L) {
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(transaction.timestamp)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "$amountPrefix ₹${"%.2f".format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionListPreview() {
    KuberaTheme {
        TransactionList(
            transactions = listOf(
                Transaction(
                    amount = 1060.82,
                    type = TransactionType.DEBIT,
                    accountNumber = "*5949",
                    timestamp = System.currentTimeMillis(),
                    bank = "Indian Bank"
                )
            )
        )
    }
}
