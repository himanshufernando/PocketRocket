package tkhug.project.pocketrocket.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.screens.PeriodUiState
import tkhug.project.pocketrocket.ui.util.DateFormatter
import tkhug.project.pocketrocket.ui.util.ViewModelFactory
import java.util.Calendar

/** Per-category spending summary shown in the Home grid. */
data class CategorySpendItem(
    val categoryId: Long,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val spentAmount: Double,
    val type: TransactionType,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val period: PeriodUiState = PeriodUiState(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenseCategories: List<CategorySpendItem> = emptyList(),
    val incomeCategories: List<CategorySpendItem> = emptyList(),
    val currencySymbol: String = "LKR",
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val settingsRepo: AppSettingsRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(PeriodUiState())
    private var financeStartDay: Int = 1
    private var financeEndDay: Int = 1

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // keep local finance month start and initialize monthly period accordingly
        viewModelScope.launch {
            settingsRepo.getSettings().collect { settings ->
                financeStartDay = settings.financeMonthStartDay
                financeEndDay = settings.financeMonthEndDay
                if (_period.value.label == "Monthly") {
                    // compute base finance-month for "now" and apply any manual offset stored in settings
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
        combine(
            _period,
            settingsRepo.getSettings(),
            categoryRepo.getAllCategories(),
        ) { period, settings, categories ->
            Triple(period, settings, categories)
        }.flatMapLatest { (period, settings, categories) ->
            transactionRepo.getTransactionsByDateRange(period.startMillis, period.endMillis)
                .map { txList ->
                    val expSums = txList
                        .filter { it.type == TransactionType.EXPENSE }
                        .groupBy { it.categoryId }
                        .mapValues { (_, l) -> l.sumOf { it.amount } }

                    val incSums = txList
                        .filter { it.type == TransactionType.INCOME }
                        .groupBy { it.categoryId }
                        .mapValues { (_, l) -> l.sumOf { it.amount } }

                    val expCats = categories
                        .filter { it.type == TransactionType.EXPENSE }
                        .sortedBy { it.sortOrder }
                        .map { c -> CategorySpendItem(c.id, c.name, c.iconName, c.colorHex, expSums[c.id] ?: 0.0, TransactionType.EXPENSE) }

                    val incCats = categories
                        .filter { it.type == TransactionType.INCOME }
                        .sortedBy { it.sortOrder }
                        .map { c -> CategorySpendItem(c.id, c.name, c.iconName, c.colorHex, incSums[c.id] ?: 0.0, TransactionType.INCOME) }

                    HomeUiState(
                        isLoading          = false,
                        period             = period,
                        totalIncome        = incCats.sumOf { it.spentAmount },
                        totalExpense       = expCats.sumOf { it.spentAmount },
                        expenseCategories  = expCats,
                        incomeCategories   = incCats,
                        currencySymbol     = settings.currencySymbol,
                    )
                }
        }.onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun previousPeriod() {
        val cur = _period.value
        val shifted = shiftPeriod(cur, -1)
        _period.value = shifted
    }

    fun nextPeriod() {
        val cur = _period.value
        val shifted = shiftPeriod(cur, +1)
        _period.value = shifted
    }

    fun setPeriodMode(mode: String) {
        _period.value = buildPeriod(mode)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildPeriod(mode: String): PeriodUiState {
        return when (mode) {
            "Daily" -> {
                val s = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
                }.timeInMillis
                PeriodUiState(label = mode, startMillis = s, endMillis = e)
            }
            "Weekly" -> {
                val s = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val e = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    add(Calendar.DATE, 6)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
                }.timeInMillis
                PeriodUiState(label = mode, startMillis = s, endMillis = e)
            }
            else -> PeriodUiState(
                label       = "Monthly",
                startMillis = DateFormatter.financeMonthRangeForNow(financeStartDay, financeEndDay).first,
                endMillis   = DateFormatter.financeMonthRangeForNow(financeStartDay, financeEndDay).second,
            )
        }
    }

    private fun shiftPeriod(cur: PeriodUiState, delta: Int): PeriodUiState {
        return when (cur.label) {
            "Daily" -> {
                val s = Calendar.getInstance().apply { timeInMillis = cur.startMillis; add(Calendar.DATE, delta) }.timeInMillis
                val e = Calendar.getInstance().apply { timeInMillis = cur.endMillis;   add(Calendar.DATE, delta) }.timeInMillis
                cur.copy(startMillis = s, endMillis = e)
            }
            "Weekly" -> {
                val s = Calendar.getInstance().apply { timeInMillis = cur.startMillis; add(Calendar.WEEK_OF_YEAR, delta) }.timeInMillis
                val e = Calendar.getInstance().apply { timeInMillis = cur.endMillis;   add(Calendar.WEEK_OF_YEAR, delta) }.timeInMillis
                cur.copy(startMillis = s, endMillis = e)
            }
            else -> {
                val (s, e) = if (financeStartDay == 1) {
                    DateFormatter.shiftMonth(cur.startMillis, cur.endMillis, delta)
                } else {
                    DateFormatter.shiftFinanceMonth(cur.startMillis, cur.endMillis, delta, financeStartDay, financeEndDay)
                }
                cur.copy(startMillis = s, endMillis = e)
            }
        }
    }

    companion object {
        fun factory(context: Context) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            val db  = app.database
            HomeViewModel(
                transactionRepo = TransactionRepository(db.transactionDao()),
                categoryRepo    = CategoryRepository(db.categoryDao()),
                settingsRepo    = AppSettingsRepository(db.appSettingsDao()),
            )
        }
    }
}
