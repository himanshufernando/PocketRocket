package tkhug.project.pocketrocket.ui.screens.addcategorydata

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.AccountEntity
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.model.TransactionEntity
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.util.DateFormatter
import tkhug.project.pocketrocket.ui.util.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

data class AddCategoryDataUiState(
    val isLoading: Boolean = true,
    val category: CategoryEntity? = null,
    val accounts: List<AccountEntity> = emptyList(),
    val selectedAccountId: Long = 0L,
    // ── Keypad calculator state ──────────────────────────────────────────
    val displayText: String = "0",       // raw digits being typed e.g. "3500", "350.5"
    val leftOperand: Double? = null,     // stored left side for + / - operations
    val operator: String? = null,        // "+", "-", "×", "÷"
    // ── Extras ───────────────────────────────────────────────────────────
    val noteText: String = "",
    val selectedTag: String? = null,
    // ── Flags ────────────────────────────────────────────────────────────
    val continuousInput: Boolean = false,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val validationError: String? = null,
    val currencySymbol: String = "LKR",
    val expenseTags: List<String> = emptyList(),
    val incomeTags: List<String> = emptyList(),
    val categoryBudget: Double? = null,   // null = no budget set
    val categorySpent: Double = 0.0,      // spent in current finance period
) {
    /** True only for expense categories that have no budget record set. */
    val showBudgetBanner: Boolean
        get() = category?.type == TransactionType.EXPENSE && categoryBudget == null

    val budgetRemaining: Double
        get() = (categoryBudget ?: 0.0) - categorySpent

    val budgetProgress: Float
        get() = if ((categoryBudget ?: 0.0) > 0) (categorySpent / categoryBudget!!).coerceIn(0.0, 1.0).toFloat() else 0f


    /** Display string computed from raw digits - preserves trailing dot / decimals while typing. */
    val formattedAmount: String
        get() {
            if (displayText == "0") return "${currencySymbol}0"
            val dotIdx = displayText.indexOf('.')
            val intStr = if (dotIdx >= 0) displayText.substring(0, dotIdx) else displayText
            val decStr = if (dotIdx >= 0) displayText.substring(dotIdx) else ""
            val intFormatted = intStr.toLongOrNull()
                ?.let { NumberFormat.getNumberInstance(Locale.US).format(it) }
                ?: intStr
            return "$currencySymbol$intFormatted$decStr"
        }

    /** Expression label shown above amount when an operator is active e.g. "3,500 +" */
    val expressionLabel: String?
        get() {
            if (leftOperand == null || operator == null) return null
            val leftFmt = NumberFormat.getNumberInstance(Locale.US).format(leftOperand)
            return "$currencySymbol$leftFmt  $operator"
        }
}

