package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.db.dao.CategoryDao
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.model.TransactionType

class CategoryRepository(private val dao: CategoryDao) {

    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        dao.getCategoriesByType(type)

    suspend fun getCategoryById(id: Long): CategoryEntity? = dao.getCategoryById(id)

    suspend fun insertCategory(category: CategoryEntity): Long = dao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)
}

