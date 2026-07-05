package tkhug.project.pocketrocket.ui.screens.transactions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.TransactionEntity
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.screens.PeriodUiState
import tkhug.project.pocketrocket.ui.screens.TransactionDisplayItem
import tkhug.project.pocketrocket.ui.screens.toDisplayItem
import tkhug.project.pocketrocket.ui.util.DateFormatter
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

/** 0 = All, 1 = Income, 2 = Expense */
data class TransactionsUiState(
    val isLoading: Boolean = true,
    val period: PeriodUiState = PeriodUiState(),
    val filterTab: Int = 0,
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val currencySymbol: String = "LKR",
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: AccountRepository,
    private val settingsRepo: AppSettingsRepository,
) : ViewModel() {

    private val _period    = MutableStateFlow(PeriodUiState())
    private val _filterTab = MutableStateFlow(0)
    private var financeStartDay: Int = 1
    private var financeEndDay: Int = 1

    init {
        // keep local copy of finance month start day and ensure monthly period matches setting
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

    val uiState: StateFlow<TransactionsUiState> = combine(
        _period,
        _filterTab,
        settingsRepo.getSettings(),
        categoryRepo.getAllCategories(),
        accountRepo.getAllAccounts(),
    ) { period, tab, settings, cats, accs ->
        listOf(period, tab, settings, cats, accs)   // bridge for flatMapLatest
    }.flatMapLatest { args ->
        val period   = args[0] as PeriodUiState
        val tab      = args[1] as Int
        @Suppress("UNCHECKED_CAST")
        val cats     = args[3] as List<tkhug.project.pocketrocket.data.model.CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val accs     = args[4] as List<tkhug.project.pocketrocket.data.model.AccountEntity>
        val settings = args[2] as tkhug.project.pocketrocket.data.model.AppSettingsEntity

        transactionRepo.getTransactionsByDateRange(period.startMillis, period.endMillis)
            .map { txList ->
                val filtered = when (tab) {
                    1    -> txList.filter { it.type == TransactionType.INCOME }
                    2    -> txList.filter { it.type == TransactionType.EXPENSE }
                    else -> txList
                }
                val catMap = cats.associateBy { it.id }
                val accMap = accs.associateBy { it.id }
                TransactionsUiState(
                    isLoading      = false,
                    period         = period,
                    filterTab      = tab,
                    transactions   = filtered.map { it.toDisplayItem(catMap[it.categoryId], accMap[it.accountId]) },
                    currencySymbol = settings.currencySymbol,
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun setFilterTab(tab: Int) { _filterTab.value = tab }

    fun previousPeriod() {
        val (s, e) = if (financeStartDay == 1) {
            DateFormatter.shiftMonth(_period.value.startMillis, _period.value.endMillis, -1)
        } else {
            DateFormatter.shiftFinanceMonth(_period.value.startMillis, _period.value.endMillis, -1, financeStartDay, financeEndDay)
        }
        _period.value = _period.value.copy(startMillis = s, endMillis = e)
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
                transactionRepo.deleteTransactionById(id)
        }
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
            TransactionsViewModel(
                transactionRepo = TransactionRepository(db.transactionDao()),
                categoryRepo    = CategoryRepository(db.categoryDao()),
                accountRepo     = AccountRepository(db.accountDao()),
                settingsRepo    = AppSettingsRepository(db.appSettingsDao()),
            )
        }
    }
}

