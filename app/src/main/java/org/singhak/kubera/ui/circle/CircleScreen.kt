package org.singhak.kubera.ui.circle

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.singhak.kubera.db.Person
import org.singhak.kubera.db.PersonIdentifier
import org.singhak.kubera.model.PersonSummary
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.home.TxnRow
import org.singhak.kubera.ui.theme.GreenColor
import org.singhak.kubera.ui.theme.RedColor

@Suppress("LongMethod")
@Composable
fun CircleScreen(
    onTransactionClick: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CircleViewModel = hiltViewModel(),
) {
    val summaries by viewModel.personSummaries.collectAsState()
    var selected by remember { mutableStateOf<PersonSummary?>(null) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var addIdentifierTarget by remember { mutableStateOf<Person?>(null) }

    if (selected != null) {
        val summary = summaries.find { it.person.id == selected!!.person.id } ?: selected!!
        CircleDetailScreen(
            summary = summary,
            onBack = { selected = null },
            onTransactionClick = onTransactionClick,
            onAddIdentifier = { addIdentifierTarget = summary.person },
            onDeleteIdentifier = { viewModel.deleteIdentifier(it) },
        )
    } else {
        CircleListScreen(
            summaries = summaries,
            onSelectSummary = { selected = it },
            onAddPerson = { showAddPersonDialog = true },
            modifier = modifier,
        )
    }

    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onConfirm = { name ->
                showAddPersonDialog = false
                viewModel.addPerson(name)
            },
        )
    }

    addIdentifierTarget?.let { person ->
        AddIdentifierDialog(
            personName = person.name,
            onDismiss = { addIdentifierTarget = null },
            onConfirm = { identifier ->
                addIdentifierTarget = null
                viewModel.addIdentifier(person.id, identifier)
            },
        )
    }
}

@Composable
private fun CircleListScreen(
    summaries: List<PersonSummary>,
    onSelectSummary: (PersonSummary) -> Unit,
    onAddPerson: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        item {
            Text(
                text = "Circles",
                fontSize = 16.sp,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            )
        }

        if (summaries.isEmpty()) {
            item {
                Text(
                    text = "No one in your circle yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
        } else {
            items(summaries, key = { it.person.id }) { summary ->
                val lastTxnDate = summary.transactions.firstOrNull()?.let {
                    dateFormat.format(Date(it.timestamp))
                }
                val netColor = if (summary.net >= 0) GreenColor else RedColor
                val netText = "₹${"%, .0f".format(kotlin.math.abs(summary.net))}"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSummary(summary) }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = summary.person.name.first().uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight(600),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.person.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight(600),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(3.dp))
                        val idCount = summary.identifiers.size
                        val idLabel = if (idCount == 1) "UPI ID" else "UPI IDs"
                        val subtitle = "$idCount $idLabel" + if (lastTxnDate != null) " · Last $lastTxnDate" else ""
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = netText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight(700),
                            color = netColor,
                        )
                        Text(
                            text = "›",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                )
            }
        }

        item {
            Text(
                text = "+ ADD PERSON",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onAddPerson() }
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Suppress("LongMethod")
@Composable
private fun CircleDetailScreen(
    summary: PersonSummary,
    onBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAddIdentifier: () -> Unit,
    onDeleteIdentifier: (PersonIdentifier) -> Unit,
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val grouped = remember(summary.transactions) {
        summary.transactions.groupBy { monthFormat.format(Date(it.timestamp)) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "‹",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onBack() }.padding(4.dp),
                )
                Text(
                    text = summary.person.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                listOf(
                    Triple("YOU PAID", summary.sent, RedColor),
                    Triple("RECEIVED", summary.received, GreenColor),
                ).forEach { (label, amount, color) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(color),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "₹${"%, .0f".format(amount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight(600),
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        item {
            val netColor = if (summary.net >= 0) GreenColor else RedColor
            val netText = "₹${"%, .0f".format(kotlin.math.abs(summary.net))}"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "NET",
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
                Text(
                    text = netText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(700),
                    letterSpacing = (-0.5).sp,
                    color = netColor,
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "LINKED UPI IDs",
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    color = MaterialTheme.colorScheme.outline,
                )
                summary.identifiers.forEach { id ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = id.identifier,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "×",
                            fontSize = 14.sp,
                            color = RedColor,
                            modifier = Modifier
                                .clickable { onDeleteIdentifier(id) }
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                        )
                    }
                }
                Text(
                    text = "+ ADD IDENTIFIER",
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clickable { onAddIdentifier() }
                        .padding(vertical = 6.dp),
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            )
        }

        if (summary.transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions linked to this circle yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(24.dp),
                )
            }
        } else {
            grouped.forEach { (month, txns) ->
                item(key = month) {
                    Text(
                        text = month.uppercase(Locale.getDefault()),
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp),
                    )
                }
                items(txns, key = { it.id }) { txn ->
                    TxnRow(
                        transaction = txn,
                        onPress = { onTransactionClick(txn) },
                    )
                }
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun AddPersonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD PERSON",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            )
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("NAME", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text("ADD", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp))
            }
        },
    )
}

@Composable
private fun AddIdentifierDialog(
    personName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var identifier by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD IDENTIFIER · ${personName.uppercase(Locale.getDefault())}",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter a UPI ID or merchant name to associate with this person.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                androidx.compose.material3.OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("IDENTIFIER", style = MaterialTheme.typography.labelSmall) },
                    placeholder = {
                        Text(
                            "e.g. name@okaxis or merchant name",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (identifier.isNotBlank()) onConfirm(identifier) },
                enabled = identifier.isNotBlank(),
            ) {
                Text("ADD", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp))
            }
        },
    )
}
