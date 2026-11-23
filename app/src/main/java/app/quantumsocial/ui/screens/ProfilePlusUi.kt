package app.quantumsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.AuraStore
import app.quantumsocial.core.data.ConnectionStore
import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.data.GratitudeStore
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Badge
import app.quantumsocial.core.model.BadgeStats
import app.quantumsocial.core.model.DailyAura
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.LinkStatus
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.ui.signals.EmotionOrb

/** Dnešná aura: farba + krátky odkaz, zhasne o polnoci. */
@Composable
fun DailyAuraCard() {
    val aura by AuraStore.aura.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val active = aura?.takeIf { it.isActive(today) }
    var emotion by remember { mutableStateOf(active?.emotion ?: Emotion.Joy) }
    var text by remember { mutableStateOf(active?.text.orEmpty()) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("Dnešná aura"), style = MaterialTheme.typography.titleMedium)
            Text(
                active?.let {
                    val note = if (it.text.isEmpty()) "" else tr(" – „%s“", it.text)
                    tr("Dnes žiariš: %s", tr(it.emotion.label())) + note
                }
                    ?: tr("Ako dnes žiariš? Aura zhasne o polnoci."),
                style = MaterialTheme.typography.bodySmall,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Emotion.values().forEach { e -> EmotionOrb(emotion = e, selected = e == emotion) { emotion = e } }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(DailyAura.MAX_TEXT) },
                label = { Text(tr("Krátky odkaz (max %s)", DailyAura.MAX_TEXT)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row {
                TextButton(onClick = { AuraStore.set(emotion, text) }) { Text(tr("✨ Rozžiariť auru")) }
                if (active != null) TextButton(onClick = { AuraStore.clear() }) { Text(tr("Zhasnúť")) }
            }
        }
    }
}

/** Odznaky: získané svietia, ostatné sú bledé s nápovedou. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BadgesCard(myStarId: String) {
    val dust by StarDust.state.collectAsState()
    val journal by GratitudeStore.journal.collectAsState()
    val garden by SeedStore.garden.collectAsState()
    val links by ConnectionStore.links.collectAsState()
    val galaxies by GalaxyStore.shared.galaxies.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val stats =
        BadgeStats(
            lifetimeDust = dust.lifetime,
            gratitudeDays = journal.entries.map { it.day }.toSet(),
            seeds = garden.seeds.size,
            galaxies = galaxies.count { myStarId in it.members },
            activeSoulLinks = links.links.count { it.status == LinkStatus.Active },
            today = today,
        )
    val earned = Badge.earnedBy(stats)

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("Odznaky %s/%s", earned.size, Badge.values().size), style = MaterialTheme.typography.titleMedium)
            if (stats.gratitudeStreak > 0) {
                Text(tr("🔥 %s dní vďačnosti v rade", stats.gratitudeStreak), style = MaterialTheme.typography.bodySmall)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Badge.values().forEach { b ->
                    val has = b in earned
                    Column(
                        modifier = Modifier.alpha(if (has) 1f else 0.35f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(b.emoji, style = MaterialTheme.typography.headlineSmall)
                        Text(tr(b.label), style = MaterialTheme.typography.labelSmall)
                        if (!has) Text(tr(b.hint), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
