package tkhug.project.pocketrocket.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tkhug.project.pocketrocket.data.db.dao.AppSettingsDao
import tkhug.project.pocketrocket.data.model.AppSettingsEntity

class AppSettingsRepository(private val dao: AppSettingsDao) {

    private val defaults = AppSettingsEntity()

    fun getSettings(): Flow<AppSettingsEntity> =
        dao.getSettings().map { it ?: defaults }

    suspend fun getSettingsOnce(): AppSettingsEntity =
        dao.getSettingsOnce() ?: defaults

    suspend fun saveSettings(settings: AppSettingsEntity) = dao.insertSettings(settings)

    suspend fun updateCurrency(code: String, symbol: String) {
        val current = dao.getSettingsOnce() ?: defaults
        dao.insertSettings(current.copy(currencyCode = code, currencySymbol = symbol))
    }

    suspend fun updateThemeMode(mode: String) {
        val current = dao.getSettingsOnce() ?: defaults
        dao.insertSettings(current.copy(themeMode = mode))
    }

    suspend fun updateFinanceMonthStart(day: Int) {
        val current = dao.getSettingsOnce() ?: defaults
        // keep existing endDay if present; caller may prefer to set both via updateFinanceMonthRange
        dao.insertSettings(current.copy(financeMonthStartDay = day))
    }

    suspend fun updateFinanceMonthRange(startDay: Int, endDay: Int) {
        val current = dao.getSettingsOnce() ?: defaults
        dao.insertSettings(current.copy(financeMonthStartDay = startDay, financeMonthEndDay = endDay))
    }

    suspend fun incrementFinanceMonthOffset(delta: Int) {
        val current = dao.getSettingsOnce() ?: defaults
        dao.insertSettings(current.copy(financeMonthOffsetMonths = current.financeMonthOffsetMonths + delta))
    }

    suspend fun setFinanceMonthOffset(value: Int) {
        val current = dao.getSettingsOnce() ?: defaults
        dao.insertSettings(current.copy(financeMonthOffsetMonths = value))
    }
}

