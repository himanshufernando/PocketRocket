package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.theme.*
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter
import tkhug.project.pocketrocket.ui.util.DateFormatter

/**
 * A single transaction row showing icon, title, category, date, and amount.
 */
@Composable
fun TransactionRow(
    title: String,
    categoryName: String,
    categoryIconName: String,
    categoryColorHex: String,
    amount: Double,
    type: TransactionType,
    dateMillis: Long,
    currencySymbol: String = "LKR",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(iconName = categoryIconName, bgColorHex = categoryColorHex, size = 44.dp, iconSize = 22.dp)
        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary,
                maxLines   = 1,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(TextHint),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = DateFormatter.formatShortDate(dateMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(Modifier.width(8.dp))
        AmountText(
            amount     = amount,
            type       = type,
            symbol     = currencySymbol,
            showSign   = true,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

