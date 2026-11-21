package app.quantumsocial.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Farebná nálada oblohy hlavnej galaxie (bod 32). Mení sa len obloha – hmloviny a svetlušky
 * ostávajú z [GalaxyPalette], aby galaxia zostala veselá. Kontrast bieleho textu stráži test.
 */
enum class GalaxyTheme(
    val label: String,
    val skyStops: List<Long>,
) {
    Dawn("Úsvit", GalaxyPalette.skyStops),
    Aurora("Polárna žiara", listOf(0xFF0F6B5C, 0xFF2E5FA8, 0xFF5B3FA0)),
    Rose("Ruža", listOf(0xFFB03A6A, 0xFF8A3BA6, 0xFF5A4BC2)),
    Ocean("Oceán", listOf(0xFF0B5A8C, 0xFF1F4FA0, 0xFF0B6A78)),
    GoldenHour("Zlatá hodina", listOf(0xFFB04A1C, 0xFF9A3A6A, 0xFF5A3D9E)),
    ;

    val skyBrush: Brush
        get() = Brush.verticalGradient(skyStops.map { Color(it) })

    companion object {
        fun parse(stored: String?): GalaxyTheme = values().firstOrNull { it.name == stored } ?: Dawn
    }
}
