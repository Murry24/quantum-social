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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.model.PulseEffect
import app.quantumsocial.ui.model.StarPoint
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Pulz letiaci zo stredu galaxie (moja hviezda) k cieľovej hviezde (bod 8). */
data class PulseFlight(
    val id: String,
    val starId: String,
    val color: Color,
    val emoji: String? = null,
    val effect: PulseEffect? = null,
) {
    companion object {
        fun of(
            sent: PulseOutbox.Sent,
            color: Color,
        ) = PulseFlight(sent.signal.id, sent.resolvedStarId, color, sent.signal.emoji, sent.signal.effect)
    }
}

private val rainbow =
    listOf(
        Color(0xFFFF6B6B),
        Color(0xFFFFC145),
        Color(0xFFFFF06B),
        Color(0xFF6BFF9E),
        Color(0xFF6BC8FF),
        Color(0xFFB26BFF),
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
    val textMeasurer = rememberTextMeasurer()

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
                        val trailColor = if (f.effect == PulseEffect.Rainbow) rainbow[i % rainbow.size] else f.color
                        drawCircle(
                            color = trailColor.copy(alpha = 0.55f * fade),
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
                    if (f.effect == PulseEffect.Sparks) {
                        // iskry: drobné bodky rozhádzané okolo hlavy pulzu
                        for (k in 0 until 6) {
                            val a = k * 1.047f + travel * 12f
                            drawCircle(
                                color = Color.White.copy(alpha = 0.8f),
                                radius = orb * 0.18f,
                                center = Offset(tx * travel + cos(a) * orb * 1.6f, ty * travel + sin(a) * orb * 1.6f),
                            )
                        }
                    }
                    f.emoji?.let { e ->
                        val layout = textMeasurer.measure(e, TextStyle(fontSize = (18 / scale).sp))
                        drawText(
                            textLayoutResult = layout,
                            topLeft =
                                Offset(
                                    tx * travel - layout.size.width / 2f,
                                    ty * travel - layout.size.height - orb,
                                ),
                        )
                    }
                }

                // dopad: rozvlnený kruh okolo hviezdy
                if (p >= ARRIVAL_PART) {
                    val wave = (p - ARRIVAL_PART) / (1f - ARRIVAL_PART)
                    // vlna = 3 kruhy za sebou, inak 1
                    val rings = if (f.effect == PulseEffect.Wave) 3 else 1
                    for (ring in 0 until rings) {
                        val w = (wave - ring * 0.2f).coerceAtLeast(0f)
                        drawCircle(
                            color = f.color.copy(alpha = 0.8f * (1f - w)),
                            radius = orb * (1.5f + 5f * w),
                            center = Offset(tx, ty),
                            style = Stroke(width = 2.dp.toPx() / scale),
                            blendMode = BlendMode.Plus,
                        )
                    }
                    if (f.effect == PulseEffect.Flash) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f * (1f - wave)),
                            radius = orb * 4f,
                            center = Offset(tx, ty),
                            blendMode = BlendMode.Plus,
                        )
                    }
                }
            }
        }
    }
}
