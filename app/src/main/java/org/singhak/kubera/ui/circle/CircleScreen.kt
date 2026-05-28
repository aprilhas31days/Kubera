package org.singhak.kubera.ui.circle

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import org.singhak.kubera.db.Person
import org.singhak.kubera.db.PersonIdentifier
import org.singhak.kubera.model.PersonSummary
import org.singhak.kubera.ui.theme.Credit

@Suppress("LongMethod")
@Composable
fun CircleScreen(
    modifier: Modifier = Modifier,
    viewModel: CircleViewModel = hiltViewModel(),
) {
    val summaries by viewModel.personSummaries.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var addIdentifierTarget by remember { mutableStateOf<Person?>(null) }

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
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "MY CIRCLE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-1.5).sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeriodFilter.entries.forEach { filter ->
                        val selected = filter == selectedFilter
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = if (selected) MaterialTheme.colorScheme.background
                                    else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }

        if (summaries.isEmpty()) {
            item {
                Text(
                    text = "No one in your circle yet. Add a person to start tracking.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
        } else {
            items(summaries, key = { it.person.id }) { summary ->
                PersonSection(
                    summary = summary,
                    onDeletePerson = { viewModel.deletePerson(summary.person) },
                    onDeleteIdentifier = { viewModel.deleteIdentifier(it) },
                    onAddIdentifier = { addIdentifierTarget = summary.person },
                )
            }
        }

        item {
            Text(
                text = "+ ADD PERSON",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { showAddPersonDialog = true }
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
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

@Suppress("LongMethod")
@Composable
private fun PersonSection(
    summary: PersonSummary,
    onDeletePerson: () -> Unit,
    onDeleteIdentifier: (PersonIdentifier) -> Unit,
    onAddIdentifier: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = summary.person.name.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "×",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .clickable { onDeletePerson() }
                    .padding(8.dp),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AmountsRow(summary = summary)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        summary.identifiers.forEach { identifier ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = identifier.identifier,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "×",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clickable { onDeleteIdentifier(identifier) }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }

        Text(
            text = "+ ADD IDENTIFIER",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .clickable { onAddIdentifier() }
                .padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun AmountsRow(summary: PersonSummary) {
    val netColor = if (summary.net >= 0) Credit else MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AmountLine(label = "SENT", amount = summary.sent, color = MaterialTheme.colorScheme.primary)
        AmountLine(label = "RECEIVED", amount = summary.received, color = Credit)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
        )
        AmountLine(
            label = "NET",
            amount = summary.net,
            color = netColor,
            signed = true,
        )
    }
}

@Composable
private fun AmountLine(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    signed: Boolean = false,
) {
    val amountText = if (signed && amount > 0) {
        "+ ₹${"%, .2f".format(amount)}"
    } else {
        "₹${"%, .2f".format(amount)}"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = amountText,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
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
                    label = { Text("UPI ID / NAME", style = MaterialTheme.typography.labelSmall) },
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
