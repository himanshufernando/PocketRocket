package tkhug.project.pocketrocket.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.model.TagEntity
import tkhug.project.pocketrocket.data.model.TransactionType

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE type = :type ORDER BY sortOrder, name")
    fun getTagsByType(type: TransactionType): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun count(): Int
}