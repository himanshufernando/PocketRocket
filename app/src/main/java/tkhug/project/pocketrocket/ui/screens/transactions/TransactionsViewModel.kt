package tkhug.project.pocketrocket.ui.screens.transactions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.model.TagEntity
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.screens.PeriodUiState
import tkhug.project.pocketrocket.ui.screens.TransactionDisplayItem
import tkhug.project.pocketrocket.ui.screens.toDisplayItem
import tkhug.project.pocketrocket.ui.util.DateFormatter
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

private data class FilterState(
    val tab: Int = 0,
    val categoryId: Long? = null,
    val tagName: String? = null,
)

/** 0 = All, 1 = Income, 2 = Expense */
data class TransactionsUiState(
    val isLoading: Boolean = true,
    val period: PeriodUiState = PeriodUiState(),
    val filterTab: Int = 0,
    val transactions: List<TransactionDisplayItem> = emptyList(),
    val currencySymbol: String = "LKR",
    val selectedCategoryId: Long? = null,
    val selectedTagName: String? = null,
    val availableCategories: List<CategoryEntity> = emptyList(),
    val availableTags: List<TagEntity> = emptyList(),
) {
    val hasActiveFilter: Boolean get() = selectedCategoryId != null || selectedTagName != null
}

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel(
    private val transactionRepo: TransactionRepository,
    private val categoryRepo: CategoryRepository,
    private val accountRepo: AccountRepository,
    private val settingsRepo: AppSettingsRepository,
    private val tagRepo: TagRepository,
) : ViewModel() {

    private val _period    = MutableStateFlow(PeriodUiState())
    private val _filterTab = MutableStateFlow(0)
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedTagName    = MutableStateFlow<String?>(null)
    private var financeStartDay: Int = 1
    private var financeEndDay: Int = 1

    private val _filterState: Flow<FilterState> = combine(
        _filterTab, _selectedCategoryId, _selectedTagName,
    ) { tab, catId, tagName -> FilterState(tab, catId, tagName) }

    private val _allTags: Flow<List<TagEntity>> = combine(
        tagRepo.getIncomeTags(), tagRepo.getExpenseTags(),
    ) { income, expense -> income + expense }

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
        _filterState,
        combine(settingsRepo.getSettings(), _allTags) { s, t -> s to t },
        categoryRepo.getAllCategories(),
        accountRepo.getAllAccounts(),
    ) { period, filter, settingsTags, cats, accs ->
        listOf(period, filter, settingsTags, cats, accs)
    }.flatMapLatest { args ->
        val period   = args[0] as PeriodUiState
        val filter   = args[1] as FilterState
        @Suppress("UNCHECKED_CAST")
        val settingsTags = args[2] as Pair<tkhug.project.pocketrocket.data.model.AppSettingsEntity, List<TagEntity>>
        @Suppress("UNCHECKED_CAST")
        val cats     = args[3] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val accs     = args[4] as List<tkhug.project.pocketrocket.data.model.AccountEntity>

        val settings = settingsTags.first
        val allTags  = settingsTags.second

        transactionRepo.getTransactionsByDateRange(period.startMillis, period.endMillis)
            .map { txList ->
                var filtered = when (filter.tab) {
                    1    -> txList.filter { it.type == TransactionType.INCOME }
                    2    -> txList.filter { it.type == TransactionType.EXPENSE }
                    else -> txList
                }
                if (filter.categoryId != null) {
                    filtered = filtered.filter { it.categoryId == filter.categoryId }
                }
                if (filter.tagName != null) {
                    filtered = filtered.filter { it.note.startsWith("[${filter.tagName}]") }
                }
                val catMap = cats.associateBy { it.id }
                val accMap = accs.associateBy { it.id }
                TransactionsUiState(
                    isLoading           = false,
                    period              = period,
                    filterTab           = filter.tab,
                    transactions        = filtered.map { it.toDisplayItem(catMap[it.categoryId], accMap[it.accountId]) },
                    currencySymbol      = settings.currencySymbol,
                    selectedCategoryId  = filter.categoryId,
                    selectedTagName     = filter.tagName,
                    availableCategories = cats,
                    availableTags       = allTags,
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun setFilterTab(tab: Int) { _filterTab.value = tab }
    fun setSelectedCategory(id: Long?) { _selectedCategoryId.value = id }
    fun setSelectedTag(name: String?) { _selectedTagName.value = name }
    fun clearFilters() {
        _selectedCategoryId.value = null
        _selectedTagName.value = null
    }

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
                tagRepo         = TagRepository(db.tagDao()),
            )
        }
    }
}

