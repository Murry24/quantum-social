package app.quantumsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.DreamCircleStore
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.DreamCircles
import app.quantumsocial.core.model.DreamNoteKind
import app.quantumsocial.core.model.DreamSymbol
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.signals.EmotionOrb

private val onSky = Color(GalaxyPalette.ON_SKY)

private fun today() = StarDustState.epochDay(System.currentTimeMillis())

/** V DreamMirror: kruhy k vybraným symbolom + moje kruhy. */
@Composable
fun DreamCirclesSection(
    symbols: Set<DreamSymbol>,
    onOpen: (String) -> Unit,
) {
    val state by DreamCircleStore.state.collectAsState()
    val day = today()
    val mine = state.alive(day).circles
    val shown = (symbols + mine.map { it.symbol }).distinct()
    if (shown.isEmpty()) return

    Text(tr("🌌 Snové kruhy"), style = MaterialTheme.typography.titleMedium, color = onSky)
    Text(tr("Hviezdy, ktoré snívali o tom istom. Nikto nevie, kto je kto."), color = onSky)
    shown.forEach { sym ->
        val joined = state.joined(sym, day)
        val window = DreamCircles.windowOf(day)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tr("Kruh: %s", "${sym.emoji} ${tr(sym.label)}"), style = MaterialTheme.typography.titleSmall)
                    Text(
                        tr("%s snívajúcich · zhasne o %s d", DreamCircles.others(sym, window) + 1, window + 3 - day),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (joined != null) {
                    FilledTonalButton(onClick = { onOpen(joined.key) }) { Text(tr("Otvoriť")) }
                } else {
                    Button(onClick = {
                        DreamCircleStore.join(sym)
                        DreamCircleStore.state.value.joined(sym, today())?.let { onOpen(it.key) }
                    }) { Text(tr("🌌 Vstúpiť")) }
                }
            }
        }
    }
}

/** Vnútri kruhu: anonymné odkazy, spoločná niť, rezonancia a môj odkaz. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DreamCircleScreen(
    key: String,
    onBack: () -> Unit,
) {
    val state by DreamCircleStore.state.collectAsState()
    val day = today()
    val circle = state.circles.firstOrNull { it.key == key && it.isAlive(day) }
    if (circle == null) {
        // kruh medzitým zhasol
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val notes = DreamCircles.notes(circle, day)
    var kind by remember { mutableStateOf(DreamNoteKind.Meaning) }
    var emotion by remember { mutableStateOf<Emotion?>(null) }
    var text by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            TextButton(onClick = onBack) { Text(tr("‹ Späť"), color = onSky) }
            Text(
                tr("Kruh: %s", "${circle.symbol.emoji} ${tr(circle.symbol.label)}"),
                style = MaterialTheme.typography.titleLarge,
                color = onSky,
            )
            Text(
                tr(
                    "%s snívajúcich · zhasne o %s d",
                    DreamCircles.others(circle.symbol, circle.window) + 1,
                    circle.daysLeft(day),
                ),
                color = onSky,
            )
            DreamCircles.sharedThread(notes)?.let { (e, n) ->
                Text(tr("🧵 Spoločná niť: %s hviezd cítilo %s", n, tr(e.label())), color = onSky)
            }
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DreamNoteKind.values().forEach { k ->
                            FilterChip(
                                selected = k == kind,
                                onClick = { kind = k },
                                label = { Text("${k.emoji} ${tr(k.label)}") },
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Emotion.values().forEach { e ->
                            EmotionOrb(emotion = e, selected = e == emotion) { emotion = if (emotion == e) null else e }
                        }
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.take(DreamCircles.MAX_NOTE) },
                        label = { Text(tr("Povedz kruhu viac o svojom sne (max %s)", DreamCircles.MAX_NOTE)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { if (DreamCircleStore.post(key, kind, emotion, text)) text = "" },
                        enabled = text.isNotBlank(),
                    ) { Text(tr("Poslať do kruhu ✦")) }
                }
            }
        }
        items(notes, key = { it.id }) { n ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val who = if (n.author == 0) tr("Ty") else tr("Snívajúca hviezda %s", n.author)
                    val mood = n.emotion?.let { " · " + tr(it.label()) }.orEmpty()
                    Text(
                        "${n.kind.emoji} $who · ${tr(n.kind.label)}$mood",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    // ukážkové odkazy sú kľúče slovníka, moje sú vlastný text
                    Text(if (n.author == 0) n.text else tr(n.text))
                    if (n.author != 0) {
                        val mineToo = n.id in circle.resonated
                        TextButton(onClick = { DreamCircleStore.toggleResonance(key, n.id) }) {
                            Text(tr("✨ Rezonuje %s", DreamCircles.resonance(circle, n)) + if (mineToo) " ✓" else "")
                        }
                    }
                }
            }
        }
    }
}
