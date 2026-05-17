package tkhug.project.pocketrocket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppLightColorScheme = lightColorScheme(
    primary          = PrimaryIndigo,
    onPrimary        = Color.White,
    primaryContainer = PastelIndigo,
    onPrimaryContainer = PrimaryIndigoDark,
    secondary        = PrimaryIndigoLight,
    onSecondary      = Color.White,
    secondaryContainer = PastelPurple,
    onSecondaryContainer = PrimaryIndigoDark,
    background       = BackgroundSoft,
    onBackground     = TextPrimary,
    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = BackgroundSoft,
    onSurfaceVariant = TextSecondary,
    outline          = DividerColor,
    error            = ExpenseCoral,
    onError          = Color.White,
)

private val AppDarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    background = Color(0xFF0F1720),
    onBackground = Color.White,
    surface = Color(0xFF111827),
    onSurface = Color.White,
)

@Composable
fun PocketRocketTheme(
    themeMode: String = "SYSTEM", // "LIGHT" | "DARK" | "SYSTEM"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode.uppercase()) {
        "LIGHT" -> false
        "DARK"  -> true
        else     -> isSystemInDarkTheme()
    }

    val colors = if (darkTheme) AppDarkColorScheme else AppLightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography  = Typography,
        content     = content
    )
}