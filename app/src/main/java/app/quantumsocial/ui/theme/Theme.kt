package app.quantumsocial.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

val LightColors: ColorScheme = lightColorScheme(
    primary = GalaxyBlue,
    secondary = GalaxyTurquoise,
    tertiary = GalaxyPurple
)
val DarkColors: ColorScheme = darkColorScheme(
    primary = GalaxyBlue,
    secondary = GalaxyTurquoise,
    tertiary = GalaxyPurple
)

val GalaxyGradient
    get() = Brush.verticalGradient(listOf(GalaxyBgTop, GalaxyBgBottom))

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
