package tkhug.project.pocketrocket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val colorHex: String,       // e.g. "#4CAF50"
    val iconName: String,       // e.g. "home", "salary"
    val monthlyBudgetLimit: Double? = null,
    val sortOrder: Int = 0,
)

