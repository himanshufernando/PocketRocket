package tkhug.project.pocketrocket.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showFinanceStartDialog by remember { mutableStateOf(false) }
    var showCloseFinanceConfirm by remember { mutableStateOf(false) }
    Scaffold(modifier = modifier, containerColor = BackgroundSoft, contentWindowInsets = WindowInsets.statusBars) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }

            // General section
            item { SettingsSectionLabel("General") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon        = Icons.Rounded.CurrencyExchange,
                        iconBg      = PastelGreen,
                        iconTint    = IncomeGreen,
                        title       = "Currency",
                        subtitle    = "${state.settings.currencyCode} (${state.settings.currencySymbol})",
                        onClick     = { showCurrencyDialog = true },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon        = Icons.Rounded.Palette,
                        iconBg      = PastelYellow,
                        iconTint    = Color(0xFFF57F17),
                        title       = "Appearance",
                        subtitle    = state.settings.themeMode,
                        onClick     = { showAppearanceDialog = true },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon        = Icons.Rounded.DateRange,
                        iconBg      = PastelBlue,
                        iconTint    = PrimaryIndigo,
                        title       = "First day of week",
                        subtitle    = if (state.settings.firstDayOfWeek == 2) "Monday" else "Sunday",
                        onClick     = { /* TODO */ },
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // Data section
            item { SettingsSectionLabel("Data") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon     = Icons.Rounded.Category,
                        iconBg   = PastelTeal,
                        iconTint = Color(0xFF00897B),
                        title    = "Categories",
                        subtitle = "Manage income & expense",
                        onClick  = { navController.navigate(tkhug.project.pocketrocket.ui.navigation.NavRoutes.MANAGE_CATEGORIES) },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon     = Icons.Rounded.Backup,
                        iconBg   = PastelTeal,
                        iconTint = Color(0xFF00897B),
                        title    = "Export data",
                        subtitle = "Export as CSV",
                        onClick  = { /* TODO */ },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon     = Icons.Rounded.RestorePage,
                        iconBg   = PastelOrange,
                        iconTint = Color(0xFFE65100),
                        title    = "Import data",
                        subtitle = "Restore from CSV",
                        onClick  = { /* Placeholder: file picker / import not implemented */ },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon     = Icons.Rounded.Repeat,
                        iconBg   = PastelIndigo,
                        iconTint = PrimaryIndigo,
                        title    = "Recurring transactions",
                        subtitle = "Manage recurring entries",
                        onClick  = { navController.navigate(tkhug.project.pocketrocket.ui.navigation.NavRoutes.RECURRING_TRANSACTIONS) },
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // Finance section
            item { SettingsSectionLabel("Finance") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon     = Icons.Rounded.CalendarToday,
                        iconBg   = PastelBlue,
                        iconTint = PrimaryIndigo,
                        title    = "Budgets",
                        subtitle = "Default budget period",
                        onClick  = { navController.navigate(tkhug.project.pocketrocket.ui.navigation.NavRoutes.BUDGET_SETTINGS) },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon     = Icons.Rounded.CalendarViewMonth,
                        iconBg   = PastelBlue,
                        iconTint = PrimaryIndigo,
                        title    = "Finance month",
                        subtitle = "Starts on ${state.settings.financeMonthStartDay}, ends on ${state.settings.financeMonthEndDay}",
                        onClick  = { showFinanceStartDialog = true },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    // Make the end-day more discoverable by providing an explicit row for editing the end day
                    SettingsRow(
                        icon     = Icons.Rounded.CalendarToday,
                        iconBg   = PastelBlue,
                        iconTint = PrimaryIndigo,
                        title    = "Finance month end",
                        subtitle = "Ends on ${state.settings.financeMonthEndDay}",
                        onClick  = { showFinanceStartDialog = true },
                    )
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                    SettingsRow(
                        icon     = Icons.Rounded.Sync,
                        iconBg   = PastelOrange,
                        iconTint = Color(0xFFE65100),
                        title    = "Close finance month",
                        subtitle = "End current finance month and start the next",
                        onClick  = { showCloseFinanceConfirm = true },
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // About section
            item { SettingsSectionLabel("About") }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon     = Icons.Rounded.Info,
                        iconBg   = PastelIndigo,
                        iconTint = PrimaryIndigo,
                        title    = "Version",
                        subtitle = "1.0.0",
                        onClick  = {},
                        showChevron = false,
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // Currency picker dialog
    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCode = state.settings.currencyCode,
            onSelect    = { code, symbol ->
                vm.updateCurrency(code, symbol)
                showCurrencyDialog = false
            },
            onDismiss   = { showCurrencyDialog = false },
        )
    }

    // Appearance picker dialog
    if (showAppearanceDialog) {
        val options = listOf("SYSTEM", "LIGHT", "DARK")
        var selected by remember { mutableStateOf(state.settings.themeMode) }
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("Appearance", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    options.forEach { opt ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = opt },
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = opt == selected, onClick = { selected = opt })
                            Spacer(Modifier.width(8.dp))
                            Text(opt, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateThemeMode(selected)
                    showAppearanceDialog = false
                }) { Text("Save", color = PrimaryIndigo) }
            },
            dismissButton = { TextButton(onClick = { showAppearanceDialog = false }) { Text("Cancel", color = PrimaryIndigo) } },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // Finance month start & end dialog — compact horizontal chip pickers (days 20..30)
    if (showFinanceStartDialog) {
        val days = (20..30).toList()
        val initStart = if (state.settings.financeMonthStartDay in 20..30) state.settings.financeMonthStartDay else 23
        val initEnd   = if (state.settings.financeMonthEndDay   in 20..30) state.settings.financeMonthEndDay   else 22
        // key by current persisted values so the chips re-initialize whenever settings change
        var selectedStart by remember(initStart) { mutableIntStateOf(initStart) }
        var selectedEnd   by remember(initEnd)   { mutableIntStateOf(initEnd) }
        AlertDialog(
            onDismissRequest = { showFinanceStartDialog = false },
            title = { Text("Finance month range", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ── Start day chips ───────────────────────────────────────
                    Text(
                        "Start day",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        days.forEach { day ->
                            val selected = day == selectedStart
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) PrimaryIndigo else PastelIndigo)
                                    .clickable { selectedStart = day }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$day",
                                    color = if (selected) Color.White else TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                    // ── End day chips ─────────────────────────────────────────
                    Text(
                        "End day",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        days.forEach { day ->
                            val selected = day == selectedEnd
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) PrimaryIndigo else PastelIndigo)
                                    .clickable { selectedEnd = day }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$day",
                                    color = if (selected) Color.White else TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                    // ── Preview ───────────────────────────────────────────────
                    val direction = if (selectedEnd >= selectedStart) "same month" else "crosses to next month"
                    Text(
                        "Period: day $selectedStart → day $selectedEnd ($direction)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateFinanceMonthRange(selectedStart, selectedEnd)
                    showFinanceStartDialog = false
                }) { Text("Save", color = PrimaryIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showFinanceStartDialog = false }) { Text("Cancel", color = PrimaryIndigo) }
            },
            shape = RoundedCornerShape(20.dp),
        )
    }

    // Close finance month confirmation
    if (showCloseFinanceConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseFinanceConfirm = false },
            title = { Text("Close finance month", fontWeight = FontWeight.SemiBold) },
            text = { Text("This will end the current finance month and create budgets for the next finance month. Proceed?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.closeFinanceMonth()
                    showCloseFinanceConfirm = false
                }) { Text("Yes", color = PrimaryIndigo) }
            },
            dismissButton = { TextButton(onClick = { showCloseFinanceConfirm = false }) { Text("Cancel", color = PrimaryIndigo) } },
            shape = RoundedCornerShape(20.dp),
        )
    }
}

@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text     = label.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        color    = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 4.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite),
        content = content,
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showChevron: Boolean = true,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (showChevron) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Currency picker ──────────────────────────────────────────────────────────

private val commonCurrencies = listOf(
    "LKR" to "LKR",
    "USD" to "$",
    "EUR" to "€",
    "GBP" to "£",
    "INR" to "₹",
    "JPY" to "¥",
    "AUD" to "A$",
    "CAD" to "C$",
    "SGD" to "S$",
    "AED" to "AED",
)

@Composable
private fun CurrencyPickerDialog(
    currentCode: String,
    onSelect: (code: String, symbol: String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                commonCurrencies.forEach { (code, symbol) ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(code, symbol) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = code == currentCode, onClick = { onSelect(code, symbol) })
                        Spacer(Modifier.width(8.dp))
                        Text("$code  ($symbol)", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PrimaryIndigo) }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

