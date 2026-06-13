package org.singhak.kubera.ui.settings

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.singhak.kubera.db.CategoryRule
import org.singhak.kubera.db.PersonIdentifier
import org.singhak.kubera.model.PersonSummary
import org.singhak.kubera.model.TransactionCategory
import org.singhak.kubera.model.color
import org.singhak.kubera.ui.circle.CircleViewModel
import org.singhak.kubera.ui.rules.RulesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var subScreen by remember { mutableStateOf<String?>(null) }
    var selectedPersonId by remember { mutableStateOf<Long?>(null) }

    BackHandler {
        when {
            selectedPersonId != null -> selectedPersonId = null
            subScreen != null -> subScreen = null
            else -> onBack()
        }
    }

    when {
        subScreen == "rules" -> RulesSubScreen(onBack = { subScreen = null })
        subScreen == "categories" -> CategoriesSubScreen(onBack = { subScreen = null })
        subScreen == "circles" && selectedPersonId != null ->
            CircleDetailSubScreen(
                personId = selectedPersonId!!,
                onBack = { selectedPersonId = null },
            )
        subScreen == "circles" ->
            CirclesSubScreen(
                onBack = { subScreen = null },
                onSelectPerson = { selectedPersonId = it },
            )
        else -> RootSettingsList(
            onBack = onBack,
            onNavigate = { subScreen = it },
            modifier = modifier,
        )
    }
}

@Composable
private fun RootSettingsList(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    rulesViewModel: RulesViewModel = hiltViewModel(),
    circleViewModel: CircleViewModel = hiltViewModel(),
) {
    val userRules by rulesViewModel.userRules.collectAsState()
    val summaries by circleViewModel.personSummaries.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
            )
            Text(
                text = "Settings",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )

        // Settings rows
        SettingsRow(
            title = "Rules",
            subtitle = "${userRules.size} custom ${if (userRules.size == 1) "rule" else "rules"}",
            onClick = { onNavigate("rules") },
        )
        SettingsRow(
            title = "Categories",
            subtitle = "${TransactionCategory.entries.size} categories",
            onClick = { onNavigate("categories") },
        )
        SettingsRow(
            title = "Circles",
            subtitle = "${summaries.size} ${if (summaries.size == 1) "person" else "people"}",
            onClick = { onNavigate("circles") },
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(500),
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LongMethod")
@Composable
private fun RulesSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RulesViewModel = hiltViewModel(),
) {
    val userRules by viewModel.userRules.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
            )
            Text(
                text = "Rules",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable { showAddSheet = true }
                    .padding(8.dp),
            )
        }

        // Description
        Text(
            text = "When a merchant name contains the pattern, it is automatically assigned that category.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = (12 * 1.6).sp,
            ),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )

        // Section label
        Text(
            text = "ALL RULES",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (userRules.isEmpty()) {
                item {
                    Text(
                        text = "No custom rules yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
            } else {
                items(userRules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        onDelete = { viewModel.deleteRule(rule) },
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            AddRuleSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { keyword, category ->
                    viewModel.addRule(keyword, category)
                    showAddSheet = false
                },
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: CategoryRule,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = rule.keyword,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight(600),
                fontFamily = FontFamily.Monospace,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(rule.category.color),
        )
        Text(
            text = rule.category.displayName,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline,
        )
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .size(18.dp)
                .clickable { onDelete() },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("LongMethod")
@Composable
private fun AddRuleSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, TransactionCategory) -> Unit,
) {
    var keyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.OTHER) }
    val categories = remember {
        listOf(
            TransactionCategory.FOOD,
            TransactionCategory.RIDES,
            TransactionCategory.TRAVEL,
            TransactionCategory.GROCERIES,
            TransactionCategory.SHOPPING,
            TransactionCategory.BILLS,
            TransactionCategory.FUEL,
            TransactionCategory.INVESTMENTS,
            TransactionCategory.RENT,
            TransactionCategory.ENTERTAINMENT,
            TransactionCategory.OTHER,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "New Rule",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight(600),
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Keyword input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "MERCHANT CONTAINS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.outline,
            )
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Category selection
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "ASSIGN CATEGORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.outline,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { category ->
                    val selected = selectedCategory == category
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(20.dp),
                            )
                            .background(
                                if (selected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(category.color),
                        )
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = if (selected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }

        // Save button
        TextButton(
            onClick = { if (keyword.isNotBlank()) onConfirm(keyword.trim(), selectedCategory) },
            enabled = keyword.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = "Add Rule",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                color = if (keyword.isNotBlank()) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline,
            )
        }
    }
}

