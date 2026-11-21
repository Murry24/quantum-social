package app.quantumsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.PerformanceChoice
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.model.StarLevel
import app.quantumsocial.core.ui.theme.GalaxyTheme
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import app.quantumsocial.ui.perf.GalaxyThemeSettings
import app.quantumsocial.ui.perf.PerformanceSettings
import app.quantumsocial.wallpaper.WallpaperLauncher

@Composable
fun ProfileScreen(onRebirth: () -> Unit) {
    val context = LocalContext.current
    val choice by PerformanceSettings.choice.collectAsState()
    val theme by GalaxyThemeSettings.theme.collectAsState()
    val dust by StarDust.state.collectAsState()

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Môj profil", style = MaterialTheme.typography.titleLarge)

        // bod 20: StarDust – nedá sa kúpiť ani predať
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // bod 16: svetelný level podľa nazbieraného prachu
                val level = StarLevel.of(dust.balance)
                Text("${level.emoji} ${level.label}", style = MaterialTheme.typography.titleLarge)
                LinearProgressIndicator(
                    progress = { StarLevel.progress(dust.balance) },
                    modifier = Modifier.fillMaxWidth(),
                )
                level.next?.let { next ->
                    Text(
                        "Do levelu ${next.label} chýba ${next.minDust - dust.balance} StarDust",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text("✨ ${dust.balance} StarDust", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Hviezdny prach za pulzy, odpovede, priania, minútu ticha a nové galaxie. " +
                        "Nedá sa kúpiť ani predať.",
                    style = MaterialTheme.typography.bodySmall,
                )
                val today = StarDustState.epochDay(System.currentTimeMillis())
                DustAction.values().forEach { a ->
                    val used = if (dust.day == today) dust.countToday(a) else 0
                    Text(
                        "${a.label()}: dnes $used/${a.dailyCap} · +${a.reward}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        // bod 21: môj svetelný podpis – jedinečný pre každú hviezdu
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LightSignatureView(starId = OnboardingPrefs.myStarId(context))
            Column {
                Text("Môj svetelný podpis", style = MaterialTheme.typography.titleMedium)
                Text("Farby a rytmus len tvojej hviezdy.", style = MaterialTheme.typography.bodySmall)
            }
        }

        // bod 32: farebná nálada oblohy hlavnej galaxie
        Text("Farba galaxie", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GalaxyTheme.values()) { t ->
                FilterChip(
                    selected = t == theme,
                    onClick = { GalaxyThemeSettings.set(context, t) },
                    label = { Text(t.label) },
                    leadingIcon = {
                        Box(Modifier.size(16.dp).clip(CircleShape).background(t.skyBrush))
                    },
                )
            }
        }

        // bod 33: moja galaxia ako pohyblivá tapeta telefónu
        OutlinedButton(onClick = { WallpaperLauncher.open(context) }) { Text("✨ Nastaviť galaxiu ako tapetu") }

        // bod 6: ručné prepnutie šetriča výkonu (Auto = podľa šetriča batérie v Androide)
        Text("Animácie galaxie", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PerformanceChoice.values().forEach { option ->
                FilterChip(
                    selected = option == choice,
                    onClick = { PerformanceSettings.set(context, option) },
                    label = { Text(option.label()) },
                )
            }
        }

        // bod 18: na testovanie – otvorí onboarding znova, obnovovací kľúč ostáva
        OutlinedButton(onClick = onRebirth) { Text("Znova prejsť zrodením") }
    }
}

private fun PerformanceChoice.label() =
    when (this) {
        PerformanceChoice.Auto -> "Auto"
        PerformanceChoice.Full -> "Plné"
        PerformanceChoice.Saver -> "Šetrič"
    }

private fun DustAction.label() =
    when (this) {
        DustAction.Pulse -> "Pulz"
        DustAction.Reply -> "Odpoveď svetlom"
        DustAction.Wish -> "Prianie vo WishNet"
        DustAction.Zen -> "Minúta ticha"
        DustAction.Galaxy -> "Nová galaxia"
        DustAction.Gratitude -> "Vďačnosť"
        DustAction.Challenge -> "Výzva dňa"
    }
