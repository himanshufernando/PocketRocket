package tkhug.project.pocketrocket.ui.navigation

/**
 * All in-app navigation routes.
 * String constants are used to avoid sealed-class per-instance allocation while
 * keeping route definitions in one place.
 */
object NavRoutes {
    const val HOME         = "home"
    const val ACCOUNTS     = "accounts"
    const val TRANSACTIONS = "transactions"
    const val BUDGET       = "budget"
    const val SETTINGS     = "settings"
    const val MANAGE_CATEGORIES = "manage_categories"
    const val MANAGE_TAGS        = "manage_tags"
    const val RECURRING_TRANSACTIONS = "recurring_transactions"
    const val BUDGET_SETTINGS = "budget_settings"

    const val ADD_TRANSACTION    = "add_transaction"
    const val EDIT_TRANSACTION   = "edit_transaction/{id}"
    const val CATEGORY_DETAIL    = "category_detail/{id}"
    const val ACCOUNT_DETAIL     = "account_detail/{id}"

    // ── New category-tile flows ──────────────────────────────────────
    const val ADD_CATEGORY_DATA  = "addCategoryData/{categoryId}"
    const val CATEGORY_HISTORY   = "categoryHistory/{categoryId}"

    fun editTransaction(id: Long)     = "edit_transaction/$id"
    fun categoryDetail(id: Long)      = "category_detail/$id"
    fun accountDetail(id: Long)       = "account_detail/$id"
    fun addCategoryData(id: Long)     = "addCategoryData/$id"
    fun categoryHistory(id: Long)     = "categoryHistory/$id"

    /** Routes that show the floating bottom nav bar */
    val bottomNavRoutes = setOf(HOME, ACCOUNTS, TRANSACTIONS, BUDGET, SETTINGS)
}

