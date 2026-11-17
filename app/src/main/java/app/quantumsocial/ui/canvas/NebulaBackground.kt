package app.quantumsocial.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.random.Random

@Composable
fun NebulaBackground(
    modifier: Modifier = Modifier,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    seed: Int = 1337,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val r = maxOf(w, h)

        val sky =
            Brush.radialGradient(
                colors = listOf(Color(0xFF1A1457), Color(0xFF0B0B2B)),
                center = Offset(w * 0.60f, h * 0.40f),
                radius = r,
            )
        drawRect(brush = sky, size = size)

        fun haze(
            cx: Float,
            cy: Float,
            radius: Float,
            color: Color,
            alpha: Float,
            parallax: Float,
        ) {
            val px = cx - offsetX * parallax
            val py = cy - offsetY * parallax
            drawCircle(color = color.copy(alpha = alpha), radius = radius, center = Offset(px, py))
        }

        haze(w * 0.22f, h * 0.30f, r * 0.40f, Color(0xFF7B1FA2), 0.25f, 0.02f)
        haze(w * 0.78f, h * 0.22f, r * 0.30f, Color(0xFF3949AB), 0.20f, 0.03f)
        haze(w * 0.58f, h * 0.66f, r * 0.42f, Color(0xFF512DA8), 0.22f, 0.035f)
        haze(w * 0.40f, h * 0.78f, r * 0.28f, Color(0xFFAD1457), 0.18f, 0.045f)
        haze(w * 0.66f, h * 0.46f, r * 0.22f, Color(0xFF00ACC1), 0.12f, 0.05f)

        val rnd = Random(seed)
        withTransform({ translate(left = -offsetX * 0.015f, top = -offsetY * 0.015f) }) {
            repeat(350) {
                val x = rnd.nextFloat() * w
                val y = rnd.nextFloat() * h
                val rad = 0.5f + rnd.nextFloat() * 1.5f
                val a = 0.35f + rnd.nextFloat() * 0.45f
                drawCircle(color = Color.White.copy(alpha = a), radius = rad, center = Offset(x, y))
            }
        }
    }
}
