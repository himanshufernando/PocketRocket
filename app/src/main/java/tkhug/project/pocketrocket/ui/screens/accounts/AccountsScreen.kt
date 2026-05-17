package tkhug.project.pocketrocket.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import tkhug.project.pocketrocket.data.model.AccountEntity
import tkhug.project.pocketrocket.data.model.AccountType
import tkhug.project.pocketrocket.ui.components.CategoryIcon
import tkhug.project.pocketrocket.ui.components.EmptyState
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter

@Composable
fun AccountsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val vm: AccountsViewModel = viewModel(factory = AccountsViewModel.factory(context))
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier            = modifier,
        containerColor      = BackgroundSoft,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { /* TODO: Add account */ },
                shape          = RoundedCornerShape(50),
                containerColor = PrimaryIndigo,
                contentColor   = Color.White,
            ) { Icon(Icons.Rounded.Add, "Add account") }
        },
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                // Header total
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryIndigo)
                        .padding(20.dp),
                ) {
                    Text("Net Worth", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(state.totalBalance, state.currencySymbol),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            item { Spacer(Modifier.height(4.dp)) }

            if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryIndigo) } }
            } else if (state.accounts.isEmpty()) {
                item { EmptyState(message = "No accounts yet", subtitle = "Tap + to add an account") }
            } else {
                val grouped = state.accounts.groupBy { it.type }
                AccountType.values().forEach { type ->
                    val group = grouped[type] ?: return@forEach
                    item {
                        Text(
                            text = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    items(group, key = { it.id }) { account ->
                        AccountCard(
                            account  = account,
                            symbol   = state.currencySymbol,
                            onClick  = { navController.navigate(NavRoutes.accountDetail(account.id)) },
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun AccountCard(
    account: AccountEntity,
    symbol: String,
    onClick: () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(iconName = account.iconName, bgColorHex = account.colorHex, size = 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(account.type.name.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            val balanceColor = if (account.balance >= 0) IncomeGreen else ExpenseCoral
            Text(
                text = CurrencyFormatter.format(account.balance, symbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor,
            )
        }
    }
}

