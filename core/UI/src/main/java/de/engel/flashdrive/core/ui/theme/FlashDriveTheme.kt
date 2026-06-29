package de.engel.flashdrive.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FlashDriveColorScheme = darkColorScheme(
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

private val FlashDriveShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun FlashDriveTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FlashDriveColorScheme,
        typography = FlashDriveTypography,
        shapes = FlashDriveShapes,
        content = content
    )
}
