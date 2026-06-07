package org.singhak.kubera.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val KuberaColorScheme = darkColorScheme(
    background = BgColor,
    surface = CardColor,
    surfaceVariant = SubtleColor,
    surfaceContainerLow = SubtleColor,
    surfaceContainerLowest = BgColor,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    primary = TextPrimary,
    onPrimary = BgColor,
    outline = TextSecondary,
    outlineVariant = BorderColor,
    error = RedColor,
    onError = BgColor,
)

private val KuberaShapes = Shapes(
    extraSmall = RoundedCornerShape(20.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun KuberaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KuberaColorScheme,
        typography = Typography,
        shapes = KuberaShapes,
        content = content
    )
}
