package org.singhak.kubera.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.singhak.kubera.db.Person
import org.singhak.kubera.model.Bank
import org.singhak.kubera.model.Transaction
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.TransactionChannel
import org.singhak.kubera.model.TransactionType
import org.singhak.kubera.ui.circle.CircleViewModel

@Suppress("LongMethod")
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    onBack: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel(),
    circleViewModel: CircleViewModel = hiltViewModel(),
) {
    BackHandler { onBack() }

    LaunchedEffect(transaction.id) { viewModel.load(transaction) }

    val saveResult by viewModel.saveResult.collectAsState()
    val circlePeople by circleViewModel.people.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showRememberDialog by remember { mutableStateOf(false) }
    var showAddToCircleDialog by remember { mutableStateOf(false) }
    var showNewPersonDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saveResult) {
        if (saveResult is SaveResult.Success) onBack()
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timestampDate = remember(viewModel.timestamp) { Date(viewModel.timestamp) }
    val timestampCal = remember(viewModel.timestamp) {
        Calendar.getInstance().apply { timeInMillis = viewModel.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        ScreenHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            EditField(label = "MERCHANT") {
                OutlinedTextField(
                    value = viewModel.merchant,
                    onValueChange = { viewModel.merchant = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            EditField(label = "AMOUNT") {
                OutlinedTextField(
                    value = viewModel.amount,
                    onValueChange = { viewModel.amount = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            EditField(label = "TYPE") {
                ChipSelector(
                    options = TransactionType.entries,
                    selected = viewModel.type,
                    label = { it.name },
                    onSelect = { viewModel.type = it },
                )
            }

            EditField(label = "BANK") {
                ChipSelector(
                    options = Bank.entries,
                    selected = viewModel.bank,
                    label = { it.displayName.uppercase(Locale.getDefault()) },
                    onSelect = { viewModel.bank = it },
                )
            }

            EditField(label = "CHANNEL") {
                ChipSelector(
                    options = TransactionChannel.entries,
                    selected = viewModel.channel,
                    label = { it.displayName.uppercase(Locale.getDefault()) },
                    onSelect = { viewModel.channel = it },
                )
            }

            EditField(label = "ACCOUNT · LAST 4") {
                OutlinedTextField(
                    value = viewModel.account,
                    onValueChange = { new ->
                        val digits = new.filter { it.isDigit() }
                        if (digits.length <= 4) viewModel.account = digits
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            EditField(label = "DATE") {
                PickerField(
                    value = dateFormat.format(timestampDate).uppercase(Locale.getDefault()),
                    onClick = { showDatePicker = true },
                )
            }

            EditField(label = "TIME") {
                PickerField(
                    value = timeFormat.format(timestampDate),
                    onClick = { showTimePicker = true },
                )
            }

            EditField(label = "CATEGORY") {
                CategoryGrid(
                    selected = viewModel.category,
                    onSelect = { viewModel.category = it },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (saveResult is SaveResult.DuplicateError) {
            Text(
                text = "A transaction with these details already exists.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    if (viewModel.merchant.isNotBlank() && viewModel.categoryChanged) {
                        showRememberDialog = true
                    } else {
                        viewModel.save(false)
                    }
                }
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "SAVE TRANSACTION",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        if (viewModel.merchant.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
                    .clickable { showAddToCircleDialog = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ADD TO CIRCLE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialYear = timestampCal.get(Calendar.YEAR),
            initialMonth = timestampCal.get(Calendar.MONTH) + 1,
            initialDay = timestampCal.get(Calendar.DAY_OF_MONTH),
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                showDatePicker = false
                viewModel.updateDate(millis)
            },
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = timestampCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = timestampCal.get(Calendar.MINUTE),
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                showTimePicker = false
                viewModel.updateTime(hour, minute)
            },
        )
    }

    if (showAddToCircleDialog) {
        AddToCircleDialog(
            merchant = viewModel.merchant,
            people = circlePeople,
            onDismiss = { showAddToCircleDialog = false },
            onAddToExisting = { person ->
                showAddToCircleDialog = false
                circleViewModel.addIdentifier(person.id, viewModel.merchant)
            },
            onNewPerson = {
                showAddToCircleDialog = false
                showNewPersonDialog = true
            },
        )
    }

    if (showNewPersonDialog) {
        NewPersonForCircleDialog(
            merchant = viewModel.merchant,
            onDismiss = { showNewPersonDialog = false },
            onConfirm = { name ->
                showNewPersonDialog = false
                circleViewModel.addPersonWithIdentifier(name, viewModel.merchant)
            },
        )
    }

    if (showRememberDialog) {
        AlertDialog(
            onDismissRequest = { showRememberDialog = false },
            title = {
                Text(
                    text = "REMEMBER CHOICE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                )
            },
            text = {
                Text(
                    text = "Apply ${viewModel.category.displayName.uppercase(Locale.getDefault())} to all" +
                        " ${viewModel.merchant.trim()} transactions?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.outline,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRememberDialog = false
                    viewModel.save(true)
                }) {
                    Text(
                        text = "YES, REMEMBER",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRememberDialog = false
                    viewModel.save(false)
                }) {
                    Text(
                        text = "SKIP",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    )
                }
            },
        )
    }
}

@Composable
private fun ScreenHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "EDIT TRANSACTION",
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
}

@Composable
private fun EditField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        content()
    }
}

@Composable
private fun <T> ChipSelector(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            Text(
                text = label(option),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                    )
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = if (isSelected) 0f else 0.4f,
                        ),
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun CategoryGrid(selected: TransactionCategory, onSelect: (TransactionCategory) -> Unit) {
    val categories = remember { TransactionCategory.entries.toList() }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        categories.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { category ->
                    val isSelected = selected == category
                    Text(
                        text = category.displayName.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.background
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                            )
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = if (isSelected) 0f else 0.4f,
                                ),
                            )
                            .clickable { onSelect(category) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToCircleDialog(
    merchant: String,
    people: List<Person>,
    onDismiss: () -> Unit,
    onAddToExisting: (Person) -> Unit,
    onNewPerson: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ADD TO CIRCLE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = merchant.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                people.forEach { person ->
                    Text(
                        text = person.name.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddToExisting(person) }
                            .padding(vertical = 10.dp),
                    )
                }
                Text(
                    text = "+ NEW PERSON",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .clickable { onNewPerson() }
                        .padding(vertical = 10.dp),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp))
            }
        },
    )
}

@Composable
private fun NewPersonForCircleDialog(
    merchant: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "NEW PERSON",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${merchant.uppercase(Locale.getDefault())} will be added as an identifier.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("NAME", style = MaterialTheme.typography.labelSmall) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
private fun PickerField(value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        )
    }
}
