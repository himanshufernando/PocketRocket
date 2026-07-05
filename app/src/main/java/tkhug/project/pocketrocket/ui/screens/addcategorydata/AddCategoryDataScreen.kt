package tkhug.project.pocketrocket.ui.screens.addcategorydata

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.AccountEntity
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.components.CategoryIcon
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import kotlin.math.abs

// ─── Toolbar items definition ─────────────────────────────────────────────────
private data class ToolbarItem(val label: String, val icon: ImageVector, val key: String)
private val toolbarItems = listOf(
    ToolbarItem("Tag",        Icons.Rounded.Label,                "tag"),
    ToolbarItem("Note",       Icons.Rounded.Notes,                "note"),
    ToolbarItem("Account",    Icons.Rounded.AccountBalanceWallet, "account"),
    ToolbarItem("Today",      Icons.Rounded.CalendarToday,        "today"),
    ToolbarItem("Recurring",  Icons.Rounded.Repeat,               "recurring"),
    ToolbarItem("Exclude",    Icons.Rounded.RemoveCircleOutline,  "exclude"),
    ToolbarItem("Cont.Input", Icons.Rounded.AllInclusive,         "cont_input"),
    ToolbarItem("More",       Icons.Rounded.Tune,                 "more"),
)

// ─── Keypad definition ────────────────────────────────────────────────────────
private data class KeyDef(
    val label: String,
    val isBackspace: Boolean  = false,
    val isConfirm: Boolean    = false,
    val isOperator: Boolean   = false,
    val isCurrency: Boolean   = false,
)
private val keyRows = listOf(
    listOf(KeyDef("7"), KeyDef("8"), KeyDef("9"), KeyDef("⌫", isBackspace = true)),
    listOf(KeyDef("4"), KeyDef("5"), KeyDef("6"), KeyDef("+", isOperator = true)),
    listOf(KeyDef("1"), KeyDef("2"), KeyDef("3"), KeyDef("-", isOperator = true)),
    listOf(KeyDef("LKR", isCurrency = true), KeyDef("0"), KeyDef("."), KeyDef("✓", isConfirm = true)),
)

// ─── Colours ──────────────────────────────────────────────────────────────────
private val KeyBg     = Color(0xFFF2F2F7)
private val ConfirmYellow = Color(0xFFFAC748)
private val ToolbarBg = Color(0xFFFFF8E2)
private val SheetBg   = Color(0xFFEEEEF5)   // slightly dimmed behind white sheet

// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun AddCategoryDataScreen(
    categoryId: Long,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: AddCategoryDataViewModel = viewModel(
        key     = "adddata_$categoryId",
        factory = AddCategoryDataViewModel.factory(context, categoryId),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    // Navigate back (or reset for continuous input) after save
    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) {
            if (state.continuousInput) vm.resetAfterSave() else navController.popBackStack()
        }
    }

    // Local UI toggles
    var showNote    by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }

    val category    = state.category
    val isIncome    = category?.type == TransactionType.INCOME
    val accentColor = remember(category?.colorHex) {
        category?.colorHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        } ?: PrimaryIndigo
    }

    // ── Root: dim background + white rounded sheet ────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SheetBg)
            .statusBarsPadding(),        // respect camera hole / status bar
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 8.dp),    // small visual gap below status bar
            shape  = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color  = SurfaceWhite,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── 1. Top bar ─────────────────────────────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // X close
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.Close, "Close",
                            tint     = TextPrimary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    // Category name (center)
                    Text(
                        text       = category?.name ?: "Add Data",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.weight(1f),
                    )
                    // History icon (circular, light purple)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PastelIndigo)
                            .clickable { navController.navigate(NavRoutes.categoryHistory(categoryId)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Rounded.FormatListBulleted,
                            contentDescription = "History",
                            tint               = PrimaryIndigo,
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }

                // ── 2. Middle section (scrolls if content overflows) ───────
                Column(
                    modifier            = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(4.dp))

                    // Expression label (e.g. "LKR3,500  +")
                    val expr = state.expressionLabel
                    if (expr != null) {
                        Text(expr, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(Modifier.height(2.dp))
                    }

                    // ── Amount display ─────────────────────────────────────
                    Text(
                        text       = state.formattedAmount,
                        fontSize   = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (state.displayText == "0") TextHint else TextPrimary,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        maxLines   = 1,
                    )

                    // Validation error
                    val err = state.validationError
                    if (err != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(err, style = MaterialTheme.typography.bodySmall, color = ExpenseCoral)
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Budget info row (when budget exists) ──────────────
                    AnimatedVisibility(
                        visible = state.categoryBudget != null,
                        enter   = expandVertically(),
                        exit    = shrinkVertically(),
                    ) {
                        state.categoryBudget?.let { budget ->
                            BudgetInfoRow(
                                budget         = budget,
                                spent          = state.categorySpent,
                                remaining      = state.budgetRemaining,
                                progress       = state.budgetProgress,
                                currencySymbol = state.currencySymbol,
                                accentColor    = accentColor,
                                modifier       = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // ── Budget not set banner (expense only) ───────────────
                    AnimatedVisibility(
                        visible = state.showBudgetBanner,
                        enter   = expandVertically(),
                        exit    = shrinkVertically(),
                    ) {
                        BudgetNotSetBanner(
                            onSetNow  = { navController.navigate(NavRoutes.BUDGET) },
                            modifier  = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Quick tag chips ────────────────────────────────────
                    val tags = state.expenseTags
                    println("xxxxxxxx tags "+tags)
                    QuickTagChips(
                        tags        = tags,
                        selectedTag = state.selectedTag,
                        accentColor = accentColor,
                        onTagClick  = vm::onTagSelected,
                    )

                    Spacer(Modifier.height(8.dp))

                    // ── Settings / gear chip ───────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .border(1.dp, DividerColor, RoundedCornerShape(50))
                                .clickable { navController.navigate(tkhug.project.pocketrocket.ui.navigation.NavRoutes.SETTINGS) }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Settings, null,
                                    tint     = TextSecondary,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Settings", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }

                // ── 3. Toolbar strip ───────────────────────────────────────
                ToolbarStrip(
                    items              = toolbarItems,
                    isContinuousInput  = state.continuousInput,
                    onItemClick        = { key ->
                        when (key) {
                            "note"       -> showNote = !showNote
                            "account"    -> showAccount = true
                            "cont_input" -> vm.toggleContinuousInput()
                        }
                    },
                )

                // ── 4. Note input (animated) ───────────────────────────────
                AnimatedVisibility(
                    visible = showNote,
                    enter   = expandVertically(),
                    exit    = shrinkVertically(),
                ) {
                    OutlinedTextField(
                        value         = state.noteText,
                        onValueChange = vm::onNoteChanged,
                        placeholder   = { Text("Add a note…", color = TextPrimary, fontSize = 13.sp) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = accentColor,
                            unfocusedBorderColor = TextPrimary,
                            focusedTextColor     = TextPrimary,
                            unfocusedTextColor   = TextPrimary,
                            cursorColor          = accentColor,
                        ),
                        leadingIcon   = {
                            Icon(Icons.Rounded.Notes, null, tint = TextSecondary,
                                modifier = Modifier.size(16.dp))
                        },
                    )
                }

                // ── 5. Numeric keypad ──────────────────────────────────────
                NumericKeypad(
                    currencyLabel = state.currencySymbol,
                    onKey         = vm::onKeyPress,
                    onConfirm     = vm::saveTransaction,
                    isSaving      = state.isSaving,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()          // lift above OS nav bar
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }

        // ── Account picker dialog ─────────────────────────────────────────────
        if (showAccount && state.accounts.isNotEmpty()) {
            AccountPickerDialog(
                accounts          = state.accounts,
                selectedId        = state.selectedAccountId,
                onSelect          = { vm.onAccountSelected(it); showAccount = false },
                onDismiss         = { showAccount = false },
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// Sub-composables
// ═════════════════════════════════════════════════════════════════════════════

// ── Budget info row ───────────────────────────────────────────────────────────
@Composable
private fun BudgetInfoRow(
    budget: Double,
    spent: Double,
    remaining: Double,
    progress: Float,
    currencySymbol: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val isOver = spent > budget
    val barColor = if (isOver) ExpenseCoral else accentColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Spent", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    CurrencyFormatter.format(spent, currencySymbol),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isOver) ExpenseCoral else TextPrimary,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Budget", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    CurrencyFormatter.format(budget, currencySymbol),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(if (isOver) "Over" else "Left", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    CurrencyFormatter.format(abs(remaining), currencySymbol),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isOver) ExpenseCoral else accentColor,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color      = barColor,
            trackColor = accentColor.copy(alpha = 0.15f),
        )
    }
}

// ── Budget not set banner ─────────────────────────────────────────────────────
@Composable
private fun BudgetNotSetBanner(onSetNow: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF8E2))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Info, null,
            tint     = Color(0xFFF59E0B),
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "No budget was set.",
            style  = MaterialTheme.typography.bodySmall,
            color  = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick      = {},
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        ) { Text("Not Now", fontSize = 11.sp, color = TextSecondary) }
        TextButton(
            onClick      = onSetNow,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        ) { Text("Set Now", fontSize = 11.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold) }
    }
}

// ── Quick tag chips ───────────────────────────────────────────────────────────
@Composable
private fun QuickTagChips(
    tags: List<String>,
    selectedTag: String?,
    accentColor: Color,
    onTagClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag ->
            val selected = tag == selectedTag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) accentColor else KeyBg)
                    .clickable { onTagClick(tag) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(
                    text       = tag,
                    fontSize   = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (selected) Color.White else TextSecondary,
                )
            }
        }
    }
}

