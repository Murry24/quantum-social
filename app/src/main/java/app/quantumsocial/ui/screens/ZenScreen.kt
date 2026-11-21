package app.quantumsocial.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.model.ZenSession
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private val ZenTop = Color(0xFF1B3A5C)
private val ZenBottom = Color(0xFF2E2A6B)
private val BubbleColors =
    listOf(Color(0xFF8FD3FF), Color(0xFFB9A7FF), Color(0xFF9EF0E0), Color(0xFFFFE3A3))

private data class Bubble(
    val x: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float,
    val color: Color,
)

/**
 * ZenNet (bod 8): bublina ticha. Jedna minúta pokoja – žiadne slová ani signály,
 * len pomaly stúpajúce bubliny a dýchajúci kruh, ktorý ukazuje plynutie času.
 */
@Composable
fun ZenScreen(onClose: () -> Unit) {
    var session by remember { mutableStateOf<ZenSession?>(null) }
    val openedAt = remember { System.currentTimeMillis() }

    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(50)
        }
    }

    val bubbles =
        remember {
            val rnd = Random(42)
            List(28) {
                Bubble(
                    x = rnd.nextFloat(),
                    radius = 8f + rnd.nextFloat() * 26f,
                    speed = 0.02f + rnd.nextFloat() * 0.05f,
                    phase = rnd.nextFloat(),
                    color = BubbleColors[rnd.nextInt(BubbleColors.size)],
                )
            }
        }

    val breath by rememberInfiniteTransition(label = "zen-breath").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "breath",
    )

    val active = session
    val progress = active?.progress(now) ?: 0f
    val finished = active?.isFinished(now) == true
    // relatívny čas (Float by pri absolútnych ms stratil presnosť)
    val seconds = (now - openedAt) / 1000f

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ZenTop, ZenBottom))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // bubliny stúpajú zdola nahor, jemne sa kolíšu
            for (b in bubbles) {
                val t = ((seconds * b.speed + b.phase) % 1f)
                val y = size.height * (1.1f - 1.2f * t)
                val sway = sin((t * 2f * PI + b.phase * 6f).toFloat()) * 18f
                val x = size.width * b.x + sway
                val r = b.radius.dp.toPx() * 0.5f
                drawCircle(
                    color = b.color.copy(alpha = 0.18f),
                    radius = r,
                    center = Offset(x, y),
                    blendMode = BlendMode.Plus,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = r,
                    center = Offset(x, y),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }

            // dýchajúci kruh v strede + oblúk priebehu minúty
            val c = Offset(size.width / 2f, size.height / 2f)
            val base = min(size.width, size.height) * 0.22f
            val breathR = base * (0.85f + 0.15f * breath)
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                        center = c,
                        radius = breathR,
                    ),
                radius = breathR,
                center = c,
            )
            if (active != null) {
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(c.x - base * 1.15f, c.y - base * 1.15f),
                    size = Size(base * 2.3f, base * 2.3f),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
        ) {
            when {
                active == null ->
                    Button(onClick = { session = ZenSession(startedAt = System.currentTimeMillis()) }) {
                        Text("Vstúpiť do ticha")
                    }
                finished ->
                    Text(text = "✓", fontSize = 32.sp, color = Color.White)
                else -> Unit
            }
            TextButton(onClick = onClose, modifier = Modifier.alpha(0.7f)) {
                Text(if (finished) "Späť do galaxie" else "Odísť", color = Color.White)
            }
        }
    }
}
