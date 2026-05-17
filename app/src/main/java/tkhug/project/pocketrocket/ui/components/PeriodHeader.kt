package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tkhug.project.pocketrocket.ui.theme.PrimaryIndigo
import tkhug.project.pocketrocket.ui.theme.TextPrimary
import tkhug.project.pocketrocket.ui.theme.TextSecondary

/**
 * A reusable period selector header:
 *   [Monthly ▾]   [◀  04.23~05.22  ▶]   [actionIcon?]
 */
@Composable
fun PeriodHeader(
    periodLabel: String,
    dateRangeText: String,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onPeriodLabelClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Period dropdown ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .clickable { onPeriodLabelClick() }
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text       = periodLabel,
                style      = MaterialTheme.typography.labelLarge,
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector        = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Select period",
                tint               = TextSecondary,
                modifier           = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.weight(1f))

        // ── Date range navigator ────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousPeriod, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.ChevronLeft, "Previous",
                    tint = TextSecondary, modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text  = dateRangeText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
            )
            IconButton(onClick = onNextPeriod, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Rounded.ChevronRight, "Next",
                    tint = TextSecondary, modifier = Modifier.size(20.dp),
                )
            }
        }

        // ── Optional action icon ────────────────────────────────────────────
        if (actionIcon != null) {
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onActionClick, modifier = Modifier.size(32.dp)) {
                Icon(actionIcon, "Action", tint = PrimaryIndigo, modifier = Modifier.size(20.dp))
            }
        }
    }
}

