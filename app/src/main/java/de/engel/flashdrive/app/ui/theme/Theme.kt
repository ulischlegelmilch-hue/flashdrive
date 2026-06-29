package de.engel.flashdrive.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── FlashDrive Brand Colors (Dark Mode by default) ──
private val DeepBlue = Color(0xFF1A1A2E)
private val PurpleAccent = Color(0xFF7C5CFC)
private val CyanAccent = Color(0xFF00D4FF)
private val SurfaceDark = Color(0xFF0F0F1A)
private val CardDark = Color(0xFF1C1C2E)
private val SuccessGreen = Color(0xFF34D399)
private val ErrorRed = Color(0xFFF87171)
private val WarningOrange = Color(0xFFFB923C)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2D2060),
    onPrimaryContainer = Color(0xFFE8E0FF),
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003D52),
    onSecondaryContainer = Color(0xFFB3E5FF),
    tertiary = WarningOrange,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4A2800),
    onTertiaryContainer = Color(0xFFFFD180),
    background = SurfaceDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
)

private val LightColorScheme = darkColorScheme(
    primary = PurpleAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E0FF),
    onPrimaryContainer = Color(0xFF1A0F40),
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFB3E5FF),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = WarningOrange,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFD180),
    onTertiaryContainer = Color(0xFF2A1400),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
)

/**
 * FlashDrive Theme — always uses dark mode for consistent brand identity.
 * The app is designed for low-light environments (evening study, car dashboard).
 */
@Composable
fun FlashDriveTheme(
    darkTheme: Boolean = true, // Always dark by default for brand consistency
    dynamicColor: Boolean = false, // Disable dynamic color to keep brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