// ── Categories sub-screen ────────────────────────────────────────────────────

@Composable
private fun CategoriesSubScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = remember { TransactionCategory.entries }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.clickable { onBack() }.padding(8.dp),
            )
            Text(
                text = "Categories",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight(600)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
        }

        Text(
            text = "ALL CATEGORIES",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(categories) { category ->
                CategoryRow(category = category)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(category: TransactionCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(category.color),
        )
        Text(
            text = category.displayName,
            fontSize = 13.sp,
            fontWeight = FontWeight(600),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "DEFAULT",
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

// ── Circles sub-screens ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CirclesSubScreen(
    onBack: () -> Unit,
    onSelectPerson: (Long) -> Unit,
    viewModel: CircleViewModel = hiltViewModel(),
) {
    val summaries by viewModel.personSummaries.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.clickable { onBack() }.padding(8.dp),
            )
            Text(
                text = "Circles",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight(600)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { showAddSheet = true }.padding(8.dp),
            )
        }

        Text(
            text = "ALL CIRCLES",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (summaries.isEmpty()) {
                item {
                    Text(
                        text = "No circles yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
            } else {
                items(summaries, key = { it.person.id }) { summary ->
                    CircleSummaryRow(summary = summary, onClick = { onSelectPerson(summary.person.id) })
                    Box(
                        modifier = Modifier.fillMaxWidth().height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            AddPersonSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { name ->
                    viewModel.addPerson(name)
                    showAddSheet = false
                },
            )
        }
    }
}

@Composable
private fun CircleSummaryRow(summary: PersonSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = summary.person.name.first().uppercase(),
                fontSize = 13.sp,
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
            )
            Spacer(Modifier.height(3.dp))
            val idCount = summary.identifiers.size
            Text(
                text = "$idCount UPI ${if (idCount == 1) "ID" else "IDs"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CircleDetailSubScreen(
    personId: Long,
    onBack: () -> Unit,
    viewModel: CircleViewModel = hiltViewModel(),
) {
    val summaries by viewModel.personSummaries.collectAsState()
    val summary = summaries.find { it.person.id == personId }
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.clickable { onBack() }.padding(8.dp),
            )
            Text(
                text = summary?.person?.name ?: "",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight(600)),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            Text(
                text = "+",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable { showAddSheet = true }.padding(8.dp),
            )
        }

        Text(
            text = "LINKED UPI IDs",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            val identifiers = summary?.identifiers ?: emptyList()
            if (identifiers.isEmpty()) {
                item {
                    Text(
                        text = "No identifiers yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
            } else {
                items(identifiers, key = { it.id }) { identifier ->
                    IdentifierRow(
                        identifier = identifier,
                        onDelete = { viewModel.deleteIdentifier(identifier) },
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth().height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            AddIdentifierSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { identifier ->
                    viewModel.addIdentifier(personId, identifier)
                    showAddSheet = false
                },
            )
        }
    }
}

@Composable
private fun IdentifierRow(identifier: PersonIdentifier, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = identifier.identifier,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp).clickable { onDelete() },
        )
    }
}

@Composable
private fun AddPersonSheet(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "New Circle",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight(600)),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "NAME",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.outline,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        TextButton(
            onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
            enabled = name.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = "Create Circle",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                color = if (name.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun AddIdentifierSheet(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var identifier by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Add UPI ID",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight(600)),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "UPI ID",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 2.sp),
                color = MaterialTheme.colorScheme.outline,
            )
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                singleLine = true,
                placeholder = { Text("e.g. name@okaxis", style = MaterialTheme.typography.bodySmall) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        TextButton(
            onClick = { if (identifier.isNotBlank()) onConfirm(identifier.trim()) },
            enabled = identifier.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = "Add",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight(600)),
                color = if (identifier.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            )
        }
    }
}
