package tkhug.project.pocketrocket.ui.screens.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.components.*
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter

// ─── Budget Summary Card ───────────────────────────────────────────────────────

@Composable
fun BudgetSummaryCard(
    spent: Double,
    budget: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier,
) {
    val progress = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOver   = spent > budget

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, DividerColor),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        CurrencyFormatter.format(spent, currencySymbol),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = if (isOver) ExpenseCoral else TextPrimary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Budget", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        CurrencyFormatter.format(budget, currencySymbol),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().height(6.dp),
                color      = if (isOver) ExpenseCoral else PrimaryIndigo,
                trackColor = PastelIndigo,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "${(progress * 100).toInt()}% of budget used",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

// ─── Budget Screen ─────────────────────────────────────────────────────────────

@Composable
fun BudgetScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: BudgetViewModel = viewModel(factory = BudgetViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    // ── Dialog state ──────────────────────────────────────────────────────────
    var showBudgetDialog      by remember { mutableStateOf(false) }
    var showDeleteConfirm     by remember { mutableStateOf(false) }
    var editingBudgetId       by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId    by remember { mutableStateOf<Long?>(null) }
    var pendingDeleteBudgetId by remember { mutableStateOf<Long?>(null) }
    var amountInput           by remember { mutableStateOf("") }

    Scaffold(
        modifier            = modifier,
        containerColor      = BackgroundSoft,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBudgetId    = null
                    selectedCategoryId = null
                    amountInput        = ""
                    showBudgetDialog   = true
                },
                containerColor = PrimaryIndigo,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add budget", tint = Color.White)
            }
        },
    ) { padding ->

        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {

            // Period navigation header
            item {
                PeriodHeader(
                    periodLabel      = state.period.label,
                    dateRangeText    = state.period.dateRangeText,
                    onPreviousPeriod = vm::previousPeriod,
                    onNextPeriod     = vm::nextPeriod,
                )
            }

            // Overall summary card
            if (!state.isLoading && state.totalBudget > 0) {
                item {
                    BudgetSummaryCard(
                        spent          = state.totalSpent,
                        budget         = state.totalBudget,
                        currencySymbol = state.currencySymbol,
                        modifier       = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            // Section title
            item {
                Text(
                    text       = "Categories",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            // Loading / empty / list
            if (state.isLoading) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = PrimaryIndigo) }
                }
            } else if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        message  = "No budget categories",
                        subtitle = "Add expense categories to track spending",
                    )
                }
            } else {
                items(state.items, key = { it.category.id }) { item ->

                    // Category spend card
                    CategoryCard(
                        categoryName   = item.category.name,
                        iconName       = item.category.iconName,
                        colorHex       = item.category.colorHex,
                        spent          = item.spent,
                        budget         = item.budget?.plannedAmount ?: 0.0,
                        currencySymbol = state.currencySymbol,
                        type           = TransactionType.EXPENSE,
                        onClick        = { navController.navigate(NavRoutes.categoryDetail(item.category.id)) },
                        modifier       = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    // Action row: Set / Edit / Delete
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        if (item.budget == null) {
                            TextButton(
                                onClick = {
                                    editingBudgetId    = null
                                    selectedCategoryId = item.category.id
                                    amountInput        = ""
                                    showBudgetDialog   = true
                                },
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Set budget", fontSize = 13.sp)
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    editingBudgetId    = item.budget.id
                                    selectedCategoryId = item.category.id
                                    amountInput        = item.budget.plannedAmount
                                        .toBigDecimal().stripTrailingZeros().toPlainString()
                                    showBudgetDialog   = true
                                },
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit", fontSize = 13.sp)
                            }

                            Spacer(Modifier.width(4.dp))

                            TextButton(
                                onClick = {
                                    pendingDeleteBudgetId = item.budget.id
                                    showDeleteConfirm      = true
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = ExpenseCoral),
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete", fontSize = 13.sp)
                            }
                        }
                    }

                    HorizontalDivider(
                        color     = DividerColor,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }

    // ── Add / Edit dialog ─────────────────────────────────────────────────────
    if (showBudgetDialog) {
        BudgetEntryDialog(
            isEditing         = editingBudgetId != null,
            initialAmount     = amountInput,
            initialCategoryId = selectedCategoryId,
            categoryItems     = state.items,
            periodLabel       = state.period.dateRangeText,
            currencySymbol    = state.currencySymbol,
            onConfirm         = { catId, amount ->
                if (editingBudgetId != null) {
                    vm.updateBudget(
                        editingBudgetId!!,
                        amount,
                        state.period.startMillis,
                        state.period.endMillis,
                    )
                } else {
                    vm.createBudget(catId, amount, state.period.startMillis, state.period.endMillis)
                }
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false },
        )
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm     = false
                pendingDeleteBudgetId = null
            },
            icon  = { Icon(Icons.Rounded.Warning, contentDescription = null, tint = ExpenseCoral) },
            title = { Text("Delete budget?") },
            text  = {
                Text("This will remove the budget for this category. Spending data will not be affected.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteBudgetId?.let { vm.deleteBudget(it) }
                        showDeleteConfirm     = false
                        pendingDeleteBudgetId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm     = false
                    pendingDeleteBudgetId = null
                }) { Text("Cancel") }
            },
        )
    }
}

