package tkhug.project.pocketrocket.ui.screens.budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.*
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.screens.PeriodUiState
import tkhug.project.pocketrocket.ui.util.DateFormatter
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class BudgetCategoryItem(
    val category: CategoryEntity,
    val budget: BudgetEntity?,
    val spent: Double,
)

data class BudgetUiState(
    val isLoading: Boolean = true,
    val period: PeriodUiState = PeriodUiState(),
    val items: List<BudgetCategoryItem> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val currencySymbol: String = "LKR",
)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val budgetRepo: BudgetRepository,
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository,
    private val settingsRepo: AppSettingsRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(PeriodUiState())
    private var financeStartDay: Int = 1
    private var financeEndDay: Int = 1

    init {
        viewModelScope.launch {
            settingsRepo.getSettings().collect { settings ->
                financeStartDay = settings.financeMonthStartDay
                financeEndDay = settings.financeMonthEndDay
                if (_period.value.label == "Monthly") {
                    val (baseStart, baseEnd) = DateFormatter.financeMonthRangeForNow(financeStartDay, financeEndDay)
                    val (s, e) = if (settings.financeMonthOffsetMonths != 0) {
                        DateFormatter.shiftFinanceMonth(baseStart, baseEnd, settings.financeMonthOffsetMonths, financeStartDay, financeEndDay)
                    } else {
                        baseStart to baseEnd
                    }
                    _period.value = _period.value.copy(startMillis = s, endMillis = e)
                }
            }
        }
    }

    val uiState: StateFlow<BudgetUiState> = combine(
        _period,
        settingsRepo.getSettings(),
        categoryRepo.getCategoriesByType(TransactionType.EXPENSE),
    ) { period, settings, categories -> Triple(period, settings, categories) }
        .flatMapLatest { (period, settings, categories) ->
            combine(
                budgetRepo.getActiveBudgetsAt(period.startMillis),
                budgetRepo.getLatestRecurringBudgetsUpTo(period.startMillis),
                transactionRepo.getTransactionsByDateRange(period.startMillis, period.endMillis),
            ) { periodBudgets, recurringBudgets, transactions ->
                // Period-specific budgets take priority; recurring ones fill in the rest
                val periodByCat    = periodBudgets.associateBy { it.categoryId }
                val recurringByCat = recurringBudgets.associateBy { it.categoryId }
                val budgetByCat    = recurringByCat + periodByCat

                val spentByCat = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.categoryId }
                    .mapValues { (_, list) -> list.sumOf { it.amount } }

                val items = categories.map { cat ->
                    BudgetCategoryItem(
                        category = cat,
                        budget   = budgetByCat[cat.id],
                        spent    = spentByCat[cat.id] ?: 0.0,
                    )
                }
                BudgetUiState(
                    isLoading      = false,
                    period         = period,
                    items          = items,
                    totalBudget    = items.sumOf { it.budget?.plannedAmount ?: 0.0 },
                    totalSpent     = items.sumOf { it.spent },
                    currencySymbol = settings.currencySymbol,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    fun previousPeriod() {
        val (s, e) = if (financeStartDay == 1) {
            DateFormatter.shiftMonth(_period.value.startMillis, _period.value.endMillis, -1)
        } else {
            DateFormatter.shiftFinanceMonth(_period.value.startMillis, _period.value.endMillis, -1, financeStartDay, financeEndDay)
        }
        _period.value = _period.value.copy(startMillis = s, endMillis = e)
    }

    fun nextPeriod() {
        val (s, e) = if (financeStartDay == 1) {
            DateFormatter.shiftMonth(_period.value.startMillis, _period.value.endMillis, 1)
        } else {
            DateFormatter.shiftFinanceMonth(_period.value.startMillis, _period.value.endMillis, 1, financeStartDay, financeEndDay)
        }
        _period.value = _period.value.copy(startMillis = s, endMillis = e)
    }

    companion object {
        fun factory(context: Context) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            val db  = app.database
            BudgetViewModel(
                budgetRepo      = BudgetRepository(db.budgetDao()),
                categoryRepo    = CategoryRepository(db.categoryDao()),
                transactionRepo = TransactionRepository(db.transactionDao()),
                settingsRepo    = AppSettingsRepository(db.appSettingsDao()),
            )
        }
    }

    // CRUD helpers for budgets
    fun createBudget(categoryId: Long, plannedAmount: Double, startMillis: Long, endMillis: Long) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                id = 0,
                categoryId = categoryId,
                periodType = PeriodType.CUSTOM,
                plannedAmount = plannedAmount,
                startDate = startMillis,
                endDate = endMillis,
                isRecurring = true,
            )
            budgetRepo.insertBudget(budget)
        }
    }

    fun updateBudget(budgetId: Long, plannedAmount: Double, startMillis: Long? = null, endMillis: Long? = null) {
        viewModelScope.launch {
            val existing = budgetRepo.getBudgetById(budgetId) ?: return@launch
            val updated = existing.copy(
                plannedAmount = plannedAmount,
                startDate = startMillis ?: existing.startDate,
                endDate = endMillis ?: existing.endDate,
            )
            budgetRepo.updateBudget(updated)
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            val existing = budgetRepo.getBudgetById(budgetId) ?: return@launch
            budgetRepo.deleteBudget(existing)
        }
    }
}
