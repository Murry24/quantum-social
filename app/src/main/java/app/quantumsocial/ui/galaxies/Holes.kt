package app.quantumsocial.ui.galaxies

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.model.PerformanceMode
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.perf.rememberGalaxySeconds

/** Bod 31: čierna diera = vstup, biela = odchod, červia = skok do inej mojej galaxie. */
enum class HoleKind { Black, White, Worm }

/** Pomaly sa točiaca diera s pastelovým prstencom a popisom pod ňou. */
@Composable
fun HoleButton(
    kind: HoleKind,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    onClick: () -> Unit,
) {
    val seconds = rememberGalaxySeconds()
    val ring =
        remember(kind) {
            val colors =
                when (kind) {
                    HoleKind.Black -> GalaxyPalette.hazes.map { it.argb }
                    HoleKind.White -> GalaxyPalette.fireflies
                    HoleKind.Worm -> GalaxyPalette.zenBubbles
                }.map { Color(it) }
            Brush.sweepGradient(colors + colors.first())
        }
    val core = Color(if (kind == HoleKind.Black) GalaxyPalette.HOLE_CORE else GalaxyPalette.ON_SKY)
    // biela diera sa točí opačne – „vypľúva“ von
    val dir = if (kind == HoleKind.White) -1f else 1f

    Column(
        modifier = modifier.clickable(onClickLabel = label, onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val r = this.size.minDimension / 2
            rotate(degrees = seconds.value * 40f * dir) {
                drawCircle(brush = ring, radius = r * 0.78f, style = Stroke(width = r * 0.3f))
            }
            drawCircle(
                brush = Brush.radialGradient(listOf(core, core.copy(alpha = 0f)), center, r * 0.62f),
                radius = r * 0.62f,
            )
        }
        Text(label, fontSize = 11.sp, color = Color(GalaxyPalette.ON_SKY))
    }
}

/**
 * „Warp“ pri prelete dierou: [t] = 0 normálne, 1 = obrazovka stiahnutá do stredu.
 * V šetriči výkonu bez točenia – len krátky fade.
 */
fun Modifier.warp(
    t: Float,
    mode: PerformanceMode,
): Modifier =
    graphicsLayer {
        alpha = 1f - t
        if (mode == PerformanceMode.Full) {
            scaleX = 1f - t
            scaleY = 1f - t
            rotationZ = 540f * t
        }
    }

/** Polovica preletu (stiahnutie alebo rozbalenie) – spolu ~1 s, v šetriči krátko. */
fun warpMillis(mode: PerformanceMode): Int = if (mode == PerformanceMode.Full) 500 else 150
