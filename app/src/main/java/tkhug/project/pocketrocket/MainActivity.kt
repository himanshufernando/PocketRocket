package tkhug.project.pocketrocket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import tkhug.project.pocketrocket.ui.screens.settings.SettingsViewModel
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import tkhug.project.pocketrocket.ui.navigation.AppNavGraph
import tkhug.project.pocketrocket.ui.theme.BackgroundSoft
import tkhug.project.pocketrocket.ui.theme.PocketRocketTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Obtain current app settings (theme mode) and pass to theme
            val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(this))
            val state by vm.uiState.collectAsStateWithLifecycle()

            PocketRocketTheme(themeMode = state.settings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = BackgroundSoft,
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