// ── Yellow toolbar strip ──────────────────────────────────────────────────────
@Composable
private fun ToolbarStrip(
    items: List<ToolbarItem>,
    isContinuousInput: Boolean,
    onItemClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ToolbarBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items.forEach { item ->
            val isActive = item.key == "cont_input" && isContinuousInput
            Column(
                modifier            = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) PrimaryIndigo.copy(alpha = 0.10f) else Color.Transparent)
                    .clickable { onItemClick(item.key) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = item.label,
                    tint               = if (isActive) PrimaryIndigo else TextSecondary,
                    modifier           = Modifier.size(16.dp),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text      = item.label,
                    fontSize  = 9.sp,
                    color     = if (isActive) PrimaryIndigo else TextSecondary,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Numeric keypad ────────────────────────────────────────────────────────────
@Composable
private fun NumericKeypad(
    currencyLabel: String,
    onKey: (String) -> Unit,
    onConfirm: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    // Ensure we respect both bottom and horizontal navigation bar insets.
    val layoutDir = LocalLayoutDirection.current
    val navInsets = WindowInsets.navigationBars.asPaddingValues()
    val endNavPadding = navInsets.calculateEndPadding(layoutDir)

    Column(
        modifier            = modifier.padding(end = endNavPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        keyRows.forEach { rowKeys ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowKeys.forEach { key ->
                    val keyMod = if (key.isConfirm) Modifier
                        .weight(1f)
                        .padding(end = endNavPadding) else Modifier.weight(1f)
                    KeyButton(
                        keyDef        = key,
                        currencyLabel = currencyLabel,
                        isSaving      = isSaving,
                        onClick       = {
                            if (key.isConfirm) onConfirm()
                            else if (key.isCurrency) { /* currency picker TODO */ }
                            else onKey(key.label)
                        },
                        modifier      = keyMod,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    keyDef: KeyDef,
    currencyLabel: String,
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        keyDef.isConfirm   -> ConfirmYellow
        keyDef.isOperator  -> Color(0xFFE8E8F0)
        keyDef.isBackspace -> Color(0xFFE8E8F0)
        keyDef.isCurrency  -> KeyBg
        else               -> KeyBg
    }
    val contentColor = when {
        keyDef.isConfirm   -> Color(0xFF5C3700)
        keyDef.isOperator  -> PrimaryIndigo
        else               -> TextPrimary
    }

    Surface(
        modifier      = modifier
            .height(60.dp)
            .shadow(
                elevation = if (keyDef.isConfirm) 4.dp else 0.dp,
                shape = RoundedCornerShape(14.dp),
                clip = false
            ),
        shape         = RoundedCornerShape(14.dp),
        color         = bgColor,
        tonalElevation = 0.dp,
        onClick       = onClick,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            when {
                keyDef.isConfirm   -> {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color       = Color(0xFF5C3700),
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Rounded.Check, "Confirm",
                            tint     = Color(0xFF5C3700),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
                keyDef.isBackspace -> Icon(
                    Icons.Rounded.Backspace, "⌫",
                    tint     = TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
                keyDef.isCurrency  -> Text(
                    text       = currencyLabel,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextSecondary,
                )
                else -> Text(
                    text       = keyDef.label,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color      = contentColor,
                )
            }
        }
    }
}

// ── Account picker dialog ─────────────────────────────────────────────────────
@Composable
private fun AccountPickerDialog(
    accounts: List<AccountEntity>,
    selectedId: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Account", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        },
        text = {
            Column {
                accounts.forEach { acc ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelect(acc.id) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = acc.id == selectedId,
                            onClick  = { onSelect(acc.id) },
                            colors   = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo),
                        )
                        Spacer(Modifier.width(8.dp))
                        CategoryIcon(iconName = acc.iconName, bgColorHex = acc.colorHex, size = 30.dp, iconSize = 15.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(acc.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text(acc.type.name.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
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

// Previews
@Preview(showBackground = true)
@Composable
fun Preview_AddCategoryDataScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = SheetBg) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Close, "Close", tint = TextPrimary)
                    }
                    Text(
                        text = "Salary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Box(modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PastelIndigo), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.FormatListBulleted, contentDescription = null, tint = PrimaryIndigo)
                    }
                }

                Spacer(Modifier.height(6.dp))
                // Amount
                Text(text = CurrencyFormatter.format(3500.0, "LKR"), fontSize = 44.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp))

                Spacer(Modifier.height(8.dp))
                // Budget info row (budget set)
                BudgetInfoRow(
                    budget         = 5000.0,
                    spent          = 2500.0,
                    remaining      = 2500.0,
                    progress       = 0.5f,
                    currencySymbol = "LKR",
                    accentColor    = Color(0xFF43A047),
                    modifier       = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(12.dp))
                // Quick tags
                QuickTagChips(tags = listOf("Salary","Bonus","Freelance","Gift"), selectedTag = "Salary", accentColor = Color(0xFF43A047), onTagClick = {})

                Spacer(Modifier.height(12.dp))
                // Toolbar and keypad preview
                ToolbarStrip(items = toolbarItems, isContinuousInput = false, onItemClick = {})
                Spacer(Modifier.height(8.dp))
                NumericKeypad(currencyLabel = "LKR", onKey = {}, onConfirm = {}, isSaving = false, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp))
            }
        }
    }
}
