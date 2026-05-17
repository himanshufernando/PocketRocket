package tkhug.project.pocketrocket.ui.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats [amount] as a currency string, e.g. "LKR331,000" or "LKR-5,000".
     * Negative amounts are shown with a minus prefix.
     */
    fun format(amount: Double, symbol: String = "LKR"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
            isGroupingUsed = true
        }
        val abs = formatter.format(Math.abs(amount))
        return if (amount < 0) "$symbol-$abs" else "$symbol$abs"
    }

    /**
     * Formats with decimal places (for fractional amounts).
     */
    fun formatDecimal(amount: Double, symbol: String = "LKR"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
            isGroupingUsed = true
        }
        val abs = formatter.format(Math.abs(amount))
        return if (amount < 0) "$symbol-$abs" else "$symbol$abs"
    }

    /** Returns the sign prefix: "+" for income, "-" for expense. */
    fun signPrefix(isIncome: Boolean): String = if (isIncome) "+" else "-"
}

