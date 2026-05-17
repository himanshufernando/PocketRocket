package tkhug.project.pocketrocket.ui.screens.edittransaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.TransactionEntity
import tkhug.project.pocketrocket.data.repository.*
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class EditTransactionUiState(
    val isLoading: Boolean = true,
    val transaction: TransactionEntity? = null,
    val accountsCount: Int = 0,
    val amountText: String = "",
    val noteText: String = "",
    val selectedAccountId: Long = 0L,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class EditTransactionViewModel(
    private val transactionId: Long,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EditTransactionUiState())
    val state: StateFlow<EditTransactionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val tx = transactionRepo.getTransactionById(transactionId)
            _state.value = EditTransactionUiState(
                isLoading = false,
                transaction = tx,
                accountsCount = 0,
                amountText = tx?.amount?.toString() ?: "",
                noteText = tx?.note ?: "",
                selectedAccountId = tx?.accountId ?: 0L,
            )
        }
    }

    fun onAmountChanged(text: String) {
        _state.value = _state.value.copy(amountText = text, error = null)
    }

    fun onNoteChanged(text: String) { _state.value = _state.value.copy(noteText = text) }

    fun onAccountSelected(id: Long) { _state.value = _state.value.copy(selectedAccountId = id) }

    fun save() {
        val s = _state.value
        val tx = s.transaction ?: run { _state.value = s.copy(error = "Transaction not found"); return }
        val amount = s.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) { _state.value = s.copy(error = "Enter an amount") ; return }
        if (s.selectedAccountId == 0L) { _state.value = s.copy(error = "Select an account") ; return }

        viewModelScope.launch {
            _state.value = s.copy(isSaving = true)
            val updated = tx.copy(
                amount = amount,
                note = s.noteText.trim(),
                accountId = s.selectedAccountId,
                updatedAt = System.currentTimeMillis(),
            )
            transactionRepo.updateTransaction(updated)
            _state.value = s.copy(isSaving = false, saved = true)
        }
    }

    companion object {
        fun factory(context: Context, txId: Long) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            val db = app.database
            EditTransactionViewModel(
                transactionId = txId,
                transactionRepo = TransactionRepository(db.transactionDao()),
                accountRepo = AccountRepository(db.accountDao()),
            )
        }
    }
}