class AddCategoryDataViewModel(
    private val categoryId: Long,
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val settingsRepo: AppSettingsRepository,
    private val tagRepo: TagRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddCategoryDataUiState())
    val state: StateFlow<AddCategoryDataUiState> = _state.asStateFlow()

    init {
        // One-shot category load
        viewModelScope.launch {
            val cat = categoryRepo.getCategoryById(categoryId)
            _state.update { it.copy(category = cat) }
        }
        // Reactive budget amount + current-period spending
        viewModelScope.launch {
            settingsRepo.getSettings().flatMapLatest { settings ->
                val (periodStart, periodEnd) = DateFormatter.financeMonthRangeForNow(
                    settings.financeMonthStartDay, settings.financeMonthEndDay
                )
                combine(
                    budgetRepo.getBudgetsByCategory(categoryId),
                    transactionRepo.getTransactionsByCategoryAndDateRange(categoryId, periodStart, periodEnd),
                ) { budgets, txns ->
                    // Prefer a budget whose range covers the period start; fall back to latest recurring
                    val active = budgets.firstOrNull { periodStart in it.startDate..it.endDate }
                        ?: budgets.filter { it.isRecurring && it.startDate <= periodStart }
                                  .maxByOrNull { it.startDate }
                    val spent = txns.sumOf { it.amount }
                    active?.plannedAmount to spent
                }
            }.collect { (budget, spent) ->
                _state.update { it.copy(categoryBudget = budget, categorySpent = spent) }
            }
        }
        // Reactive tags — two independent collectors so neither blocks the other
        viewModelScope.launch {
            tagRepo.getExpenseTags().collect { tags ->
                _state.update { it.copy(expenseTags = tags.map { t -> t.name }) }
            }
        }
        viewModelScope.launch {
            tagRepo.getIncomeTags().collect { tags ->
                _state.update { it.copy(incomeTags = tags.map { t -> t.name }) }
            }
        }
        // Reactive accounts + settings
        viewModelScope.launch {
            combine(accountRepo.getAllAccounts(), settingsRepo.getSettings()) { accs, settings ->
                accs to settings
            }.collect { (accs, settings) ->
                _state.update { cur ->
                    cur.copy(
                        isLoading         = false,
                        accounts          = accs,
                        selectedAccountId = if (cur.selectedAccountId == 0L)
                            accs.firstOrNull()?.id ?: 0L
                        else cur.selectedAccountId,
                        currencySymbol    = settings.currencySymbol,
                    )
                }
            }
        }
    }

    // ── Keypad ───────────────────────────────────────────────────────────────

    fun onKeyPress(key: String) {
        val s = _state.value
        _state.update { it.copy(validationError = null) }
        when (key) {
            "⌫" -> {
                val trimmed = s.displayText.dropLast(1)
                _state.update { it.copy(displayText = if (trimmed.isEmpty()) "0" else trimmed) }
            }
            "." -> {
                if (!s.displayText.contains("."))
                    _state.update { it.copy(displayText = s.displayText + ".") }
            }
            "+", "-", "×", "÷" -> {
                val left = s.displayText.toDoubleOrNull() ?: return
                _state.update { it.copy(leftOperand = left, operator = key, displayText = "0") }
            }
            else -> { // digit 0-9
                val raw = if (s.displayText == "0") key else s.displayText + key
                // max 12 significant digits
                if (raw.replace(".", "").length <= 12)
                    _state.update { it.copy(displayText = raw) }
            }
        }
    }

    // ── Form extras ──────────────────────────────────────────────────────────

    fun onNoteChanged(text: String) = _state.update { it.copy(noteText = text) }

    fun onTagSelected(tag: String) {
        val cur = _state.value.selectedTag
        _state.update { it.copy(selectedTag = if (cur == tag) null else tag) }
    }

    fun onAccountSelected(id: Long) = _state.update { it.copy(selectedAccountId = id) }

    fun toggleContinuousInput() = _state.update { it.copy(continuousInput = !it.continuousInput) }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun saveTransaction() {
        val s = _state.value
        val raw    = s.displayText.toDoubleOrNull() ?: 0.0
        val amount = if (s.leftOperand != null && s.operator != null) {
            when (s.operator) {
                "+"  -> s.leftOperand + raw
                "-"  -> (s.leftOperand - raw).coerceAtLeast(0.0)
                "×"  -> s.leftOperand * raw
                "÷"  -> if (raw != 0.0) s.leftOperand / raw else s.leftOperand
                else -> raw
            }
        } else raw

        val category = s.category
        if (category == null) { _state.update { it.copy(validationError = "Category not found.") }; return }
        if (amount <= 0)       { _state.update { it.copy(validationError = "Enter an amount first.") }; return }
        if (s.selectedAccountId == 0L) { _state.update { it.copy(validationError = "No account available.") }; return }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val now  = System.currentTimeMillis()
            val note = buildString {
                s.selectedTag?.let { append("[$it]  ") }
                append(s.noteText.trim())
            }.trim()
            transactionRepo.insertTransaction(
                TransactionEntity(
                    title      = category.name,
                    amount     = amount,
                    type       = category.type,
                    categoryId = category.id,
                    accountId  = s.selectedAccountId,
                    note       = note,
                    dateTime   = now,
                    createdAt  = now,
                    updatedAt  = now,
                )
            )
            _state.update {
                it.copy(
                    isSaving          = false,
                    savedSuccessfully = true,
                    displayText       = "0",
                    leftOperand       = null,
                    operator          = null,
                    noteText          = "",
                    selectedTag       = null,
                )
            }
        }
    }

    /** Call after Cont. Input save to clear the flag without navigating away. */
    fun resetAfterSave() = _state.update { it.copy(savedSuccessfully = false) }

    companion object {
        fun factory(context: Context, categoryId: Long) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            val db  = app.database
            AddCategoryDataViewModel(
                categoryId      = categoryId,
                categoryRepo    = CategoryRepository(db.categoryDao()),
                transactionRepo = TransactionRepository(db.transactionDao()),
                accountRepo     = AccountRepository(db.accountDao()),
                settingsRepo    = AppSettingsRepository(db.appSettingsDao()),
                tagRepo         = TagRepository(db.tagDao()),
                budgetRepo      = BudgetRepository(db.budgetDao()),
            )
        }
    }
}
