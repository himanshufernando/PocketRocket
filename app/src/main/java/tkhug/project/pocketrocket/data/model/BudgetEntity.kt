package tkhug.project.pocketrocket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val periodType: PeriodType,
    val plannedAmount: Double,
    val startDate: Long,   // epoch millis – inclusive start
    val endDate: Long,     // epoch millis – inclusive end
)

