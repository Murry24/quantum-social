package app.quantumsocial.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.ui.theme.GalaxyGradient

@Composable
fun GalaxyScreen() {
    // Animácie – použijeme menované parametre
    val transition = rememberInfiniteTransition(label = "pulse")

    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "scale",
    )

    val glowAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "alpha",
    )

    // Farby si vezmeme mimo Canvas (Canvas lambda nie je @Composable)
    val colors = MaterialTheme.colorScheme
    val tertiary = colors.tertiary
    val secondary = colors.secondary
    val primary = colors.primary
    val onPrimary = colors.onPrimary

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(GalaxyGradient),
        contentAlignment = Alignment.Center,
    ) {
        // jemné hviezdne bodky
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 0..60) {
                val x = (i * 37 % w.toInt()).toFloat()
                val y = (i * 79 % h.toInt()).toFloat()
                drawCircle(
                    color = tertiary.copy(alpha = 0.12f),
                    radius = 1.8f,
                    center = Offset(x, y),
                    blendMode = BlendMode.SrcOver,
                )
            }
        }

        // žiara pod hviezdou
        Canvas(
            modifier =
                Modifier
                    .size(220.dp)
                    .alpha(glowAlpha),
        ) {
            drawCircle(
                color = secondary.copy(alpha = 0.7f),
                radius = size.minDimension / 2.2f,
            )
        }

        // pulzujúca hviezda
        IconButton(onClick = { /* neskôr: akcia */ }) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Pulsujúca hviezda",
                tint = primary,
                modifier =
                    Modifier
                        .size(96.dp)
                        .scale(scale),
            )
        }

        // titulok
        Text(
            text = "Quantum Social",
            color = onPrimary.copy(alpha = 0.9f),
        )
    }
}
