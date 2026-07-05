package tkhug.project.pocketrocket.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tkhug.project.pocketrocket.data.db.dao.*
import tkhug.project.pocketrocket.data.model.*
import tkhug.project.pocketrocket.domain.SeedDataProvider

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        AppSettingsEntity::class,
        tkhug.project.pocketrocket.data.model.TagEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun tagDao(): tkhug.project.pocketrocket.data.db.dao.TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_rocket_db",
                )
                    // Migrations: 1->2 and 2->3
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(SeedCallback(scope))
                    .build()
                    .also { INSTANCE = it }
            }

        // ── Seed on first creation ──────────────────────────────────────────
        private class SeedCallback(
            private val scope: CoroutineScope,
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        SeedDataProvider.seed(
                            categoryDao    = database.categoryDao(),
                            accountDao     = database.accountDao(),
                            transactionDao = database.transactionDao(),
                            budgetDao      = database.budgetDao(),
                            settingsDao    = database.appSettingsDao(),
                            tagDao         = database.tagDao(),
                        )
                    }
                }
            }
        }
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // add new column with default 0 for existing rows
                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN financeMonthOffsetMonths INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // All existing budgets are treated as recurring by default
                database.execSQL(
                    "ALTER TABLE budgets ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `tags` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

        // Add financeMonthEndDay column and initialize it based on the start day
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new column with a default value
                database.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN financeMonthEndDay INTEGER NOT NULL DEFAULT 0"
                )
                // Initialize end day. Some older databases might not have the financeMonthStartDay column
                // (depending on prior app versions). Check table info at runtime and update accordingly.
                var hasStartColumn = false
                val cursor = database.query("PRAGMA table_info(app_settings)")
                cursor.use {
                    while (it.moveToNext()) {
                        // PRAGMA table_info returns columns: cid, name, type, notnull, dflt_value, pk
                        val colNameIndex = it.getColumnIndex("name")
                        if (colNameIndex >= 0) {
                            val name = it.getString(colNameIndex)
                            if (name == "financeMonthStartDay") {
                                hasStartColumn = true
                                break
                            }
                        }
                    }
                }

                if (hasStartColumn) {
                    // Initialize end day based on existing start day
                    database.execSQL(
                        "UPDATE app_settings SET financeMonthEndDay = CASE WHEN financeMonthStartDay > 1 THEN financeMonthStartDay - 1 ELSE 1 END"
                    )
                } else {
                    // Fallback default when start day unavailable
                    database.execSQL(
                        "UPDATE app_settings SET financeMonthEndDay = 22"
                    )
                }
            }
        }
    }
}

