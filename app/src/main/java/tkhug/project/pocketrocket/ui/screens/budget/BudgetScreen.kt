package tkhug.project.pocketrocket.ui.screens.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.components.*
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter

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
        shape     = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        CurrencyFormatter.format(spent, currencySymbol),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) ExpenseCoral else TextPrimary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Budget", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        CurrencyFormatter.format(budget, currencySymbol),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress  = { progress },
                modifier  = Modifier.fillMaxWidth().height(6.dp),
                color     = if (isOver) ExpenseCoral else PrimaryIndigo,
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


@Composable
fun BudgetScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: BudgetViewModel = viewModel(factory = BudgetViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var editingBudgetId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var amountInput by remember { mutableStateOf("") }

    Scaffold(
        modifier            = modifier,
        containerColor      = BackgroundSoft,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingBudgetId = null
                selectedCategoryId = null
                amountInput = ""
                showBudgetDialog = true
            }) { Icon(Icons.Default.Add, contentDescription = "Add budget") }
        }
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                PeriodHeader(
                    periodLabel      = state.period.label,
                    dateRangeText    = state.period.dateRangeText,
                    onPreviousPeriod = vm::previousPeriod,
                    onNextPeriod     = vm::nextPeriod,
                )
            }

            // Overall progress summary
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

            item {
                Text(
                    text     = "Categories",
                    style    = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color    = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) } }
            } else if (state.items.isEmpty()) {
                item { EmptyState(message = "No budget categories", subtitle = "Add expense categories to track spending") }
            } else {
                items(state.items, key = { it.category.id }) { item ->
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

                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.End) {
                        if (item.budget == null) {
                            TextButton(onClick = {
                                editingBudgetId = null
                                selectedCategoryId = item.category.id
                                amountInput = ""
                                showBudgetDialog = true
                            }) { Text("Set budget") }
                        } else {
                            IconButton(onClick = {
                                editingBudgetId = item.budget.id
                                selectedCategoryId = item.category.id
                                amountInput = item.budget.plannedAmount.toString()
                                showBudgetDialog = true
                            }) { Icon(Icons.Default.Edit, contentDescription = "Edit budget") }

                            IconButton(onClick = { vm.deleteBudget(item.budget.id) }) { Icon(Icons.Default.Delete, contentDescription = "Delete budget") }
                        }
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        val editing = editingBudgetId != null
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(if (editing) "Edit budget" else "Add budget") },
            text = {
                Column {
                    if (selectedCategoryId == null) {
                        Text("Select category:")
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            state.items.forEach { itc ->
                                TextButton(onClick = { selectedCategoryId = itc.category.id }) { Text(itc.category.name) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    } else {
                        val catName = state.items.find { it.category.id == selectedCategoryId }?.category?.name ?: ""
                        Text("Category: $catName")
                        Spacer(Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Planned amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("This budget will apply to the current period: ${state.period.dateRangeText}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val catId = selectedCategoryId ?: return@TextButton
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (editing) {
                        vm.updateBudget(editingBudgetId!!, amount, state.period.startMillis, state.period.endMillis)
                    } else {
                        vm.createBudget(catId, amount, state.period.startMillis, state.period.endMillis)
                    }
                    showBudgetDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showBudgetDialog = false }) { Text("Cancel") } }
        )
    }
}





