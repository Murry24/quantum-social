package app.quantumsocial.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.perf.LocalPerformanceMode
import app.quantumsocial.ui.perf.rememberGalaxySeconds
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// bod 6: rozloženie pastelových hmlovín (stred x, y, polomer, paralaxa) – farby sú v GalaxyPalette
private val hazeLayout =
    floatArrayOf(
        0.22f, 0.30f, 0.40f, 0.02f,
        0.78f, 0.22f, 0.30f, 0.03f,
        0.58f, 0.66f, 0.42f, 0.035f,
        0.40f, 0.78f, 0.28f, 0.045f,
        0.66f, 0.46f, 0.22f, 0.05f,
    )

private const val DUST_COUNT = 350

@Composable
fun NebulaBackground(
    modifier: Modifier = Modifier,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    seed: Int = 1337,
) {
    val mode = LocalPerformanceMode.current
    val seconds = rememberGalaxySeconds()
    val sky = remember { GalaxyPalette.skyBrush }
    val hazes = remember { GalaxyPalette.hazes.map { it.color } }

    // hviezdny prach: x, y, polomer, priehľadnosť – vypočítané raz, nie v každom snímku
    val dust =
        remember(seed) {
            val rnd = Random(seed)
            FloatArray(DUST_COUNT * 4) { i ->
                when (i % 4) {
                    0, 1 -> rnd.nextFloat()
                    2 -> 0.5f + rnd.nextFloat() * 1.5f
                    else -> 0.35f + rnd.nextFloat() * 0.45f
                }
            }
        }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val r = maxOf(w, h)
        val px = if (mode.parallax) offsetX else 0f
        val py = if (mode.parallax) offsetY else 0f
        // čas čítame až tu – prekresľuje sa len pozadie, nie celá obrazovka
        val t = seconds.value

        drawRect(brush = sky, size = size)

        // hmloviny pomaly plávajú (perióda niekoľko desiatok sekúnd)
        for (i in hazes.indices) {
            val b = i * 4
            val drift = 0.035f
            val cx = w * (hazeLayout[b] + drift * sin(t * 0.11f + i * 1.7f)) - px * hazeLayout[b + 3]
            val cy = h * (hazeLayout[b + 1] + drift * cos(t * 0.08f + i * 2.3f)) - py * hazeLayout[b + 3]
            val breathe = 1f + 0.06f * sin(t * 0.15f + i)
            drawCircle(color = hazes[i], radius = r * hazeLayout[b + 2] * breathe, center = Offset(cx, cy))
        }

        withTransform({ translate(left = -px * 0.015f, top = -py * 0.015f) }) {
            for (i in 0 until DUST_COUNT) {
                val b = i * 4
                drawCircle(
                    color = Color.White.copy(alpha = dust[b + 3]),
                    radius = dust[b + 2],
                    center = Offset(dust[b] * w, dust[b + 1] * h),
                )
            }
        }
    }
}
