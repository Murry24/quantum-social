package app.quantumsocial.core.model

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Živá tapeta (bod 33) – čistá matematika scény, bez Androidu, aby sa dala testovať.
 * Všetky súradnice sú 0..1 (násobia sa šírkou/výškou obrazovky), čas v sekundách.
 */
class WallpaperScene(
    seed: Int = 33,
    starCount: Int = STAR_COUNT,
) {
    data class Star(
        val x: Float,
        val y: Float,
        val radius: Float,
        val phase: Float,
        val speed: Float,
    )

    val stars: List<Star> =
        Random(seed).let { rnd ->
            List(starCount) {
                Star(
                    x = rnd.nextFloat(),
                    y = rnd.nextFloat(),
                    radius = 0.6f + rnd.nextFloat() * 1.8f,
                    phase = rnd.nextFloat() * 2f * PI.toFloat(),
                    speed = 0.4f + rnd.nextFloat() * 0.9f,
                )
            }
        }

    /** Jas hviezdy 0.2..1 – pomalé trblietanie; v šetriči hviezdy nesvietia naplno, ale stoja. */
    fun starAlpha(
        star: Star,
        seconds: Float,
        twinkle: Boolean,
    ): Float = if (twinkle) 0.6f + 0.4f * sin(seconds * star.speed + star.phase) else 0.75f

    /** Pomalý pohyb hmloviny [index] okolo jej základného miesta (posun max 6 % obrazovky). */
    fun hazeCenter(
        index: Int,
        seconds: Float,
    ): Pair<Float, Float> {
        val base = HAZE_BASE[index % HAZE_BASE.size]
        val t = seconds * HAZE_SPEED + index * 1.7f
        return (base.first + 0.06f * sin(t)) to (base.second + 0.06f * sin(t * 0.8f + 1f))
    }

    companion object {
        const val STAR_COUNT = 70
        private const val HAZE_SPEED = 0.05f

        // stred hmlovín (x, y) – rovnaké rozloženie ako na mape, len pre zvislú obrazovku
        private val HAZE_BASE =
            listOf(0.25f to 0.25f, 0.75f to 0.35f, 0.4f to 0.6f, 0.7f to 0.8f, 0.2f to 0.85f)

        /** Ako často prekresliť: tapeta má byť úsporná, v šetriči ešte úspornejšia. */
        fun frameIntervalMs(mode: PerformanceMode): Long = if (mode == PerformanceMode.Saver) 200L else 66L
    }
}
