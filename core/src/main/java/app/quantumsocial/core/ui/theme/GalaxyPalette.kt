package app.quantumsocial.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Veselá galaxia (bod 6) – svetlá „úsvitová“ obloha namiesto temného vesmíru.
 * Farby sú ako ARGB Long, aby sa dali testovať bez Androidu (kontrast v `HappyGalaxyTest`).
 * Obrazovky berú farby len odtiaľto, nie napevno.
 */
object GalaxyPalette {
    // obloha: modrá → fialová → tyrkysová
    const val SKY_TOP = 0xFF2F6FD6
    const val SKY_MIDDLE = 0xFF5B4FD9
    const val SKY_BOTTOM = 0xFF167A87

    // pastelové hmloviny (farba + priehľadnosť), ktoré pomaly plávajú po oblohe
    val hazes: List<Haze> =
        listOf(
            Haze(0xFFFF8FD8, 0.22f),
            Haze(0xFF5FF2E0, 0.20f),
            Haze(0xFFB7A6FF, 0.24f),
            Haze(0xFFFFC48A, 0.18f),
            Haze(0xFF7FD4FF, 0.18f),
        )

    // svetlušky: teplé a jasné bodky
    val fireflies: List<Long> = listOf(0xFFFFF3B0, 0xFFFFE08A, 0xFFE8FFF8, 0xFFFFD1F0)

    // ZenNet – pokojnejšia verzia tej istej oblohy
    const val ZEN_TOP = 0xFF3470BE
    const val ZEN_BOTTOM = 0xFF5E55C9
    val zenBubbles: List<Long> = listOf(0xFF8FD3FF, 0xFFB9A7FF, 0xFF9EF0E0, 0xFFFFE3A3)

    // text a jadrá hviezd na oblohe
    const val ON_SKY = 0xFFFFFFFF

    // bod 31: stred čiernej diery – hlboká indigová, nie strašidelná čierna
    const val HOLE_CORE = 0xFF241E4E

    val skyStops: List<Long> = listOf(SKY_TOP, SKY_MIDDLE, SKY_BOTTOM)

    val skyBrush: Brush
        get() = Brush.verticalGradient(skyStops.map { Color(it) })

    // jemná (pastelová) verzia oblohy pre obrazovky s bežným tmavým textom, napr. WishNet
    val softSkyBrush: Brush
        get() = Brush.verticalGradient(skyStops.map { Color(it).copy(alpha = 0.22f) })

    val zenBrush: Brush
        get() = Brush.verticalGradient(listOf(Color(ZEN_TOP), Color(ZEN_BOTTOM)))

    data class Haze(
        val argb: Long,
        val alpha: Float,
    ) {
        val color: Color get() = Color(argb).copy(alpha = alpha)
    }
}

/** Kontrast farieb podľa WCAG 2 (1 = žiadny, 21 = čierna na bielej). */
object ColorContrast {
    fun ratio(
        a: Long,
        b: Long,
    ): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    /** Farba [top] s priehľadnosťou [alpha] položená na nepriehľadné [bottom]. */
    fun blend(
        top: Long,
        alpha: Float,
        bottom: Long,
    ): Long {
        var out = 0xFF000000
        for (shift in intArrayOf(16, 8, 0)) {
            val t = (top shr shift) and 0xFF
            val b = (bottom shr shift) and 0xFF
            val c = (t * alpha + b * (1f - alpha)).roundToInt().toLong()
            out = out or (c shl shift)
        }
        return out
    }

    private fun luminance(argb: Long): Double {
        fun channel(shift: Int): Double {
            val c = ((argb shr shift) and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }
}
