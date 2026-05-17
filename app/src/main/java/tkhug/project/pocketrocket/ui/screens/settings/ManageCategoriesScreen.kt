package tkhug.project.pocketrocket.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import tkhug.project.pocketrocket.ui.screens.settings.CategoriesViewModel
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.model.TransactionType

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManageCategoriesScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.factory(context))
    val state by vm.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Rounded.Add, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundSoft,
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)) {
            item {
                Text("Income categories", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }
            items(state.income) { cat ->
                CategoryRow(category = cat, onDelete = { vm.deleteCategory(cat) })
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text("Expense categories", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }
            items(state.expense) { cat ->
                CategoryRow(category = cat, onDelete = { vm.deleteCategory(cat) })
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add category") },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                    Spacer(Modifier.height(8.dp))
                    Row { 
                        RadioButton(selected = addType == TransactionType.INCOME, onClick = { addType = TransactionType.INCOME })
                        Text("Income", Modifier.padding(start = 4.dp))
                        Spacer(Modifier.width(12.dp))
                        RadioButton(selected = addType == TransactionType.EXPENSE, onClick = { addType = TransactionType.EXPENSE })
                        Text("Expense", Modifier.padding(start = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        val cat = CategoryEntity(name = newName.trim(), type = addType, colorHex = "#9E9E9E", iconName = "label")
                        vm.addCategory(cat)
                    }
                    newName = ""
                    showAddDialog = false
                }) { Text("Add", color = PrimaryIndigo) }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = PrimaryIndigo) } },
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), color = SurfaceWhite, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(android.graphics.Color.parseColor(category.colorHex))))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, null, tint = Color.Red) }
        }
    }
}


