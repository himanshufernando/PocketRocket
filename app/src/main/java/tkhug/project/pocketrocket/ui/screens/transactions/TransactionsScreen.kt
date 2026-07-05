package tkhug.project.pocketrocket.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.components.*
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.tooling.preview.Preview
import tkhug.project.pocketrocket.data.model.TransactionType

@Composable
fun TransactionsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: TransactionsViewModel = viewModel(factory = TransactionsViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    var expandedItemId by remember { mutableStateOf<Long?>(null) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }

    // Delete confirmation dialog
    deleteTargetId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title            = { Text("Delete Transaction") },
            text             = { Text("Are you sure you want to delete this transaction?") },
            confirmButton    = {
                TextButton(onClick = {
                    vm.deleteTransaction(id)
                    deleteTargetId = null
                }) { Text("Delete", color = ExpenseCoral) }
            },
            dismissButton    = {
                TextButton(onClick = { deleteTargetId = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        modifier              = modifier,
        containerColor        = BackgroundSoft,
        contentWindowInsets   = WindowInsets.statusBars,
        floatingActionButton  = {
            FloatingActionButton(
                onClick        = { navController.navigate(NavRoutes.ADD_TRANSACTION) },
                shape          = androidx.compose.foundation.shape.RoundedCornerShape(50),
                containerColor = PrimaryIndigo,
                contentColor   = Color.White,
            ) { Icon(Icons.Rounded.Add, "Add") }
        },
    ) { padding ->
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // Period header
            item {
                PeriodHeader(
                    periodLabel      = state.period.label,
                    dateRangeText    = state.period.dateRangeText,
                    onPreviousPeriod = vm::previousPeriod,
                    onNextPeriod     = vm::nextPeriod,
                    actionIcon       = Icons.Rounded.FilterList,
                    onActionClick    = { /* TODO: filter sheet */ },
                )
            }

            // Segmented toggle
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SegmentedToggle(
                        options  = listOf("All", "Income", "Expense"),
                        selected = state.filterTab,
                        onSelect = vm::setFilterTab,
                    )
                }
            }

            if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) } }
            } else if (state.transactions.isEmpty()) {
                item { EmptyState(message = "No transactions found", subtitle = "Tap + to record a transaction") }
            } else {
                // Group by date
                val grouped = state.transactions.groupBy {
                    tkhug.project.pocketrocket.ui.util.DateFormatter.formatDate(it.dateMillis)
                }
                grouped.forEach { (dateLabel, items) ->
                    item {
                        val dayTotal = items.sumOf { if (it.type == tkhug.project.pocketrocket.data.model.TransactionType.INCOME) it.amount else -it.amount }
                        SectionTotalHeader(
                            title    = dateLabel,
                            subtitle = "${items.size} transaction${if (items.size > 1) "s" else ""}",
                        )
                    }
                    items(items, key = { it.id }) { item ->
                        @OptIn(ExperimentalFoundationApi::class)
                        TransactionRow(
                            title            = item.title,
                            categoryName     = item.categoryName,
                            categoryIconName = item.categoryIconName,
                            categoryColorHex = item.categoryColorHex,
                            amount           = item.amount,
                            type             = item.type,
                            dateMillis       = item.dateMillis,
                            currencySymbol   = state.currencySymbol,
                            note             = item.note,
                            onClick          = { navController.navigate(NavRoutes.editTransaction(item.id)) },
                            onDeleteClick    = { deleteTargetId = item.id },
                            modifier         = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ─── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Transactions - Light")
@Composable
fun TransactionsPreviewLight() {
    PocketRocketTheme(themeMode = "LIGHT") {
        Surface {
            Column(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
                PeriodHeader(
                    periodLabel      = "Monthly",
                    dateRangeText    = "Apr 23 - May 22",
                    onPreviousPeriod = {},
                    onNextPeriod     = {},
                    actionIcon       = Icons.Rounded.FilterList,
                    onActionClick    = {},
                )
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SegmentedToggle(
                        options  = listOf("All", "Income", "Expense"),
                        selected = 0,
                        onSelect = {},
                    )
                }
                SectionTotalHeader(title = "Jul 5, 2026", subtitle = "2 transactions")
                TransactionRow(
                    title            = "Grocery Shopping",
                    categoryName     = "Food",
                    categoryIconName = "restaurant",
                    categoryColorHex = "#FF8A65",
                    amount           = 85.50,
                    type             = TransactionType.EXPENSE,
                    dateMillis       = System.currentTimeMillis(),
                    currencySymbol   = "LKR",
                    note             = "Vegetables and fruits",
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = DividerColor, thickness = 0.5.dp)
                TransactionRow(
                    title            = "Freelance Payment",
                    categoryName     = "Salary",
                    categoryIconName = "payments",
                    categoryColorHex = "#81C784",
                    amount           = 500.00,
                    type             = TransactionType.INCOME,
                    dateMillis       = System.currentTimeMillis(),
                    currencySymbol   = "LKR",
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = DividerColor, thickness = 0.5.dp)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Transactions - Dark")
@Composable
fun TransactionsPreviewDark() {
    PocketRocketTheme(themeMode = "DARK") {
        Surface {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                PeriodHeader(
                    periodLabel      = "Monthly",
                    dateRangeText    = "Apr 23 - May 22",
                    onPreviousPeriod = {},
                    onNextPeriod     = {},
                    actionIcon       = Icons.Rounded.FilterList,
                    onActionClick    = {},
                )
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SegmentedToggle(
                        options  = listOf("All", "Income", "Expense"),
                        selected = 0,
                        onSelect = {},
                    )
                }
                SectionTotalHeader(title = "Jul 5, 2026", subtitle = "2 transactions")
                TransactionRow(
                    title            = "Grocery Shopping",
                    categoryName     = "Food",
                    categoryIconName = "restaurant",
                    categoryColorHex = "#FF8A65",
                    amount           = 85.50,
                    type             = TransactionType.EXPENSE,
                    dateMillis       = System.currentTimeMillis(),
                    currencySymbol   = "LKR",
                    note             = "Vegetables and fruits",
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = DividerColor, thickness = 0.5.dp)
                TransactionRow(
                    title            = "Freelance Payment",
                    categoryName     = "Salary",
                    categoryIconName = "payments",
                    categoryColorHex = "#81C784",
                    amount           = 500.00,
                    type             = TransactionType.INCOME,
                    dateMillis       = System.currentTimeMillis(),
                    currencySymbol   = "LKR",
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = DividerColor, thickness = 0.5.dp)
            }
        }
    }
}

