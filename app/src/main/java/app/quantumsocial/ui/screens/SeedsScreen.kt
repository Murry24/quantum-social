package app.quantumsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.model.FutureSeed
import app.quantumsocial.core.model.SeedStage
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.ui.theme.GalaxyPalette

/** FutureSeeds → FutureBlooms (bod 30): zasaď prianie do budúcnosti a zalievaj ho raz denne. */
@Composable
fun SeedsScreen(onBack: () -> Unit) {
    val garden by SeedStore.garden.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    var text by remember { mutableStateOf("") }
    val onSky = Color(GalaxyPalette.ON_SKY)

    Column(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("‹ Späť", color = onSky) }
        Text("🌱 Semienka budúcnosti", style = MaterialTheme.typography.titleLarge, color = onSky)
        Text(
            "Zasaď prianie do budúcnosti. Zalievaj ho raz denne – " +
                "po ${FutureSeed.WATERINGS_TO_BLOOM} dňoch rozkvitne. 🌸",
            color = onSky,
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(FutureSeed.MAX_TEXT) },
            label = { Text("moje semienko (max ${FutureSeed.MAX_TEXT})") },
            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)),
        )
        Button(onClick = { if (SeedStore.plant(text)) text = "" }, enabled = text.isNotBlank()) {
            Text("Zasadiť 🌰")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(garden.seeds, key = { it.id }) { s ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        Text(s.stage.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.text)
                            Text(
                                "${s.stage.label} · zaliate ${s.waterings}/${FutureSeed.WATERINGS_TO_BLOOM}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (s.stage != SeedStage.Bloom) {
                            FilledTonalButton(onClick = { SeedStore.water(s.id) }, enabled = s.canWater(today)) {
                                Text("💧")
                            }
                        }
                    }
                }
            }
        }
    }
}
