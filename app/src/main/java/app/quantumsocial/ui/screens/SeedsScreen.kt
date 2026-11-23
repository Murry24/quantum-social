package app.quantumsocial.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.FutureSeed
import app.quantumsocial.core.model.SeedHelpKind
import app.quantumsocial.core.model.SeedHelpers
import app.quantumsocial.core.model.SeedOutcome
import app.quantumsocial.core.model.SeedSize
import app.quantumsocial.core.model.SeedStage
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.signals.EmotionOrb
import kotlin.math.cos
import kotlin.math.sin

/**
 * FutureSeeds → FutureBlooms (bod 30): semienko = cieľ alebo malý sen. Každý deň ho zaleješ
 * malým krokom, galaxia ti môže pomôcť a splnené ciele ostanú ako kvety v záhrade.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeedsScreen(onBack: () -> Unit) {
    val garden by SeedStore.garden.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    var text by remember { mutableStateOf("") }
    var size by remember { mutableStateOf(SeedSize.Small) }
    var watering by remember { mutableStateOf<FutureSeed?>(null) }
    var digOut by remember { mutableStateOf<String?>(null) }
    val onSky = Color(GalaxyPalette.ON_SKY)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onBack) { Text(tr("‹ Späť"), color = onSky) }
            Text(tr("🌱 Semienka budúcnosti"), style = MaterialTheme.typography.titleLarge, color = onSky)
            Text(
                tr("Zasaď cieľ alebo malý sen. Každý deň ho zalej malým krokom – keď rozkvitne, ostane ti kvet."),
                color = onSky,
            )
        }
        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(FutureSeed.MAX_TEXT) },
                label = { Text(tr("môj cieľ (max %s)", FutureSeed.MAX_TEXT)) },
                modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SeedSize.values().forEach { s ->
                    FilterChip(
                        selected = s == size,
                        onClick = { size = s },
                        label = { Text(tr("%s %s · %s dní", s.emoji, tr(s.label), s.days)) },
                    )
                }
            }
            Button(onClick = { if (SeedStore.plant(text, size)) text = "" }, enabled = text.isNotBlank()) {
                Text(tr("Zasadiť 🌰"))
            }
        }
        items(garden.growing, key = { it.id }) { s ->
            SeedCard(s, today, onWater = { watering = s }, onDig = { digOut = s.id })
        }
        if (garden.flowers.isNotEmpty()) {
            item { Text(tr("🌸 Záhrada kvetov"), style = MaterialTheme.typography.titleMedium, color = onSky) }
            items(garden.flowers, key = { "f" + it.id }) { FlowerCard(it) }
        }
    }

    watering?.let { seed -> WaterDialog(seed, onClose = { watering = null }) }
    digOut?.let { id ->
        AlertDialog(
            onDismissRequest = { digOut = null },
            text = { Text(tr("Vykopať semienko?")) },
            confirmButton = {
                TextButton(onClick = {
                    SeedStore.remove(id)
                    digOut = null
                }) { Text(tr("Vykopať")) }
            },
            dismissButton = { TextButton(onClick = { digOut = null }) { Text(tr("Zrušiť")) } },
        )
    }
}

@Composable
private fun SeedCard(
    s: FutureSeed,
    today: Long,
    onWater: () -> Unit,
    onDig: () -> Unit,
) {
    var showPath by remember { mutableStateOf(false) }
    val small = MaterialTheme.typography.bodySmall
    val thirsty = s.isThirsty(today)
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (thirsty) "🥀" else s.stage.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.text)
                    Text(
                        tr("%s · %s · zaliate %s/%s", tr(s.size.label), tr(s.stage.label), s.waterings, s.target),
                        style = small,
                    )
                }
                TextButton(onClick = onDig) { Text("🗑") }
            }
            LinearProgressIndicator(
                progress = (s.waterings.toFloat() / s.target).coerceAtMost(1f),
                modifier = Modifier.fillMaxWidth(),
            )
            if (thirsty) Text(tr("🥀 Smädné – zalej ho"), style = small)

            if (s.awaitsAnswer) {
                Text(tr("🌸 Rozkvitlo! Splnilo sa?"), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalButton(onClick = { SeedStore.finish(s.id, SeedOutcome.Done) }) { Text(tr("✅ Áno")) }
                    TextButton(onClick = { SeedStore.notYet(s.id) }) { Text(tr("🌱 Ešte nie")) }
                    TextButton(onClick = { SeedStore.finish(s.id, SeedOutcome.Changed) }) { Text(tr("🍂 Zmenilo sa")) }
                }
            } else {
                FilledTonalButton(onClick = onWater, enabled = s.canWater(today)) {
                    Text(if (s.canWater(today)) tr("💧 Zaliať malým krokom") else tr("💧 Dnes zaliate"))
                }
            }

            // pomoc z galaxie
            if (s.sharedDay == null) {
                TextButton(onClick = { SeedStore.share(s.id) }) { Text(tr("🤝 Požiadať galaxiu o pomoc")) }
            } else {
                val help = SeedHelpers.helpFor(s, today)
                val rain = help.count { it.kind == SeedHelpKind.Rain }
                val tips = help.filter { it.kind == SeedHelpKind.Tip }
                Text(tr("🤝 Galaxia pomáha: 🌧️ %s dážď podpory · 💡 %s rád", rain, tips.size), style = small)
                tips.takeLast(3).forEach { Text(tr("💡 „%s“", tr(it.text)), style = small) }
            }

            if (s.steps.isNotEmpty()) {
                TextButton(onClick = { showPath = !showPath }) { Text(tr("👣 Moja cesta (%s krokov)", s.steps.size)) }
                if (showPath) StepList(s)
            }
        }
    }
}

@Composable
private fun StepList(s: FutureSeed) {
    s.steps.reversed().forEach { st ->
        val mood = st.emotion?.let { tr(it.label()) + " · " }.orEmpty()
        val day = tr("deň %s", st.day - s.plantedDay + 1)
        Text(tr("· %s: %s%s", day, mood, st.text), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FlowerCard(s: FutureSeed) {
    var showPath by remember { mutableStateOf(false) }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Flower(s.flowerEmotions, modifier = Modifier.size(64.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(s.text, style = MaterialTheme.typography.titleSmall)
                Text(tr("✅ Splnené · %s krokov", s.steps.size), style = MaterialTheme.typography.bodySmall)
                if (s.released) {
                    Text(tr("💫 Letí vo WishNete"), style = MaterialTheme.typography.bodySmall)
                } else {
                    TextButton(onClick = { SeedStore.release(s.id) }) { Text(tr("💫 Pustiť do WishNetu")) }
                }
                if (s.steps.isNotEmpty()) {
                    TextButton(onClick = { showPath = !showPath }) {
                        Text(tr("👣 Moja cesta (%s krokov)", s.steps.size))
                    }
                    if (showPath) StepList(s)
                }
            }
        }
    }
}

/** Jedinečný kvet: lupene vo farbách nálad, s ktorými sa zalievalo. */
@Composable
private fun Flower(
    emotions: List<Emotion>,
    modifier: Modifier = Modifier,
) {
    val colors = emotions.map { it.color() }
    val petals = 5 + emotions.size
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2
        for (i in 0 until petals) {
            val a = i * 2 * Math.PI / petals
            val p = Offset(c.x + (r * 0.55f * cos(a)).toFloat(), c.y + (r * 0.55f * sin(a)).toFloat())
            drawCircle(colors[i % colors.size].copy(alpha = 0.85f), radius = r * 0.4f, center = p)
        }
        drawCircle(Color(0xFFFFD54F), radius = r * 0.28f, center = c)
    }
}

/** Zaliatie: nálada + čo som dnes pre cieľ spravil (oboje nepovinné). */
@Composable
private fun WaterDialog(
    seed: FutureSeed,
    onClose: () -> Unit,
) {
    var emotion by remember { mutableStateOf<Emotion?>(null) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(tr("💧 %s", seed.text)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr("Ako sa pri tom cítiš?"))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Emotion.values().forEach { e -> EmotionOrb(emotion = e, selected = e == emotion) { emotion = e } }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(FutureSeed.MAX_STEP) },
                    label = { Text(tr("Čo si dnes pre to spravil? (nepovinné)")) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                SeedStore.water(seed.id, emotion = emotion, note = note)
                onClose()
            }) { Text(tr("💧 Zaliať")) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(tr("Zrušiť")) } },
    )
}
