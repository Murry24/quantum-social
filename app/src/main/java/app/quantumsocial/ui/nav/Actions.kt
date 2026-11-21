package app.quantumsocial.ui.nav

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.ui.perf.LocalPerformanceMode

/** Okrúhle ✦ v strede spodnej lišty; jemne pulzuje (v šetriči výkonu nie). */
@Composable
fun ActionOrb() {
    val pulse =
        if (LocalPerformanceMode.current.twinkle) {
            val t = rememberInfiniteTransition(label = "action-orb")
            val s by t.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(tween(1_400), RepeatMode.Reverse),
                label = "action-orb-scale",
            )
            s
        } else {
            1f
        }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(36.dp)
                .scale(pulse)
                .background(
                    Brush.radialGradient(listOf(Color.White, Color(0xFFFFE08A), Color(0xFFB7A6FF))),
                    CircleShape,
                ),
    ) {
        Text("✦", fontSize = 18.sp, color = Color(0xFF3A2E7A))
    }
}

/** Panel po ťuku na ✦: tri veľké voľby. */
@Composable
fun ActionsSheet(
    onChaos: () -> Unit,
    onZen: () -> Unit,
    onGalaxies: () -> Unit,
    onGratitude: () -> Unit,
    onChallenge: () -> Unit,
    onMood: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        BigAction("🌌  Do chaosu – pocit náhodnej hviezde", onChaos)
        BigAction("○  ZenNet – minúta ticha", onZen)
        BigAction("🕳  Moje galaxie", onGalaxies)
        BigAction("🌟  Vďačnosť – zlatá iskra dňa", onGratitude)
        BigAction("🎯  Výzva dňa", onChallenge)
        BigAction("🌊  Vlny nálad galaxie", onMood)
    }
}

@Composable
private fun BigAction(
    text: String,
    onClick: () -> Unit,
) {
    FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text, fontSize = 16.sp, modifier = Modifier.padding(vertical = 6.dp))
    }
}
