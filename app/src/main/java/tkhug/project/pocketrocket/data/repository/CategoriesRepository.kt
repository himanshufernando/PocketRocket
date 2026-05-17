package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.db.dao.CategoryDao
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.model.TransactionType

/**
 * Repository wrapper around CategoryDao.
 */
class CategoriesRepository(private val dao: CategoryDao) {

    fun getIncomeCategories(): Flow<List<CategoryEntity>> = dao.getCategoriesByType(TransactionType.INCOME)
    fun getExpenseCategories(): Flow<List<CategoryEntity>> = dao.getCategoriesByType(TransactionType.EXPENSE)

    suspend fun addCategory(category: CategoryEntity): Long = dao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)
}

