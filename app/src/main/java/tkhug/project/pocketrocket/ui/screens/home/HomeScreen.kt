package tkhug.project.pocketrocket.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import tkhug.project.pocketrocket.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.components.AmountText
import tkhug.project.pocketrocket.ui.components.CategoryIcon
import tkhug.project.pocketrocket.ui.components.NeutralAmountText
import tkhug.project.pocketrocket.ui.components.PeriodHeader
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    var expenseExpanded by remember { mutableStateOf(true) }
    var incomeExpanded  by remember { mutableStateOf(true) }
    var showPeriodMenu  by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceWhite),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            HomeTopBar()

            // Summary row: Income / Expense / Net difference
            SummaryRow(
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense,
                currencySymbol = state.currencySymbol,
            )

            // Period header with period-mode dropdown
            Box {
                PeriodHeader(
                    periodLabel        = state.period.label,
                    dateRangeText      = state.period.dateRangeText,
                    onPreviousPeriod   = vm::previousPeriod,
                    onNextPeriod       = vm::nextPeriod,
                    onPeriodLabelClick = { showPeriodMenu = true },
                    onActionClick      = { /* savings overview TODO */ },
                )
                DropdownMenu(
                    expanded         = showPeriodMenu,
                    onDismissRequest = { showPeriodMenu = false },
                ) {
                    listOf("Daily", "Weekly", "Monthly").forEach { mode ->
                        DropdownMenuItem(
                            text    = {
                                Text(
                                    mode,
                                    fontWeight = if (mode == state.period.label) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (mode == state.period.label) PrimaryIndigo else TextPrimary,
                                )
                            },
                            onClick = { vm.setPeriodMode(mode); showPeriodMenu = false },
                        )
                    }
                }
            }

            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                LazyColumn(
                    modifier       = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    // ── Expense section ───────────────────────────────────
                    item {
                        SectionHeader(
                            label          = "Expense",
                            totalAmount    = state.totalExpense,
                            currencySymbol = state.currencySymbol,
                            type           = TransactionType.EXPENSE,
                            isExpanded     = expenseExpanded,
                            onToggle       = { expenseExpanded = !expenseExpanded },
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = expenseExpanded,
                            enter   = expandVertically(),
                            exit    = shrinkVertically(),
                        ) {
                            if (state.expenseCategories.isEmpty()) EmptyGridPlaceholder()
                            else CategoryGrid(
                                items           = state.expenseCategories,
                                currencySymbol  = state.currencySymbol,
                                onCategoryClick = { navController.navigate(NavRoutes.addCategoryData(it)) },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(4.dp)) }

                    // ── Income section ────────────────────────────────────
                    item {
                        SectionHeader(
                            label          = "Income",
                            totalAmount    = state.totalIncome,
                            currencySymbol = state.currencySymbol,
                            type           = TransactionType.INCOME,
                            isExpanded     = incomeExpanded,
                            onToggle       = { incomeExpanded = !incomeExpanded },
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = incomeExpanded,
                            enter   = expandVertically(),
                            exit    = shrinkVertically(),
                        ) {
                            if (state.incomeCategories.isEmpty()) EmptyGridPlaceholder()
                            else CategoryGrid(
                                items           = state.incomeCategories,
                                currencySymbol  = state.currencySymbol,
                                onCategoryClick = { navController.navigate(NavRoutes.addCategoryData(it)) },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }

        // (AI Log pill removed)
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .statusBarsPadding()          // pushes content below the camera punch-hole / status bar
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Use Image for raster (PNG) drawables to avoid automatic tinting by Icon
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text       = "Budget",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    label: String,
    totalAmount: Double,
    currencySymbol: String,
    type: TransactionType,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val accentColor = if (type == TransactionType.INCOME) IncomeGreen else ExpenseCoral
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
        )
        Spacer(Modifier.width(8.dp))
        // Amount on the right with proper colouring for income/expense
        AmountText(
            amount   = totalAmount,
            type     = type,
            symbol   = currencySymbol,
            showSign = false,
            style    = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(7.dp))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.13f))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun SummaryRow(totalIncome: Double, totalExpense: Double, currencySymbol: String) {
    val diff = totalIncome - totalExpense
    val diffType = if (diff >= 0.0) TransactionType.INCOME else TransactionType.EXPENSE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FFFA)),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Income", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                AmountText(amount = totalIncome, type = TransactionType.INCOME, symbol = currencySymbol, showSign = false, style = MaterialTheme.typography.titleMedium)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F7)),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Expense", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                AmountText(amount = totalExpense, type = TransactionType.EXPENSE, symbol = currencySymbol, showSign = false, style = MaterialTheme.typography.titleMedium)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F7FF)),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Net", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                AmountText(amount = kotlin.math.abs(diff), type = diffType, symbol = currencySymbol, showSign = true, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    items: List<CategorySpendItem>,
    currencySymbol: String,
    onCategoryClick: (Long) -> Unit,
) {
    Column(
        modifier            = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.chunked(3).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { item ->
                    CategoryGridCard(
                        item           = item,
                        currencySymbol = currencySymbol,
                        onClick        = { onCategoryClick(item.categoryId) },
                        modifier       = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryGridCard(
    item: CategorySpendItem,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = remember(item.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(item.colorHex)) }
            .getOrDefault(Color(0xFF9E9E9E))
    }

    Card(
        modifier  = modifier.height(130.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick   = onClick,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Name  — top
            Text(
                text       = item.name,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary,
                textAlign  = TextAlign.Center,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
            )
            // Icon — middle
            CategoryIcon(
                iconName   = item.iconName,
                bgColorHex = item.colorHex,
                size       = 38.dp,
                iconSize   = 19.dp,
            )
            // Amount — bottom
            if (item.spentAmount > 0.0) {
                AmountText(
                    amount = item.spentAmount,
                    type = item.type,
                    symbol = currencySymbol,
                    showSign = false,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                NeutralAmountText(
                    amount = item.spentAmount,
                    modifier = Modifier,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AiLogPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier      = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(50), clip = false)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape         = RoundedCornerShape(50),
        color         = PrimaryIndigo,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector        = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text       = "AI Log",
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
        }
    }
}

@Composable
private fun EmptyGridPlaceholder() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("No categories yet", style = MaterialTheme.typography.bodySmall, color = TextHint)
    }
}

// AiLogPill removed — delete unused composable to keep file clean.



// ─── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Home - Light")
@Composable
fun HomePreviewLight() {
    PocketRocketTheme(themeMode = "LIGHT") {
        Surface {
            Column(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
                HomeTopBar()
                SummaryRow(totalIncome = 1_200.0, totalExpense = 950.0, currencySymbol = "LKR")
                PeriodHeader(periodLabel = "Monthly", dateRangeText = "Apr 23 - May 22", onPreviousPeriod = {}, onNextPeriod = {}, onPeriodLabelClick = {})
                Spacer(Modifier.height(8.dp))
                val sampleExpenses = listOf(
                    CategorySpendItem(1, "Food", "restaurant", "#FF8A65", 420.0, TransactionType.EXPENSE),
                    CategorySpendItem(2, "Transport", "directions_car", "#4DB6AC", 120.0, TransactionType.EXPENSE),
                    CategorySpendItem(3, "Shopping", "shopping_cart", "#BA68C8", 150.0, TransactionType.EXPENSE),
                )
                CategoryGrid(items = sampleExpenses, currencySymbol = "LKR", onCategoryClick = {})
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Home - Dark")
@Composable
fun HomePreviewDark() {
    PocketRocketTheme(themeMode = "DARK") {
        Surface {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                HomeTopBar()
                SummaryRow(totalIncome = 2_400.0, totalExpense = 1_700.0, currencySymbol = "LKR")
                PeriodHeader(periodLabel = "Monthly", dateRangeText = "Apr 23 - May 22", onPreviousPeriod = {}, onNextPeriod = {}, onPeriodLabelClick = {})
                Spacer(Modifier.height(8.dp))
                val sampleIncome = listOf(
                    CategorySpendItem(10, "Salary", "paid", "#81C784", 2400.0, TransactionType.INCOME),
                )
                CategoryGrid(items = sampleIncome, currencySymbol = "LKR", onCategoryClick = {})
            }
        }
    }
}


