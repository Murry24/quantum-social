package app.quantumsocial.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.ChallengeStore
import app.quantumsocial.core.data.GratitudeStore
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.model.DailyChallenge
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.LightSignature
import app.quantumsocial.core.model.MoodWaves
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.model.toStarPoint
import app.quantumsocial.ui.perf.LocalPerformanceMode
import kotlin.math.PI
import kotlin.math.sin

private val onSky = Color(GalaxyPalette.ON_SKY)

/** Spoločný rám denných obrazoviek: pokojná obloha, nadpis, tlačidlo späť. */
@Composable
private fun SkyFrame(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹ Späť", color = onSky) }
            Text(title, style = MaterialTheme.typography.titleLarge, color = onSky)
        }
        content()
    }
}

/** GratNet (bod 14): jeden zápis vďačnosti denne, zlaté iskry 7 dní. */
@Composable
fun GratitudeScreen(onBack: () -> Unit) {
    val journal by GratitudeStore.journal.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    var text by remember { mutableStateOf("") }

    SkyFrame("🌟 Vďačnosť", onBack) {
        if (journal.canWrite(today)) {
            Text("Za čo si dnes vďačný?", color = onSky)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(GratitudeJournal.MAX_TEXT) },
                label = { Text("pár slov (max ${GratitudeJournal.MAX_TEXT})") },
                modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)),
            )
            Button(onClick = { if (GratitudeStore.write(text)) text = "" }, enabled = text.isNotBlank()) {
                Text("Zapáliť zlatú iskru ✨ (+${DustAction.Gratitude.reward})")
            }
        } else {
            Text("Dnešná iskra už svieti. Zajtra môžeš zapáliť ďalšiu. ✨", color = onSky)
        }
        Text("Tvoje zlaté iskry (zhasnú po 7 dňoch)", style = MaterialTheme.typography.titleSmall, color = onSky)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(journal.visible(today).entries) { e ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    val ago = today - e.day
                    Text(
                        "✨ ${e.text}" + if (ago == 0L) "  · dnes" else "  · pred $ago d",
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

/** ChallengeNet (bod 13): jedna jemná výzva dňa pre všetkých. */
@Composable
fun ChallengeScreen(onBack: () -> Unit) {
    val doneDay by ChallengeStore.doneDay.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val done = doneDay == today

    SkyFrame("🎯 Výzva dňa", onBack) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                DailyChallenge.forDay(today),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(20.dp),
            )
        }
        Text("Dnes ju dostali všetky hviezdy v galaxii. Zajtra príde nová.", color = onSky)
        if (done) {
            Text("✅ Splnené – dobrá práca, hviezda!", color = onSky, style = MaterialTheme.typography.titleMedium)
        } else {
            Button(onClick = { ChallengeStore.complete() }) {
                Text("Splnil som ✓ (+${DustAction.Challenge.reward} StarDust)")
            }
        }
    }
}

/** Mood Waves (bod 26): vlny nálad – čím viac hviezd cíti danú emóciu, tým vyššia vlna. */
@Composable
fun MoodWavesScreen(onBack: () -> Unit) {
    val signals by InMemorySignalRepository.signals.collectAsState()
    val sent by PulseOutbox.sent.collectAsState()
    val emotions = remember(signals, sent) { signals.map { it.toStarPoint().emotion } + sent.map { it.signal.emotion } }
    val shares = MoodWaves.shares(emotions)
    val dominant = MoodWaves.dominant(emotions)
    val animate = LocalPerformanceMode.current.twinkle
    val phase by rememberInfiniteTransition(label = "waves").animateFloat(
        initialValue = 0f,
        targetValue = if (animate) 2f * PI.toFloat() else 0f,
        animationSpec = infiniteRepeatable(tween(6_000), RepeatMode.Restart),
        label = "waves-phase",
    )

    SkyFrame("🌊 Vlny nálad", onBack) {
        Text(
            if (dominant != null) "Galaxia práve najviac cíti: ${dominant.label()}" else "Galaxia je zatiaľ tichá.",
            color = onSky,
            style = MaterialTheme.typography.titleMedium,
        )
        // bod 17: emócia dňa je rovnaká pre celú galaxiu
        Text("✨ " + emotionOfDayText(StarDustState.epochDay(System.currentTimeMillis())), color = onSky)
        Canvas(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            val emotionsAll = Emotion.values()
            emotionsAll.forEachIndexed { i, e ->
                val share = shares.getValue(e)
                val baseY = size.height * (i + 1) / (emotionsAll.size + 1)
                val amp = 6.dp.toPx() + share * 40.dp.toPx()
                val path = Path()
                var x = 0f
                while (x <= size.width) {
                    val y = baseY + amp * sin(x / size.width * 4f * PI.toFloat() + phase + i)
                    if (x == 0f) path.moveTo(x, y) else path.lineTo(x, y)
                    x += 8f
                }
                drawPath(path, e.color().copy(alpha = 0.4f + 0.6f * share), style = Stroke(width = 3.dp.toPx()))
            }
        }
        Emotion.values().forEach { e ->
            Text("${e.label()}: ${(shares.getValue(e) * 100).toInt()} %", color = onSky)
        }
    }
}

/** LightSignature (bod 21): moje prstence a rytmus – jedinečné pre každú hviezdu. */
@Composable
fun LightSignatureView(
    starId: String,
    modifier: Modifier = Modifier,
) {
    val sig = remember(starId) { LightSignature.of(starId) }
    val beat = sig.rhythmMs.average().toInt()
    val pulse by rememberInfiniteTransition(label = "signature").animateFloat(
        initialValue = 0.85f,
        targetValue = if (LocalPerformanceMode.current.twinkle) 1.1f else 0.85f,
        animationSpec = infiniteRepeatable(tween(beat * 2), RepeatMode.Reverse),
        label = "signature-pulse",
    )
    Canvas(modifier = modifier.size(96.dp)) {
        val maxR = size.minDimension / 2f
        for (ring in 0 until sig.rings) {
            val color = sig.colors[ring % sig.colors.size].color()
            val r = maxR * (ring + 1) / (sig.rings + 0.5f) * pulse
            drawCircle(
                color = color.copy(alpha = 0.9f - ring * 0.2f),
                radius = r.coerceAtMost(maxR),
                style = Stroke(4.dp.toPx()),
            )
        }
        drawCircle(Color.White, radius = maxR * 0.12f, center = Offset(size.width / 2f, size.height / 2f))
    }
}
