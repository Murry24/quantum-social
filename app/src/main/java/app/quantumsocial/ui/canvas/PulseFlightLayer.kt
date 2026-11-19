package app.quantumsocial.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import app.quantumsocial.ui.model.StarPoint
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Pulz letiaci zo stredu galaxie (moja hviezda) k cieľovej hviezde (bod 8). */
data class PulseFlight(
    val id: String,
    val starId: String,
    val color: Color,
)

private const val FLIGHT_MS = 1_400
private const val ARRIVAL_PART = 0.8f
private const val TRAIL_STEPS = 6

/**
 * Kreslí letiace pulzy v rovnakom súradnicovom systéme ako [StarCanvas]:
 * svetelná guľa s chvostom letí k hviezde a pri dopade sa rozvlní kruh.
 */
@Composable
fun PulseFlightLayer(
    flights: List<PulseFlight>,
    stars: List<StarPoint>,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    onFinished: (String) -> Unit,
) {
    val progress = remember { mutableStateMapOf<String, Float>() }
    val finish by rememberUpdatedState(onFinished)

    flights.forEach { f ->
        key(f.id) {
            LaunchedEffect(f.id) {
                val anim = Animatable(0f)
                anim.animateTo(1f, tween(FLIGHT_MS, easing = FastOutSlowInEasing)) {
                    progress[f.id] = value
                }
                progress.remove(f.id)
                finish(f.id)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = min(size.width, size.height) / 2f * 0.9f
        val orb = 6.dp.toPx() / scale

        withTransform({
            translate(left = centerX + offsetX, top = centerY + offsetY)
            scale(scaleX = scale, scaleY = scale)
        }) {
            for (f in flights) {
                val target = stars.firstOrNull { it.id == f.starId } ?: continue
                val p = progress[f.id] ?: 0f
                val r = baseRadius * target.distance
                val tx = r * cos(target.angleRad.toDouble()).toFloat()
                val ty = r * sin(target.angleRad.toDouble()).toFloat()

                // let: guľa s chvostom
                val travel = (p / ARRIVAL_PART).coerceAtMost(1f)
                if (travel < 1f) {
                    for (i in TRAIL_STEPS downTo 0) {
                        val t = (travel - i * 0.035f).coerceAtLeast(0f)
                        val fade = 1f - i.toFloat() / (TRAIL_STEPS + 1)
                        drawCircle(
                            color = f.color.copy(alpha = 0.55f * fade),
                            radius = orb * (0.5f + 0.5f * fade),
                            center = Offset(tx * t, ty * t),
                            blendMode = BlendMode.Plus,
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f),
                        radius = orb * 0.55f,
                        center = Offset(tx * travel, ty * travel),
                    )
                }

                // dopad: rozvlnený kruh okolo hviezdy
                if (p >= ARRIVAL_PART) {
                    val wave = (p - ARRIVAL_PART) / (1f - ARRIVAL_PART)
                    drawCircle(
                        color = f.color.copy(alpha = 0.8f * (1f - wave)),
                        radius = orb * (1.5f + 5f * wave),
                        center = Offset(tx, ty),
                        style = Stroke(width = 2.dp.toPx() / scale),
                        blendMode = BlendMode.Plus,
                    )
                }
            }
        }
    }
}
