package tkhug.project.pocketrocket.ui.screens

import tkhug.project.pocketrocket.data.model.TransactionEntity
import tkhug.project.pocketrocket.data.model.CategoryEntity
import tkhug.project.pocketrocket.data.model.AccountEntity
import tkhug.project.pocketrocket.ui.util.DateFormatter

/** Combined view of a transaction enriched with its category and account. */
data class TransactionDisplayItem(
    val id: Long,
    val title: String,
    val amount: Double,
    val type: tkhug.project.pocketrocket.data.model.TransactionType,
    val categoryName: String,
    val categoryColorHex: String,
    val categoryIconName: String,
    val accountName: String,
    val dateMillis: Long,
    val note: String,
)

fun TransactionEntity.toDisplayItem(
    category: CategoryEntity?,
    account: AccountEntity?,
): TransactionDisplayItem = TransactionDisplayItem(
    id               = id,
    title            = title,
    amount           = amount,
    type             = type,
    categoryName     = category?.name ?: "",
    categoryColorHex = category?.colorHex ?: "#9E9E9E",
    categoryIconName = category?.iconName ?: "more_horiz",
    accountName      = account?.name ?: "",
    dateMillis       = dateTime,
    note             = note,
)

/** Shared period state used across multiple screens. */
data class PeriodUiState(
    val label: String = "Monthly",
    val startMillis: Long = DateFormatter.currentMonthStart(),
    val endMillis: Long   = DateFormatter.currentMonthEnd(),
) {
    val dateRangeText: String
        get() = DateFormatter.formatPeriodRange(startMillis, endMillis)
}

