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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.components.*
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*

@Composable
fun TransactionsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: TransactionsViewModel = viewModel(factory = TransactionsViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

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
                        TransactionRow(
                            title            = item.title,
                            categoryName     = item.categoryName,
                            categoryIconName = item.categoryIconName,
                            categoryColorHex = item.categoryColorHex,
                            amount           = item.amount,
                            type             = item.type,
                            dateMillis       = item.dateMillis,
                            currencySymbol   = state.currencySymbol,
                            onClick          = { navController.navigate(NavRoutes.editTransaction(item.id)) },
                            modifier         = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

