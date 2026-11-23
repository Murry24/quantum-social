package app.quantumsocial.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.DreamMirror
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.GalacticMoments
import app.quantumsocial.core.model.StarLevel
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.perf.LocalPerformanceMode
import kotlin.math.cos
import kotlin.math.sin

private val onSky = Color(GalaxyPalette.ON_SKY)

/**
 * Galaktický moment (bod 17): keď hviezda preskočí na vyšší level, celá obrazovka
 * na chvíľu zažiari farbami emócií. Ťuk alebo 4 s ho zavrie.
 */
@Composable
fun GalacticMomentOverlay() {
    val dust by StarDust.state.collectAsState()
    var lastDust by remember { mutableStateOf<Int?>(null) }
    var moment by remember { mutableStateOf<StarLevel?>(null) }

    LaunchedEffect(dust.lifetime) {
        val before = lastDust
        // prvé načítanie (z disku) moment nespúšťa
        if (before != null) GalacticMoments.levelUp(before, dust.lifetime)?.let { moment = it }
        lastDust = dust.lifetime
    }

    val level = moment ?: return
    val burst = remember(level) { Animatable(0f) }
    val animate = LocalPerformanceMode.current.twinkle
    LaunchedEffect(level) {
        burst.animateTo(1f, tween(if (animate) 1_600 else 300))
        kotlinx.coroutines.delay(2_400)
        moment = null
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xAA1B1446))
                .clickable { moment = null },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val maxR = size.minDimension * 0.6f * burst.value
            Emotion.values().forEachIndexed { i, e ->
                for (k in 0 until 6) {
                    val a = (i * 6 + k) * 0.21f
                    val r = maxR * (0.5f + 0.5f * ((k + 1) / 6f))
                    drawCircle(
                        color = e.color().copy(alpha = 0.8f * (1f - burst.value * 0.5f)),
                        radius = 6.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(c.x + cos(a) * r, c.y + sin(a) * r),
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tr("🌟 Galaktický moment"), style = MaterialTheme.typography.titleLarge, color = onSky)
            Text(
                tr("Stala sa z teba %s %s", tr(level.label), level.emoji),
                style = MaterialTheme.typography.headlineSmall,
                color = onSky,
                textAlign = TextAlign.Center,
            )
            Text(tr("Celá galaxia ti posiela svetlo."), color = onSky)
        }
    }
}

/** DreamMirror (bod 12): sen ostáva len v pamäti, nájde sa snový most k podobnému snu. */
@Composable
fun DreamScreen(onBack: () -> Unit) {
    var dream by remember { mutableStateOf("") }
    var bridge by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var searched by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TextButton(onClick = onBack) { Text(tr("‹ Späť"), color = onSky) }
        Text(tr("🌙 DreamMirror"), style = MaterialTheme.typography.titleLarge, color = onSky)
        Text(tr("Čo sa ti snívalo? Sen ostane len v tomto telefóne a po zatvorení zmizne."), color = onSky)
        OutlinedTextField(
            value = dream,
            onValueChange = {
                dream = it.take(DreamMirror.MAX_TEXT)
                searched = false
            },
            label = { Text(tr("môj sen (max %s)", DreamMirror.MAX_TEXT)) },
            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)),
        )
        Button(
            onClick = {
                bridge = DreamMirror.bridge(dream)
                searched = true
            },
            enabled = dream.isNotBlank(),
        ) { Text(tr("Hľadať snový most 🌉")) }
        if (searched) {
            val b = bridge
            if (b != null) {
                Text(tr("Snový most s inou hviezdou · zhoda %s %", (b.second * 100).toInt()), color = onSky)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(tr("🌙 %s", b.first), modifier = Modifier.padding(16.dp))
                }
            } else {
                Text(tr("Tvoj sen je zatiaľ jedinečný – žiadna hviezda nemala podobný. ✨"), color = onSky)
            }
        }
    }
}

/** Emócia dňa (bod 17) – krátky riadok pre obrazovky galaxie. */
fun emotionOfDayText(epochDay: Long): String = tr("Emócia dňa: %s", tr(GalacticMoments.emotionOfDay(epochDay).label()))
