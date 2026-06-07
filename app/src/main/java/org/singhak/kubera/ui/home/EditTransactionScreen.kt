package org.singhak.kubera.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.circle.CircleViewModel
import org.singhak.kubera.ui.theme.BorderColor
import org.singhak.kubera.ui.theme.RedColor
import org.singhak.kubera.ui.theme.GreenColor

@Suppress("LongMethod")
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    onBack: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel(),
    circleViewModel: CircleViewModel = hiltViewModel(),
) {
    val isNew = transaction.id == 0L

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
            .statusBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ChevronLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() },
            )
            Text(
                text = if (isNew) "Add Transaction" else "Edit Transaction",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            EditField(label = "MERCHANT") {
                OutlinedTextField(
                    value = viewModel.merchant,
                    onValueChange = { viewModel.merchant = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            EditField(label = "AMOUNT") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionType.entries.forEach { t ->
                            val sel = viewModel.type == t
                            val borderCol = if (sel) {
                                if (t == TransactionType.DEBIT) RedColor else GreenColor
                            } else {
                                BorderColor
                            }
                            val textCol = if (sel) {
                                if (t == TransactionType.DEBIT) RedColor else GreenColor
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.type = t }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = t.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    fontWeight = if (sel) FontWeight(600) else FontWeight.Normal,
                                    color = textCol,
                                )
                            }
                        }
                    }
                }
            }

            EditField(label = "CATEGORY") {
                CategoryChips(
                    selected = viewModel.category,
                    onSelect = { viewModel.category = it },
                )
            }

            EditField(label = "CHANNEL") {
                PillChipRow(
                    options = TransactionChannel.entries,
                    selected = viewModel.channel,
                    label = { it.displayName },
                    onSelect = { viewModel.channel = it },
                )
            }

            EditField(label = "BANK") {
                PillChipRow(
                    options = Bank.entries,
                    selected = viewModel.bank,
                    label = { it.displayName },
                    onSelect = { viewModel.bank = it },
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
                    shape = RoundedCornerShape(12.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    EditField(label = "DATE") {
                        PickerField(
                            value = dateFormat.format(timestampDate),
                            onClick = { showDatePicker = true },
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    EditField(label = "TIME") {
                        PickerField(
                            value = timeFormat.format(timestampDate),
                            onClick = { showTimePicker = true },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (saveResult is SaveResult.DuplicateError) {
            Text(
                text = "A transaction with these details already exists.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (viewModel.merchant.isNotBlank() && viewModel.categoryChanged) {
                            showRememberDialog = true
                        } else {
                            viewModel.save(false)
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isNew) "Add Transaction" else "Save Changes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight(700),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            if (viewModel.merchant.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAddToCircleDialog = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Add to Circle",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        SingleDatePickerSheet(
            initialMillis = viewModel.timestamp,
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
                    text = "Remember choice?",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(600)),
                )
            },
            text = {
                Text(
                    text = "Apply ${viewModel.category.displayName} to all" +
                        " ${viewModel.merchant.trim()} transactions?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.outline,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRememberDialog = false
                    viewModel.save(true)
                }) { Text("Yes, remember") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRememberDialog = false
                    viewModel.save(false)
                }) { Text("Skip") }
            },
        )
    }
}

@Composable
private fun EditField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.outline,
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(
    selected: TransactionCategory,
    onSelect: (TransactionCategory) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TransactionCategory.entries.forEach { cat ->
            val isSelected = selected == cat
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.onSurface else BorderColor,
                        RoundedCornerShape(20.dp),
                    )
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(cat.color),
                )
                Text(
                    text = cat.displayName,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight(600) else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> PillChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.onSurface else BorderColor,
                        RoundedCornerShape(20.dp),
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = label(option),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight(600) else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun PickerField(value: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Default,
        )
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
        title = { Text("Add to Circle", fontWeight = FontWeight(600)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = merchant,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                people.forEach { person ->
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddToExisting(person) }
                            .padding(vertical = 10.dp),
                    )
                }
                Text(
                    text = "+ New person",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .clickable { onNewPerson() }
                        .padding(vertical = 10.dp),
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
        title = { Text("New Person", fontWeight = FontWeight(600)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$merchant will be added as an identifier.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
