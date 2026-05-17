package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.theme.DividerColor
import tkhug.project.pocketrocket.ui.theme.SurfaceWhite
import tkhug.project.pocketrocket.ui.theme.TextPrimary
import tkhug.project.pocketrocket.ui.theme.TextSecondary
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter

/**
 * Card displaying a budget category with a progress bar.
 *
 * @param categoryName   Display name
 * @param iconName       Icon identifier (passed to [CategoryIcon])
 * @param colorHex       Category accent colour hex
 * @param spent          How much has been spent this period
 * @param budget         Total budget for the period (0 = unset)
 * @param currencySymbol e.g. "LKR"
 * @param onClick        Optional click handler
 */
@Composable
fun CategoryCard(
    categoryName: String,
    iconName: String,
    colorHex: String,
    spent: Double,
    budget: Double,
    currencySymbol: String = "LKR",
    type: TransactionType = TransactionType.EXPENSE,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val progress = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOverBudget = budget > 0 && spent > budget
    val accentColor = runCatching {
        Color(android.graphics.Color.parseColor(colorHex))
    }.getOrDefault(Color(0xFF9E9E9E))

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        onClick   = onClick ?: {},
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(iconName = iconName, bgColorHex = colorHex)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text       = categoryName,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = TextPrimary,
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text  = CurrencyFormatter.format(spent, currencySymbol),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverBudget) Color(0xFFEF5350) else accentColor,
                        )
                        if (budget > 0) {
                            Text(
                                text  = "/ ${CurrencyFormatter.format(budget, currencySymbol)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
                if (budget > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress          = { progress },
                        modifier          = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color             = if (isOverBudget) Color(0xFFEF5350) else accentColor,
                        trackColor        = accentColor.copy(alpha = 0.12f),
                    )
                }
            }
        }
    }
}

