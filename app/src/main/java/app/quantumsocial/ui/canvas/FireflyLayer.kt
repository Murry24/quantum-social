package app.quantumsocial.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.PerformanceMode
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.perf.LocalPerformanceMode
import app.quantumsocial.ui.perf.rememberGalaxySeconds
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// počet údajov na jednu svetlušku: x, y, fáza, rýchlosť stúpania, rýchlosť blikania, farba
private const val FIELDS = 6

/**
 * Svetlušky (bod 6): drobné svetielka, ktoré pomaly plávajú nahor a blikajú.
 * Všetko na jednom Canvase, údaje sú pripravené raz – v snímku sa nič nealokuje.
 * Počet podľa režimu výkonu (Saver = menej).
 */
@Composable
fun FireflyLayer(
    modifier: Modifier = Modifier,
    seed: Int = 7,
) {
    val count = LocalPerformanceMode.current.fireflyCount
    val seconds = rememberGalaxySeconds()
    val colors = remember { GalaxyPalette.fireflies.map { Color(it) } }

    val data =
        remember(seed) {
            val max = PerformanceMode.Full.fireflyCount
            val rnd = Random(seed)
            FloatArray(max * FIELDS) { i ->
                when (i % FIELDS) {
                    0, 1 -> rnd.nextFloat()
                    2 -> rnd.nextFloat() * 6.28f
                    3 -> 0.006f + rnd.nextFloat() * 0.014f
                    4 -> 0.6f + rnd.nextFloat() * 1.2f
                    else -> rnd.nextInt(colors.size).toFloat()
                }
            }
        }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = seconds.value
        val w = size.width
        val h = size.height
        val coreR = 1.6.dp.toPx()
        val glowR = 7.dp.toPx()
        val n = minOf(count, data.size / FIELDS)

        for (i in 0 until n) {
            val b = i * FIELDS
            val phase = data[b + 2]
            // stúpa nahor a dookola sa vracia dole, popri tom sa jemne kolíše do strán
            val rise = (data[b + 1] - t * data[b + 3]) % 1f
            val y = (if (rise < 0f) rise + 1f else rise) * (h + glowR * 2) - glowR
            val x = data[b] * w + sin(t * 0.35f + phase) * 0.03f * w + cos(t * 0.21f + phase * 2f) * 0.015f * w

            val blink = sin(t * data[b + 4] + phase)
            val light = 0.15f + 0.85f * (if (blink > 0f) blink * blink else 0f)
            val color = colors[data[b + 5].toInt()]

            drawCircle(
                color = color.copy(alpha = 0.35f * light),
                radius = glowR * (0.6f + 0.4f * light),
                center = Offset(x, y),
                blendMode = BlendMode.Plus,
            )
            drawCircle(color = color.copy(alpha = light), radius = coreR, center = Offset(x, y))
        }
    }
}
