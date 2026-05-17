package tkhug.project.pocketrocket.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import tkhug.project.pocketrocket.data.model.TransactionType
import tkhug.project.pocketrocket.ui.theme.ExpenseCoral
import tkhug.project.pocketrocket.ui.theme.IncomeGreen
import tkhug.project.pocketrocket.ui.theme.TextPrimary
import tkhug.project.pocketrocket.ui.util.CurrencyFormatter

/**
 * Renders a formatted currency amount with automatic sign and colour.
 *
 * @param amount       Raw amount (always positive; sign derived from [type])
 * @param type         Determines colour and sign prefix
 * @param symbol       Currency symbol, defaults to "LKR"
 * @param showSign     Prepend "+"/"-" prefix
 */
@Composable
fun AmountText(
    amount: Double,
    type: TransactionType,
    modifier: Modifier = Modifier,
    symbol: String = "LKR",
    showSign: Boolean = false,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.SemiBold,
) {
    val color: Color = when (type) {
        TransactionType.INCOME  -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseCoral
    }
    val prefix = if (showSign) CurrencyFormatter.signPrefix(type == TransactionType.INCOME) else ""
    val formatted = CurrencyFormatter.format(amount, symbol)

    Text(
        text       = "$prefix$formatted",
        style      = style,
        fontWeight = fontWeight,
        color      = color,
        modifier   = modifier,
    )
}

/** Neutral amount (no colour coding, no sign). */
@Composable
fun NeutralAmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    symbol: String = "LKR",
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold,
) {
    Text(
        text       = CurrencyFormatter.format(amount, symbol),
        style      = style,
        fontWeight = fontWeight,
        color      = TextPrimary,
        modifier   = modifier,
    )
}

