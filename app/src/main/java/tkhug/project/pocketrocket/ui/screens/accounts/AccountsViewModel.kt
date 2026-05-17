package tkhug.project.pocketrocket.ui.screens.accounts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import tkhug.project.pocketrocket.data.model.AccountEntity
import tkhug.project.pocketrocket.data.repository.AccountRepository
import tkhug.project.pocketrocket.data.repository.AppSettingsRepository
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class AccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountEntity> = emptyList(),
    val totalBalance: Double = 0.0,
    val currencySymbol: String = "LKR",
)

class AccountsViewModel(
    private val accountRepo: AccountRepository,
    private val settingsRepo: AppSettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = combine(
        accountRepo.getAllAccounts(),
        accountRepo.getTotalBalance(),
        settingsRepo.getSettings(),
    ) { accounts, total, settings ->
        AccountsUiState(
            isLoading      = false,
            accounts       = accounts,
            totalBalance   = total ?: 0.0,
            currencySymbol = settings.currencySymbol,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    companion object {
        fun factory(context: Context) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            val db  = app.database
            AccountsViewModel(
                accountRepo  = AccountRepository(db.accountDao()),
                settingsRepo = AppSettingsRepository(db.appSettingsDao()),
            )
        }
    }
}

