package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import tkhug.project.pocketrocket.data.db.dao.AccountDao
import tkhug.project.pocketrocket.data.model.AccountEntity

class AccountRepository(private val dao: AccountDao) {

    fun getAllAccounts(): Flow<List<AccountEntity>> = dao.getAllAccounts()

    fun getTotalBalance(): Flow<Double?> = dao.getTotalBalance()

    suspend fun getAccountById(id: Long): AccountEntity? = dao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity): Long = dao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = dao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = dao.deleteAccount(account)
}

