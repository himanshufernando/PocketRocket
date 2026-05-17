package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tkhug.project.pocketrocket.ui.navigation.NavRoutes
import tkhug.project.pocketrocket.ui.theme.*

private data class NavTabDef(
    val route: String,
    val icon: ImageVector,
    val label: String,
)

private val navTabs = listOf(
    NavTabDef(NavRoutes.HOME,         Icons.Rounded.Home,           "Home"),
    NavTabDef(NavRoutes.ACCOUNTS,     Icons.Rounded.AccountBalance, "Accounts"),
    NavTabDef(NavRoutes.TRANSACTIONS, Icons.Rounded.Receipt,        "Transactions"),
    NavTabDef(NavRoutes.BUDGET,       Icons.Rounded.PieChart,       "Budget"),
    NavTabDef(NavRoutes.SETTINGS,     Icons.Rounded.Settings,       "Settings"),
)

/**
 * Floating pill-shaped bottom navigation bar.
 * Selected tab shows a soft indigo oval highlight, coloured icon, and bold label.
 * Unselected tabs show a grey icon with no highlight.
 */
@Composable
fun FloatingBottomNav(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Surface(
            modifier      = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), clip = false),
            shape         = RoundedCornerShape(28.dp),
            color         = SurfaceWhite,
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                navTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavItem(
                        tab        = tab,
                        isSelected = selected,
                        onClick    = { onTabSelected(tab.route) },
                        modifier   = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: NavTabDef,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isSelected) NavHighlight else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = tab.icon,
                contentDescription = tab.label,
                tint               = if (isSelected) NavSelected else NavUnselected,
                modifier           = Modifier.size(22.dp),
            )
        }
        if (isSelected) {
            Spacer(Modifier.height(2.dp))
            Text(
                text       = tab.label,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                color      = NavSelected,
            )
        }
    }
}

