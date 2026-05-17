package tkhug.project.pocketrocket.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.repository.CategoriesRepository
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class CategoriesUiState(
    val income: List<CategoryEntity> = emptyList(),
    val expense: List<CategoryEntity> = emptyList(),
)

class CategoriesViewModel(private val repo: CategoriesRepository) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = combine(
        repo.getIncomeCategories(),
        repo.getExpenseCategories(),
    ) { income, expense ->
        CategoriesUiState(income = income, expense = expense)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    fun addCategory(category: CategoryEntity) {
        viewModelScope.launch { repo.addCategory(category) }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repo.deleteCategory(category) }
    }

    companion object {
        fun factory(context: Context) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            CategoriesViewModel(CategoriesRepository(app.database.categoryDao()))
        }
    }
}



