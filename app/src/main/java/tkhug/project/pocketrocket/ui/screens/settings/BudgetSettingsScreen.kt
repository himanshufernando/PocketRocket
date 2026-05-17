package tkhug.project.pocketrocket.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.theme.*

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BudgetSettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf("Monthly") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Defaults", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundSoft,
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            Text("Default budget period", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            RadioButtonGroup(options = listOf("Weekly", "Monthly", "Yearly"), selected = selected, onSelect = { selected = it })
            Spacer(Modifier.height(16.dp))
            Button(onClick = { /* TODO: save selection via repository */ }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun RadioButtonGroup(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        options.forEach { opt ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                RadioButton(selected = opt == selected, onClick = { onSelect(opt) })
                Spacer(Modifier.width(8.dp))
                Text(opt, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
        }
    }
}



