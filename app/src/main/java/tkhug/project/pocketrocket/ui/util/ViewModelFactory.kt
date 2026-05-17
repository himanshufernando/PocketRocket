package tkhug.project.pocketrocket.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Generic single-creator [ViewModelProvider.Factory].
 * Usage: viewModel(factory = ViewModelFactory { MyViewModel(repo) })
 */
class ViewModelFactory<T : ViewModel>(
    private val creator: () -> T,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}

