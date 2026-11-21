package app.quantumsocial.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import app.quantumsocial.ui.model.StarPoint
import app.quantumsocial.ui.screens.color // from EmotionUi.kt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun StarCanvas(
    stars: List<StarPoint>,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    reduceMotion: Boolean,
    focusStarId: String?,
    focusVisible: Boolean,
    // bod 5: hviezdy, ktorých žiarenie sleduješ – majú jemný prstenec
    watchedIds: Set<String> = emptySet(),
) {
    val tick =
        if (reduceMotion) {
            0f
        } else {
            rememberInfiniteTransition(label = "twinkle").animateFloat(
                initialValue = 0f,
                targetValue = (2f * PI).toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
                label = "tick",
            ).value
        }

    val pulse =
        if (reduceMotion) {
            0f
        } else {
            rememberInfiniteTransition(label = "focusPulse").animateFloat(
                0f,
                1f,
                animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
                label = "pulse",
            ).value
        }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeColor = Color.White.copy(alpha = 0.10f)
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = min(size.width, size.height) / 2f * 0.9f

        withTransform({
            translate(left = centerX + offsetX, top = centerY + offsetY)
            scale(scaleX = scale, scaleY = scale)
        }) {
            drawCircle(color = strokeColor, radius = baseRadius)

            stars.forEach { s ->
                val color = s.emotion.color()
                val r = baseRadius * s.distance
                val x = r * cos(s.angleRad.toDouble()).toFloat()
                val y = r * sin(s.angleRad.toDouble()).toFloat()

                // jas podľa životnosti: hviezda pri konci bledne a pri zhasnutí sa zmenšuje
                val light = s.brightness.coerceIn(0f, 1f)
                if (light <= 0f) return@forEach
                val sizeFactor = 0.6f + 0.4f * light

                val base = (if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity) * sizeFactor
                val phase = (s.id.hashCode() % 628) / 100f
                val twinkle = (kotlin.math.sin(tick + phase) + 1f) / 2f
                val haloRadius = base * if (s.isWishNet) 3f else 2f
                val haloAlpha =
                    light * if (reduceMotion) 0.10f * s.intensity else 0.08f + 0.20f * twinkle * s.intensity

                drawCircle(
                    color = color.copy(alpha = haloAlpha),
                    radius = haloRadius,
                    center = Offset(x, y),
                    blendMode = BlendMode.Plus,
                )
                drawCircle(color = color.copy(alpha = 0.95f * light), radius = base, center = Offset(x, y))
                drawCircle(
                    color = Color.White.copy(alpha = 0.90f * light),
                    radius = base * 0.55f,
                    center = Offset(x, y),
                )

                if (s.id in watchedIds) {
                    val breath = if (reduceMotion) 0.5f else (kotlin.math.sin(tick * 0.5f + phase) + 1f) / 2f
                    drawCircle(
                        color = Color.White.copy(alpha = light * (0.30f + 0.20f * breath)),
                        radius = base * 3.2f + 2f,
                        center = Offset(x, y),
                        style = Stroke(width = 1.dp.toPx() / scale.coerceAtLeast(0.5f)),
                    )
                }
            }

            if (focusVisible && focusStarId != null) {
                val s = stars.firstOrNull { it.id == focusStarId }
                if (s != null) {
                    val r = baseRadius * s.distance
                    val x = r * cos(s.angleRad.toDouble()).toFloat()
                    val y = r * sin(s.angleRad.toDouble()).toFloat()

                    val base = if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity
                    val ringRadius = base * (4f + if (reduceMotion) 0f else 2f * pulse)
                    val ringAlpha = if (reduceMotion) 0.22f else 0.35f - 0.20f * pulse

                    drawCircle(
                        color = s.emotion.color().copy(alpha = ringAlpha),
                        radius = ringRadius,
                        center = Offset(x, y),
                        style = Stroke(width = 2.dp.toPx()),
                        blendMode = BlendMode.Plus,
                    )
                }
            }
        }
    }
}
