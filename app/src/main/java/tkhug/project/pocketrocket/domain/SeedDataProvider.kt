package tkhug.project.pocketrocket.domain

import tkhug.project.pocketrocket.data.db.dao.*
import tkhug.project.pocketrocket.data.model.*
import java.util.Calendar

/**
 * Provides demo seed data for first-launch database population.
 * All amounts are in LKR. Transactions are dated within the current month.
 */
object SeedDataProvider {

    suspend fun seed(
        categoryDao: CategoryDao,
        accountDao: AccountDao,
        transactionDao: TransactionDao,
        budgetDao: BudgetDao,
        settingsDao: AppSettingsDao,
    ) {
        // ── Default settings ────────────────────────────────────────────────
        settingsDao.insertSettings(AppSettingsEntity())

        // ── Categories ──────────────────────────────────────────────────────
        val categories = listOf(
            // Income
            CategoryEntity(id = 1,  name = "Salary",              type = TransactionType.INCOME,  colorHex = "#43A047", iconName = "payments",       sortOrder = 0),
            CategoryEntity(id = 2,  name = "Freelance",           type = TransactionType.INCOME,  colorHex = "#66BB6A", iconName = "laptop_mac",     sortOrder = 1),
            CategoryEntity(id = 3,  name = "Investment",          type = TransactionType.INCOME,  colorHex = "#26A69A", iconName = "trending_up",    sortOrder = 2),
            // Expense
            CategoryEntity(id = 4,  name = "Housing Loan",        type = TransactionType.EXPENSE, colorHex = "#EF5350", iconName = "home",           monthlyBudgetLimit = 59000.0,  sortOrder = 0),
            CategoryEntity(id = 5,  name = "Online Technical",    type = TransactionType.EXPENSE, colorHex = "#5C6BC0", iconName = "computer",       monthlyBudgetLimit = 15500.0,  sortOrder = 1),
            CategoryEntity(id = 6,  name = "School Fees",         type = TransactionType.EXPENSE, colorHex = "#FFA726", iconName = "school",         monthlyBudgetLimit = 12000.0,  sortOrder = 2),
            CategoryEntity(id = 7,  name = "iPhone Installment",  type = TransactionType.EXPENSE, colorHex = "#7E57C2", iconName = "phone_iphone",   monthlyBudgetLimit = 11550.0,  sortOrder = 3),
            CategoryEntity(id = 8,  name = "Ran kakulu",          type = TransactionType.EXPENSE, colorHex = "#26C6DA", iconName = "restaurant",     monthlyBudgetLimit = 5000.0,   sortOrder = 4),
            CategoryEntity(id = 9,  name = "Mutual Funds",        type = TransactionType.EXPENSE, colorHex = "#42A5F5", iconName = "account_balance",monthlyBudgetLimit = 5000.0,   sortOrder = 5),
            CategoryEntity(id = 10, name = "Stock",               type = TransactionType.EXPENSE, colorHex = "#29B6F6", iconName = "show_chart",     monthlyBudgetLimit = 5000.0,   sortOrder = 6),
            CategoryEntity(id = 11, name = "AIA",                 type = TransactionType.EXPENSE, colorHex = "#EC407A", iconName = "health_and_safety", monthlyBudgetLimit = 4000.0, sortOrder = 7),
            CategoryEntity(id = 12, name = "Entertainment",       type = TransactionType.EXPENSE, colorHex = "#FF7043", iconName = "movie",          monthlyBudgetLimit = 0.0,      sortOrder = 8),
            CategoryEntity(id = 13, name = "Other",               type = TransactionType.EXPENSE, colorHex = "#78909C", iconName = "more_horiz",     monthlyBudgetLimit = 10000.0,  sortOrder = 9),
            CategoryEntity(id = 14, name = "Credit Card",         type = TransactionType.EXPENSE, colorHex = "#8D6E63", iconName = "credit_card",    monthlyBudgetLimit = 0.0,      sortOrder = 10),
            CategoryEntity(id = 15, name = "Tech",                type = TransactionType.EXPENSE, colorHex = "#4DB6AC", iconName = "devices",        monthlyBudgetLimit = 0.0,      sortOrder = 11),
        )
        categoryDao.insertCategories(categories)

        // ── Accounts ────────────────────────────────────────────────────────
        val accounts = listOf(
            AccountEntity(id = 1, name = "Savings Account",   type = AccountType.BANK,        balance = 250000.0,  colorHex = "#42A5F5", iconName = "savings"),
            AccountEntity(id = 2, name = "Cash",              type = AccountType.CASH,        balance = 15000.0,   colorHex = "#66BB6A", iconName = "payments"),
            AccountEntity(id = 3, name = "Credit Card",       type = AccountType.CREDIT_CARD, balance = -45000.0,  colorHex = "#EF5350", iconName = "credit_card"),
            AccountEntity(id = 4, name = "Investment Acc.",   type = AccountType.INVESTMENT,  balance = 85000.0,   colorHex = "#7E57C2", iconName = "trending_up"),
        )
        accountDao.insertAccounts(accounts)

        // ── Transactions (current month) ─────────────────────────────────────
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        val monthStart = cal.timeInMillis

        fun dayTs(day: Int, hour: Int = 10): Long {
            return Calendar.getInstance().apply {
                timeInMillis = monthStart
                add(Calendar.DAY_OF_MONTH, day - 1)
                set(Calendar.HOUR_OF_DAY, hour)
            }.timeInMillis
        }



        // ── Budgets (current month) ──────────────────────────────────────────
        val monthEnd = Calendar.getInstance().apply {
            timeInMillis = monthStart
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }.timeInMillis

        val budgets = categories
            .filter { it.type == TransactionType.EXPENSE && (it.monthlyBudgetLimit ?: 0.0) > 0.0 }
            .map { cat ->
                BudgetEntity(
                    categoryId    = cat.id,
                    periodType    = PeriodType.MONTHLY,
                    plannedAmount = cat.monthlyBudgetLimit!!,
                    startDate     = monthStart,
                    endDate       = monthEnd,
                )
            }
        budgetDao.insertBudgets(budgets)
    }
}

