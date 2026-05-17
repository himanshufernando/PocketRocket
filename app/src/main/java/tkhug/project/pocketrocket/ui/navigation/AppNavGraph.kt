package tkhug.project.pocketrocket.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import tkhug.project.pocketrocket.ui.components.FloatingBottomNav
import tkhug.project.pocketrocket.ui.screens.accounts.AccountsScreen
import tkhug.project.pocketrocket.ui.screens.addcategorydata.AddCategoryDataScreen
import tkhug.project.pocketrocket.ui.screens.budget.BudgetScreen
import tkhug.project.pocketrocket.ui.screens.categoryhistory.CategoryHistoryScreen
import tkhug.project.pocketrocket.ui.screens.home.HomeScreen
import tkhug.project.pocketrocket.ui.screens.settings.SettingsScreen
import tkhug.project.pocketrocket.ui.screens.transactions.TransactionsScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomNav = currentRoute in NavRoutes.bottomNavRoutes

    // Respect the OS navigation bar height so the floating nav sits above it.
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Bottom nav pill: 64dp height + 12dp top + 12dp bottom padding = 88dp, plus nav bar height.
    val bottomPadding = if (showBottomNav) 88.dp + navBarBottomPadding else 0.dp

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController    = navController,
            startDestination = NavRoutes.HOME,
            modifier         = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding),
            enterTransition  = { fadeIn() },
            exitTransition   = { fadeOut() },
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(navController = navController)
            }
            composable(NavRoutes.ACCOUNTS) {
                AccountsScreen(navController = navController)
            }
            composable(NavRoutes.TRANSACTIONS) {
                TransactionsScreen(navController = navController)
            }
            composable(NavRoutes.BUDGET) {
                BudgetScreen(navController = navController)
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen(navController = navController)
            }
            composable(NavRoutes.ADD_TRANSACTION) {
                // TODO: AddTransactionScreen
                Box(Modifier.fillMaxSize())
            }
            composable(
                route     = NavRoutes.EDIT_TRANSACTION,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { backStack ->
                val txId = backStack.arguments?.getLong("id") ?: -1L
                // Edit transaction screen (reuses add-like UI)
                tkhug.project.pocketrocket.ui.screens.edittransaction.EditTransactionScreen(
                    txId = txId,
                    navController = navController,
                )
            }
            composable(
                route     = NavRoutes.CATEGORY_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                // TODO: CategoryDetailScreen
                Box(Modifier.fillMaxSize())
            }
            composable(
                route     = NavRoutes.ACCOUNT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                // TODO: AccountDetailScreen
                Box(Modifier.fillMaxSize())
            }
            // ── Category tile flows ───────────────────────────────────────
            composable(
                route     = NavRoutes.ADD_CATEGORY_DATA,
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType }),
            ) { backStack ->
                val categoryId = backStack.arguments?.getLong("categoryId") ?: -1L
                AddCategoryDataScreen(
                    categoryId    = categoryId,
                    navController = navController,
                )
            }
            composable(
                route     = NavRoutes.CATEGORY_HISTORY,
                arguments = listOf(navArgument("categoryId") { type = NavType.LongType }),
            ) { backStack ->
                val categoryId = backStack.arguments?.getLong("categoryId") ?: -1L
                CategoryHistoryScreen(
                    categoryId    = categoryId,
                    navController = navController,
                )
            }
            // Settings sub-screens
            composable(NavRoutes.MANAGE_CATEGORIES) {
                tkhug.project.pocketrocket.ui.screens.settings.ManageCategoriesScreen(navController = navController)
            }
            composable(NavRoutes.RECURRING_TRANSACTIONS) {
                tkhug.project.pocketrocket.ui.screens.settings.RecurringTransactionsScreen(navController = navController)
            }
            composable(NavRoutes.BUDGET_SETTINGS) {
                tkhug.project.pocketrocket.ui.screens.settings.BudgetSettingsScreen(navController = navController)
            }
        }

        if (showBottomNav) {
            FloatingBottomNav(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                // navigationBarsPadding() lifts the nav pill above the OS nav buttons/gesture bar.
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}
