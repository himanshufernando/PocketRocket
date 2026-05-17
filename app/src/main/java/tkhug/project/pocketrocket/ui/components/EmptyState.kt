package tkhug.project.pocketrocket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tkhug.project.pocketrocket.ui.theme.TextHint
import tkhug.project.pocketrocket.ui.theme.TextSecondary

/**
 * Full-area empty-state placeholder.
 */
@Composable
fun EmptyState(
    message: String = "Nothing here yet",
    subtitle: String? = null,
    icon: ImageVector = Icons.Rounded.Inbox,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = TextHint,
            modifier           = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = message,
            style      = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color      = TextSecondary,
            textAlign  = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = TextHint,
                textAlign = TextAlign.Center,
            )
        }
    }
}

