package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.db.dao.TransactionDao
import tkhug.project.pocketrocket.data.model.TransactionEntity
import tkhug.project.pocketrocket.data.model.TransactionType

class TransactionRepository(private val dao: TransactionDao) {

    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsByDateRange(start, end)

    fun getTransactionsByTypeAndDateRange(
        type: TransactionType, start: Long, end: Long
    ): Flow<List<TransactionEntity>> =
        dao.getTransactionsByTypeAndDateRange(type, start, end)

    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsByCategory(categoryId)

    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsByAccount(accountId)

    suspend fun getTransactionById(id: Long): TransactionEntity? = dao.getTransactionById(id)

    suspend fun sumByTypeAndDateRange(type: TransactionType, start: Long, end: Long): Double =
        dao.sumByTypeAndDateRange(type, start, end) ?: 0.0

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        dao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        dao.deleteTransaction(transaction)
}

