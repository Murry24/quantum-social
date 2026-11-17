package app.quantumsocial.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.quantumsocial.ui.model.StarPoint
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Screen-space hit-test robustný pri zoome a filtrovaní.
 * Vyberá hviezdu s najmenším normalizovaným skóre (d^2 / R^2).
 */
fun nearestHitStarScreen(
    tap: Offset,
    stars: List<StarPoint>,
    canvasSize: IntSize,
    animScale: Float,
    animOffsetX: Float,
    animOffsetY: Float,
    density: androidx.compose.ui.unit.Density,
): StarPoint? {
    if (canvasSize.width == 0 || canvasSize.height == 0) return null

    val width = canvasSize.width.toFloat()
    val height = canvasSize.height.toFloat()
    val centerX = width / 2f
    val centerY = height / 2f
    val baseRadius = min(width, height) / 2f * 0.9f

    val minPx = with(density) { 14.dp.toPx() }
    val maxPx = with(density) { 56.dp.toPx() }

    var best: StarPoint? = null
    var bestScore = Float.MAX_VALUE

    for (s in stars) {
        val r = baseRadius * s.distance
        val xw = r * cos(s.angleRad.toDouble()).toFloat()
        val yw = r * sin(s.angleRad.toDouble()).toFloat()
        val sx = centerX + animOffsetX + animScale * xw
        val sy = centerY + animOffsetY + animScale * yw

        val base = if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity
        val starCorePx = base * animScale
        val hitR = (starCorePx * 1.6f).coerceIn(minPx, maxPx)

        val dx = tap.x - sx
        val dy = tap.y - sy
        val d2 = dx * dx + dy * dy
        if (d2 <= hitR * hitR) {
            val score = d2 / (hitR * hitR)
            if (score < bestScore) {
                bestScore = score
                best = s
            }
        }
    }

    return best
}
