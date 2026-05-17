package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import tkhug.project.pocketrocket.data.model.TransactionEntity

/**
 * Stub repository for recurring transactions. Replace with real DAO-backed implementation.
 */
class RecurringRepository {
    fun getRecurringTransactions(): Flow<List<TransactionEntity>> = flowOf(emptyList())

    suspend fun addRecurring(transaction: TransactionEntity) {
        // TODO: persist recurring transaction
    }

    suspend fun deleteRecurring(id: Long) {
        // TODO: delete recurring transaction
    }
}

