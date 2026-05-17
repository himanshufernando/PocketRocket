package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Circular icon badge used for categories.
 * [iconName] maps to a Material Icon; [bgColorHex] tints the circle background.
 */
@Composable
fun CategoryIcon(
    iconName: String,
    bgColorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    val bgColor = runCatching { Color(android.graphics.Color.parseColor(bgColorHex)) }
        .getOrDefault(Color(0xFFE0E0E0))
    val pastel = bgColor.copy(alpha = 0.18f)

    Box(
        modifier         = modifier
            .size(size)
            .clip(CircleShape)
            .background(pastel),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = iconForName(iconName),
            contentDescription = iconName,
            tint               = bgColor,
            modifier           = Modifier.size(iconSize),
        )
    }
}

/** Maps a string icon name to a [ImageVector] from Material Icons Rounded. */
fun iconForName(name: String): ImageVector = when (name) {
    "home"               -> Icons.Rounded.Home
    "payments"           -> Icons.Rounded.Payments
    "laptop_mac"         -> Icons.Rounded.LaptopMac
    "trending_up"        -> Icons.Rounded.TrendingUp
    "computer"           -> Icons.Rounded.Computer
    "school"             -> Icons.Rounded.School
    "phone_iphone"       -> Icons.Rounded.PhoneIphone
    "restaurant"         -> Icons.Rounded.Restaurant
    "account_balance"    -> Icons.Rounded.AccountBalance
    "show_chart"         -> Icons.Rounded.ShowChart
    "health_and_safety"  -> Icons.Rounded.HealthAndSafety
    "movie"              -> Icons.Rounded.Movie
    "more_horiz"         -> Icons.Rounded.MoreHoriz
    "credit_card"        -> Icons.Rounded.CreditCard
    "devices"            -> Icons.Rounded.Devices
    "savings"            -> Icons.Rounded.Savings
    "add"                -> Icons.Rounded.Add
    "settings"           -> Icons.Rounded.Settings
    "pie_chart"          -> Icons.Rounded.PieChart
    "receipt"            -> Icons.Rounded.Receipt
    else                 -> Icons.Rounded.Category
}

