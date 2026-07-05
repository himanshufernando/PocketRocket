package tkhug.project.pocketrocket.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.TagEntity
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManageTagsScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: TagsViewModel = viewModel(factory = TagsViewModel.factory(context))
    val state by vm.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tags", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add tag")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
            )
        },
        containerColor = BackgroundSoft,
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            item {
                Text("Income tags", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }
            items(state.income) { tag ->
                TagRow(tag = tag, onDelete = { vm.deleteTag(tag) })
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text("Expense tags", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }
            items(state.expense) { tag ->
                TagRow(tag = tag, onDelete = { vm.deleteTag(tag) })
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add tag") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Tag name") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = addType == TransactionType.INCOME,
                            onClick = { addType = TransactionType.INCOME },
                        )
                        Text("Income", Modifier.padding(start = 4.dp))
                        Spacer(Modifier.width(12.dp))
                        RadioButton(
                            selected = addType == TransactionType.EXPENSE,
                            onClick = { addType = TransactionType.EXPENSE },
                        )
                        Text("Expense", Modifier.padding(start = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        vm.addTag(TagEntity(name = newName.trim(), type = addType))
                    }
                    newName = ""
                    showAddDialog = false
                }) { Text("Add", color = PrimaryIndigo) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = PrimaryIndigo) }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
private fun TagRow(tag: TagEntity, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete tag", tint = Color.Red)
            }
        }
    }
}
