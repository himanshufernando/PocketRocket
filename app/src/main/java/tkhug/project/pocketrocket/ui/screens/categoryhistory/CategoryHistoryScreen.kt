package tkhug.project.pocketrocket.ui.screens.categoryhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.components.CategoryIcon
import tkhug.project.pocketrocket.ui.components.EmptyState
import tkhug.project.pocketrocket.ui.components.TransactionRow
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter
import tkhug.project.pocketrocket.ui.util.DateFormatter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHistoryScreen(
    categoryId: Long,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: CategoryHistoryViewModel = viewModel(
        key     = "history_$categoryId",
        factory = CategoryHistoryViewModel.factory(context, categoryId),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    val category    = state.category
    val accentColor = remember(category?.colorHex) {
        category?.colorHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        } ?: PrimaryIndigo
    }
    val isIncome = category?.type == TransactionType.INCOME
    val typeColor = if (isIncome) IncomeGreen else ExpenseCoral

    Scaffold(
        modifier            = modifier,
        containerColor      = BackgroundSoft,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            // Custom top row to match spec: X | ◀ 04.23~05.22 ▶ | [clock] [filter]
            Surface(
                color = SurfaceWhite,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                    Spacer(Modifier.width(4.dp))
                    // Center date range with chevrons
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        IconButton(onClick = { vm.previousPeriod() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Prev", tint = TextSecondary)
                        }
                        Text(
                            text = state.period.dateRangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                        IconButton(onClick = { vm.nextPeriod() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next", tint = TextSecondary)
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(50)).background(PastelIndigo).clickable { /* quick action */ }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Timer, contentDescription = "Clock", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                        }
                        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(50)).background(PastelIndigo).clickable { /* filter sheet TODO */ }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.FilterList, contentDescription = "Filter", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // ── Category summary header ───────────────────────────────────
            item {
                CategorySummaryHeader(
                    categoryName   = category?.name ?: "",
                    iconName       = category?.iconName ?: "category",
                    colorHex       = category?.colorHex ?: "#9E9E9E",
                    totalAmount    = state.totalAmount,
                    txCount        = state.transactions.size,
                    currencySymbol = state.currencySymbol,
                    typeColor      = typeColor,
                    isIncome       = isIncome,
                )
            }

            item {
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 16.dp),
                    color     = DividerColor,
                    thickness = 1.dp,
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Transaction list grouped by day ─────────────────────────
            if (state.transactions.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(48.dp))
                        Text("No records yet", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text("Add your first transaction for this category.", style = MaterialTheme.typography.bodySmall, color = TextHint)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navController.navigate(NavRoutes.addCategoryData(categoryId)) }, shape = RoundedCornerShape(12.dp)) {
                            Text("Add Record")
                        }
                    }
                }
            } else {
                val grouped = state.transactions.groupBy { tx ->
                    DateFormatter.formatDayOfWeek(tx.dateMillis) + ", " + DateFormatter.formatShortDate(tx.dateMillis)
                }
                grouped.forEach { (dayLabel, txList) ->
                    val dayTotal = txList.sumOf { it.amount }
                    item(key = dayLabel) {
                        TransactionGroupCard(
                            dateLabel = dayLabel,
                            dayTotal  = dayTotal,
                            currency  = state.currencySymbol,
                            isIncome  = isIncome,
                            typeColor = typeColor,
                        )
                    }
                    items(txList, key = { it.id }) { tx ->
                        TransactionListItem(
                            id               = tx.id,
                            title            = tx.title,
                            note             = tx.note,
                            iconName         = tx.categoryIconName,
                            iconColorHex     = tx.categoryColorHex,
                            amount           = tx.amount,
                            type             = tx.type,
                            dateMillis       = tx.dateMillis,
                            currencySymbol   = state.currencySymbol,
                            onClick          = { navController.navigate(NavRoutes.editTransaction(tx.id)) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionGroupCard(
    dateLabel: String,
    dayTotal: Double,
    currency: String,
    isIncome: Boolean,
    typeColor: Color,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = dateLabel, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(
                text = (if (isIncome) "IN " else "OUT ") + CurrencyFormatter.format(dayTotal, currency),
                style = MaterialTheme.typography.bodyMedium,
                color = typeColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TransactionListItem(
    id: Long,
    title: String,
    note: String,
    iconName: String,
    iconColorHex: String,
    amount: Double,
    type: TransactionType,
    dateMillis: Long,
    currencySymbol: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(iconName = iconName, bgColorHex = iconColorHex, size = 40.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            if (note.isNotBlank()) Text(text = note, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            val formatted = CurrencyFormatter.format(amount, currencySymbol)
            val signed = if (type == TransactionType.INCOME) "+$formatted" else "-$formatted"
            Text(text = signed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (type == TransactionType.INCOME) IncomeGreen else ExpenseCoral)
            Spacer(Modifier.height(4.dp))
            Text(text = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(dateMillis)), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun CategorySummaryHeader(
    categoryName: String,
    iconName: String,
    colorHex: String,
    totalAmount: Double,
    txCount: Int,
    currencySymbol: String,
    typeColor: Color,
    isIncome: Boolean,
) {
    val accentColor = runCatching {
        Color(android.graphics.Color.parseColor(colorHex))
    }.getOrDefault(PrimaryIndigo)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(
                iconName   = iconName,
                bgColorHex = colorHex,
                size       = 52.dp,
                iconSize   = 26.dp,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = categoryName,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text  = "$txCount transaction${if (txCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = CurrencyFormatter.format(totalAmount, currencySymbol),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = typeColor,
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text      = if (isIncome) "Income" else "Expense",
                        fontSize  = 10.sp,
                        color     = typeColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    label: String,
    total: Double,
    currencySymbol: String,
    typeColor: Color,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary,
        )
        Text(
            text       = CurrencyFormatter.format(total, currencySymbol),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = typeColor,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
private fun CategoryHistoryPreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            CategorySummaryHeader(
                categoryName = "Salary",
                iconName = "payments",
                colorHex = "#43A047",
                totalAmount = 331000.0,
                txCount = 1,
                currencySymbol = "LKR",
                typeColor = IncomeGreen,
                isIncome = true,
            )
            Spacer(Modifier.height(8.dp))
            TransactionGroupCard(
                dateLabel = "Mon, Apr 1",
                dayTotal = 331000.0,
                currency = "LKR",
                isIncome = true,
                typeColor = IncomeGreen,
            )
            Spacer(Modifier.height(8.dp))
            TransactionListItem(
                id = 1L,
                title = "Monthly Salary",
                note = "April payroll",
                iconName = "payments",
                iconColorHex = "#43A047",
                amount = 331000.0,
                type = TransactionType.INCOME,
                dateMillis = System.currentTimeMillis(),
                currencySymbol = "LKR",
                onClick = {}
            )
        }
    }
}
