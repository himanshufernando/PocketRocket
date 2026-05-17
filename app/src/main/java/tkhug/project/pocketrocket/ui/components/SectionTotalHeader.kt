package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.theme.TextPrimary
import tkhug.project.pocketrocket.ui.theme.TextSecondary

/**
 * Section header for a group of transactions.
 * Example:  "Today"                               "LKR331,000"
 *           "2 transactions"
 */
@Composable
fun SectionTotalHeader(
    title: String,
    subtitle: String? = null,
    total: Double? = null,
    totalType: TransactionType = TransactionType.EXPENSE,
    currencySymbol: String = "LKR",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
            )
            if (subtitle != null) {
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        if (total != null) {
            AmountText(
                amount     = total,
                type       = totalType,
                symbol     = currencySymbol,
                showSign   = true,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

