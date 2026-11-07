package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LuminaDarkColorScheme = darkColorScheme(
    primary = LuminaIndigo,
    onPrimary = Color.White,
    primaryContainer = LuminaIndigoContainer,
    onPrimaryContainer = LuminaIndigoLight,
    secondary = LuminaEmerald,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = LuminaEmeraldContainer,
    onSecondaryContainer = LuminaEmeraldLight,
    tertiary = LuminaAmber,
    onTertiary = Color(0xFF1F1200),
    tertiaryContainer = LuminaAmberContainer,
    onTertiaryContainer = LuminaAmberLight,
    error = LuminaCrimson,
    onError = Color.White,
    errorContainer = LuminaCrimsonContainer,
    onErrorContainer = LuminaCrimsonLight,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    outlineVariant = ObsidianCardElevated
)

val LuminaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun LuminaSpendTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LuminaDarkColorScheme,
        typography = Typography,
        shapes = LuminaShapes,
        content = content
    )
}
