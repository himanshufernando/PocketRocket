package tkhug.project.pocketrocket.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.AppSettingsEntity
import tkhug.project.pocketrocket.data.model.BudgetEntity
import tkhug.project.pocketrocket.data.repository.AppSettingsRepository
import tkhug.project.pocketrocket.data.repository.BudgetRepository
import tkhug.project.pocketrocket.data.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: AppSettingsEntity = AppSettingsEntity(),
)

class SettingsViewModel(
    private val settingsRepo: AppSettingsRepository,
    private val categoryRepo: CategoryRepository,
    private val budgetRepo: BudgetRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepo
        .getSettings()
        .map { SettingsUiState(isLoading = false, settings = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun updateCurrency(code: String, symbol: String) {
        viewModelScope.launch { settingsRepo.updateCurrency(code, symbol) }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch { settingsRepo.updateThemeMode(mode) }
    }

    fun updateFirstDayOfWeek(day: Int) {
        viewModelScope.launch {
            val current = settingsRepo.getSettingsOnce()
            settingsRepo.saveSettings(current.copy(firstDayOfWeek = day))
        }
    }

    fun updateFinanceMonthStart(day: Int) {
        viewModelScope.launch { settingsRepo.updateFinanceMonthStart(day) }
    }

    fun updateFinanceMonthRange(startDay: Int, endDay: Int) {
        viewModelScope.launch { settingsRepo.updateFinanceMonthRange(startDay, endDay) }
    }

    /**
     * Close the current finance month and create budgets for the next finance month
     * based on category monthlyBudgetLimit values.
     */
    fun closeFinanceMonth() {
        viewModelScope.launch {
            val settings = settingsRepo.getSettingsOnce()
            val startDay = settings.financeMonthStartDay
            val endDay   = settings.financeMonthEndDay
            // compute next finance month range using explicit end day
            val (curStart, curEnd) = tkhug.project.pocketrocket.ui.util.DateFormatter.financeMonthRangeForNow(startDay, endDay)
            val (nextStart, nextEnd) = tkhug.project.pocketrocket.ui.util.DateFormatter.shiftFinanceMonth(curStart, curEnd, 1, startDay, endDay)

            // create budgets for next month from categories that have monthlyBudgetLimit > 0
            val cats = categoryRepo.getAllCategories().first()
            val budgets = cats
                .filter { it.type == tkhug.project.pocketrocket.data.model.TransactionType.EXPENSE && (it.monthlyBudgetLimit ?: 0.0) > 0.0 }
                .map { cat ->
                    BudgetEntity(
                        categoryId    = cat.id,
                        periodType    = tkhug.project.pocketrocket.data.model.PeriodType.MONTHLY,
                        plannedAmount = cat.monthlyBudgetLimit!!,
                        startDate     = nextStart,
                        endDate       = nextEnd,
                    )
                }

            if (budgets.isNotEmpty()) {
                budgetRepo.insertBudgets(budgets)
            }
            // advance the app's finance-month anchor so the UI and other screens reflect the new month
            settingsRepo.incrementFinanceMonthOffset(1)
        }
    }

    companion object {
        fun factory(context: Context) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            SettingsViewModel(
                AppSettingsRepository(app.database.appSettingsDao()),
                CategoryRepository(app.database.categoryDao()),
                BudgetRepository(app.database.budgetDao()),
            )
        }
    }
}

