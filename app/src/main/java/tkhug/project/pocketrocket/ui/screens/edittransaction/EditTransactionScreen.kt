package tkhug.project.pocketrocket.ui.screens.edittransaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    txId: Long,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: EditTransactionViewModel = viewModel(
        key = "edit_$txId",
        factory = EditTransactionViewModel.factory(context, txId),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("Edit Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundSoft,
    ) { padding ->
        Column(modifier = modifier.padding(padding).padding(16.dp)) {
            Text("Amount", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            OutlinedTextField(
                value = state.amountText,
                onValueChange = vm::onAmountChanged,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("Note", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            OutlinedTextField(
                value = state.noteText,
                onValueChange = vm::onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.save() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)) {
                Icon(Icons.Rounded.Check, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Save", fontSize = 16.sp, color = Color.White)
            }
            state.error?.let { Text(it, color = ExpenseCoral, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}


