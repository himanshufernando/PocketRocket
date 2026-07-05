package tkhug.project.pocketrocket.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.model.TagEntity
import tkhug.project.pocketrocket.data.repository.TagRepository
import tkhug.project.pocketrocket.ui.util.ViewModelFactory

data class TagsUiState(
    val income: List<TagEntity> = emptyList(),
    val expense: List<TagEntity> = emptyList(),
)

class TagsViewModel(private val repo: TagRepository) : ViewModel() {

    val uiState: StateFlow<TagsUiState> = combine(
        repo.getIncomeTags(),
        repo.getExpenseTags(),
    ) { income, expense ->
        TagsUiState(income = income, expense = expense)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TagsUiState())

    fun addTag(tag: TagEntity) {
        viewModelScope.launch { repo.addTag(tag) }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch { repo.deleteTag(tag) }
    }

    companion object {
        fun factory(context: Context) = ViewModelFactory {
            val app = context.applicationContext as tkhug.project.pocketrocket.PocketRocketApp
            TagsViewModel(TagRepository(app.database.tagDao()))
        }
    }
}
