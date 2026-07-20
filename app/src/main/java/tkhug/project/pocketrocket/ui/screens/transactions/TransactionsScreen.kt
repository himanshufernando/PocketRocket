package tkhug.project.pocketrocket.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.components.*
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
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
    var showFilterSheet by remember { mutableStateOf(false) }

    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            state             = state,
            onCategorySelected = vm::setSelectedCategory,
            onTagSelected     = vm::setSelectedTag,
            onClearFilters    = vm::clearFilters,
            onDismiss         = { showFilterSheet = false },
        )
    }

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
                        onActionClick    = { showFilterSheet = true },
                        actionBadge      = state.hasActiveFilter,
                        actionIconTint   = if (state.hasActiveFilter) PrimaryIndigoDark else PrimaryIndigo,
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

            // Active filter chips row
            if (state.hasActiveFilter) {
                item {
                    LazyRow(
                        modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.selectedCategoryId?.let { catId ->
                            val cat = state.availableCategories.find { it.id == catId }
                            if (cat != null) {
                                item {
                                    ActiveFilterChip(
                                        label    = cat.name,
                                        colorHex = cat.colorHex,
                                        onRemove = { vm.setSelectedCategory(null) },
                                    )
                                }
                            }
                        }
                        state.selectedTagName?.let { tag ->
                            item {
                                ActiveFilterChip(
                                    label    = tag,
                                    colorHex = null,
                                    onRemove = { vm.setSelectedTag(null) },
                                )
                            }
                        }
                    }
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

// ─── Filter components ─────────────────────────────────────────────────────────

@Composable
private fun ActiveFilterChip(
    label: String,
    colorHex: String?,
    onRemove: () -> Unit,
) {
    val chipColor = colorHex?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrDefault(PrimaryIndigo)
    } ?: PrimaryIndigo
    Surface(
        shape         = CircleShape,
        color         = chipColor.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier          = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = chipColor, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onRemove, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove filter", tint = chipColor, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    state: TransactionsUiState,
    onCategorySelected: (Long?) -> Unit,
    onTagSelected: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    text       = "Filter Transactions",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (state.hasActiveFilter) {
                    TextButton(onClick = onClearFilters) {
                        Text("Clear all", color = ExpenseCoral)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Category section ──────────────────────────────────────────────
            Text("Category", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick  = { onCategorySelected(null) },
                        label    = { Text("All") },
                    )
                }
                items(state.availableCategories, key = { it.id }) { cat ->
                    val catColor = runCatching {
                        Color(android.graphics.Color.parseColor(cat.colorHex))
                    }.getOrDefault(PrimaryIndigo)
                    FilterChip(
                        selected    = state.selectedCategoryId == cat.id,
                        onClick     = {
                            onCategorySelected(if (state.selectedCategoryId == cat.id) null else cat.id)
                        },
                        label       = { Text(cat.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(catColor, CircleShape),
                            )
                        },
                    )
                }
            }

            // ── Tag section ───────────────────────────────────────────────────
            if (state.availableTags.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Tag", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedTagName == null,
                            onClick  = { onTagSelected(null) },
                            label    = { Text("All") },
                        )
                    }
                    items(state.availableTags, key = { it.id }) { tag ->
                        FilterChip(
                            selected = state.selectedTagName == tag.name,
                            onClick  = {
                                onTagSelected(if (state.selectedTagName == tag.name) null else tag.name)
                            },
                            label    = { Text(tag.name) },
                        )
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

