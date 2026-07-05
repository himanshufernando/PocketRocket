package tkhug.project.pocketrocket.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.model.BudgetEntity

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId ORDER BY startDate DESC")
    fun getBudgetsByCategory(categoryId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE :timestamp BETWEEN startDate AND endDate")
    fun getActiveBudgetsAt(timestamp: Long): Flow<List<BudgetEntity>>

    // Most recent recurring budget per category, for months that have no period-specific budget
    @Query("""
        SELECT b.* FROM budgets b
        INNER JOIN (
            SELECT categoryId, MAX(startDate) AS maxStart
            FROM budgets
            WHERE isRecurring = 1 AND startDate <= :timestamp
            GROUP BY categoryId
        ) latest ON b.categoryId = latest.categoryId AND b.startDate = latest.maxStart
        WHERE b.isRecurring = 1 AND b.startDate <= :timestamp
    """)
    fun getLatestRecurringBudgetsUpTo(timestamp: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun count(): Int
}

