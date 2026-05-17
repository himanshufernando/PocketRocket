package tkhug.project.pocketrocket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,                       // single-row table
    val currencyCode: String = "LKR",
    val currencySymbol: String = "LKR",
    val firstDayOfWeek: Int = 2,           // Calendar.MONDAY = 2
    // Finance month defaults: prefer a start in the later part of month (e.g. 23..22)
    val financeMonthStartDay: Int = 23,     // default start day
    val financeMonthEndDay: Int = 22,       // explicit end day
    val financeMonthOffsetMonths: Int = 0, // allow manual advances of the finance-month (0 = none)
    val themeMode: String = "SYSTEM",      // LIGHT | DARK | SYSTEM
)

