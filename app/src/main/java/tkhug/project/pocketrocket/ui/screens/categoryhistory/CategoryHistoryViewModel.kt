package tkhug.project.pocketrocket.ui.screens.categoryhistory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.screens.TransactionDisplayItem
import tkhug.project.pocketrocket.ui.screens.toDisplayItem
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class CategoryHistoryUiState(
    val isLoading: Boolean = true,
    val category: CategoryEntity? = null,
    val period: tkhug.project.pocketrocket.ui.screens.PeriodUiState = tkhug.project.pocketrocket.ui.screens.PeriodUiState(),
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val currencySymbol: String = "LKR",
)

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryHistoryViewModel(
    private val categoryId: Long,
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
    private val settingsRepo: AppSettingsRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(tkhug.project.pocketrocket.ui.screens.PeriodUiState())
    private var financeStartDay: Int = 1
    private var financeEndDay: Int = 1

    init {
        viewModelScope.launch {
            settingsRepo.getSettings().collect { settings ->
                financeStartDay = settings.financeMonthStartDay
                financeEndDay = settings.financeMonthEndDay
                if (_period.value.label == "Monthly") {
                    val (baseStart, baseEnd) = tkhug.project.pocketrocket.ui.util.DateFormatter.financeMonthRangeForNow(financeStartDay, financeEndDay)
                    val (s, e) = if (settings.financeMonthOffsetMonths != 0) {
                        tkhug.project.pocketrocket.ui.util.DateFormatter.shiftFinanceMonth(baseStart, baseEnd, settings.financeMonthOffsetMonths, financeStartDay, financeEndDay)
                    } else {
                        baseStart to baseEnd
                    }
                    _period.value = _period.value.copy(startMillis = s, endMillis = e)
                }
            }
        }
    }

    fun previousPeriod() {
        val (s, e) = if (financeStartDay == 1) {
            tkhug.project.pocketrocket.ui.util.DateFormatter.shiftMonth(_period.value.startMillis, _period.value.endMillis, -1)
        } else {
            tkhug.project.pocketrocket.ui.util.DateFormatter.shiftFinanceMonth(_period.value.startMillis, _period.value.endMillis, -1, financeStartDay, financeEndDay)
        }
        _period.value = _period.value.copy(startMillis = s, endMillis = e)
    }

    fun nextPeriod() {
        val (s, e) = if (financeStartDay == 1) {
            tkhug.project.pocketrocket.ui.util.DateFormatter.shiftMonth(_period.value.startMillis, _period.value.endMillis, 1)
        } else {
            tkhug.project.pocketrocket.ui.util.DateFormatter.shiftFinanceMonth(_period.value.startMillis, _period.value.endMillis, 1, financeStartDay, financeEndDay)
        }
        _period.value = _period.value.copy(startMillis = s, endMillis = e)
    }

    val state: StateFlow<CategoryHistoryUiState> = combine(
        _period,
        flow { emit(categoryRepo.getCategoryById(categoryId)) },
        accountRepo.getAllAccounts(),
        settingsRepo.getSettings(),
    ) { period, cat, accs, settings -> Quad(period, cat, accs, settings) }
        .flatMapLatest { (period, cat, accs, settings) ->
            transactionRepo.getTransactionsByDateRange(period.startMillis, period.endMillis)
                .map { allTx ->
                    val txList = allTx.filter { it.categoryId == categoryId }
                    val accMap = accs.associateBy { it.id }
                    val items = txList.map { it.toDisplayItem(cat, accMap[it.accountId]) }
                    CategoryHistoryUiState(
                        isLoading = false,
                        category = cat,
                        period = period,
                        transactions = items,
                        totalAmount = items.sumOf { it.amount },
                        currencySymbol = settings.currencySymbol,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryHistoryUiState())

    // small tuple helper
    private data class Quad<A,B,C,D>(val a:A, val b:B, val c:C, val d:D)

    companion object {
        fun factory(context: Context, categoryId: Long) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            val db  = app.database
            CategoryHistoryViewModel(
                categoryId      = categoryId,
                categoryRepo    = CategoryRepository(db.categoryDao()),
                transactionRepo = TransactionRepository(db.transactionDao()),
                accountRepo     = AccountRepository(db.accountDao()),
                settingsRepo    = AppSettingsRepository(db.appSettingsDao()),
            )
        }
    }
}

