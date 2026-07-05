package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.db.dao.BudgetDao
import tkhug.project.pocketrocket.data.model.BudgetEntity

class BudgetRepository(private val dao: BudgetDao) {

    fun getAllBudgets(): Flow<List<BudgetEntity>> = dao.getAllBudgets()

    fun getBudgetsByCategory(categoryId: Long): Flow<List<BudgetEntity>> =
        dao.getBudgetsByCategory(categoryId)

    fun getActiveBudgetsAt(timestamp: Long): Flow<List<BudgetEntity>> =
        dao.getActiveBudgetsAt(timestamp)

    fun getLatestRecurringBudgetsUpTo(timestamp: Long): Flow<List<BudgetEntity>> =
        dao.getLatestRecurringBudgetsUpTo(timestamp)

    suspend fun getBudgetById(id: Long): BudgetEntity? = dao.getBudgetById(id)

    suspend fun insertBudget(budget: BudgetEntity): Long = dao.insertBudget(budget)

    suspend fun updateBudget(budget: BudgetEntity) = dao.updateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) = dao.deleteBudget(budget)

    suspend fun insertBudgets(budgets: List<BudgetEntity>) = dao.insertBudgets(budgets)
}