// ─── Add / Edit Budget Dialog ─────────────────────────────────────────────────

@Composable
private fun BudgetEntryDialog(
    isEditing: Boolean,
    initialAmount: String,
    initialCategoryId: Long?,
    categoryItems: List<BudgetCategoryItem>,
    periodLabel: String,
    currencySymbol: String,
    onConfirm: (categoryId: Long, amount: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedCat by remember { mutableStateOf(initialCategoryId) }
    var amountInput by remember { mutableStateOf(initialAmount) }
    val focusManager = LocalFocusManager.current
    val focusReq     = remember { FocusRequester() }
    val scrollState  = rememberScrollState()

    val amountValid = amountInput.toDoubleOrNull()?.let { it > 0 } == true
    val catValid    = selectedCat != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Edit budget" else "Set budget",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!isEditing) {
                    // ── Category picker ───────────────────────────────────
                    Text(
                        "Select category",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categoryItems.forEach { item ->
                            val isSelected = item.category.id == selectedCat
                            val accentColor = runCatching {
                                Color(android.graphics.Color.parseColor(item.category.colorHex))
                            }.getOrDefault(Color(0xFF9E9E9E))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) accentColor.copy(alpha = 0.15f)
                                        else Color.Transparent,
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentColor else DividerColor,
                                        shape = RoundedCornerShape(12.dp),
                                    )
                                    .clickable { selectedCat = item.category.id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                CategoryIcon(
                                    iconName   = item.category.iconName,
                                    bgColorHex = item.category.colorHex,
                                    size       = 32.dp,
                                    iconSize   = 16.dp,
                                )
                                Text(
                                    item.category.name,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isSelected) accentColor else TextPrimary,
                                    modifier   = Modifier.weight(1f),
                                )
                                if (item.budget != null) {
                                    Text(
                                        CurrencyFormatter.format(item.budget.plannedAmount, currencySymbol),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = DividerColor)
                } else {
                    // ── Editing: show category info (read-only) ───────────
                    val editItem = categoryItems.find { it.category.id == selectedCat }
                    if (editItem != null) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CategoryIcon(
                                iconName   = editItem.category.iconName,
                                bgColorHex = editItem.category.colorHex,
                                size       = 36.dp,
                                iconSize   = 18.dp,
                            )
                            Column {
                                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    editItem.category.name,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextPrimary,
                                )
                            }
                        }
                    }
                }

                // ── Amount input ──────────────────────────────────────────
                OutlinedTextField(
                    value         = amountInput,
                    onValueChange = { amountInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label         = { Text("Planned amount ($currencySymbol)") },
                    singleLine    = true,
                    isError       = amountInput.isNotEmpty() && !amountValid,
                    supportingText = if (amountInput.isNotEmpty() && !amountValid) {
                        { Text("Enter a valid positive amount", color = ExpenseCoral) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction    = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier      = Modifier.fillMaxWidth().focusRequester(focusReq),
                )

                // ── Period info ───────────────────────────────────────────
                Text(
                    "Period: $periodLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val catId  = selectedCat ?: return@Button
                    val amount = amountInput.toDoubleOrNull() ?: return@Button
                    onConfirm(catId, amount)
                },
                enabled = catValid && amountValid,
                colors  = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    // Auto-focus amount field when editing (category already known)
    LaunchedEffect(isEditing) {
        if (isEditing) focusReq.requestFocus()
    }
}

