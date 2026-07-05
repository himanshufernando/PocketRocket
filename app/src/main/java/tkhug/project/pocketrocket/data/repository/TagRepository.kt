package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.db.dao.TagDao
import tkhug.project.pocketrocket.data.model.TagEntity
import tkhug.project.pocketrocket.data.model.TransactionType

class TagRepository(private val dao: TagDao) {

    fun getIncomeTags(): Flow<List<TagEntity>> = dao.getTagsByType(TransactionType.INCOME)
    fun getExpenseTags(): Flow<List<TagEntity>> = dao.getTagsByType(TransactionType.EXPENSE)

    suspend fun addTag(tag: TagEntity): Long = dao.insertTag(tag)
    suspend fun deleteTag(tag: TagEntity) = dao.deleteTag(tag)
}
