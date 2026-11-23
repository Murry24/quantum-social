@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.ConnectionStore
import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Constellation
import app.quantumsocial.core.model.LinkStatus
import app.quantumsocial.core.model.QuantName
import app.quantumsocial.core.model.Serendipity
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.model.SoulLinks
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.model.WorldLanguages
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.model.starEmotionForId
import app.quantumsocial.ui.model.starLangForId
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import app.quantumsocial.ui.signals.SendPulseSheet

private val onSky = Color(GalaxyPalette.ON_SKY)

@Composable
private fun SkyColumn(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text(tr("‹ Späť"), color = onSky) }
        Text(title, style = MaterialTheme.typography.titleLarge, color = onSky)
        Text(subtitle, color = onSky)
        content()
    }
}

/** Spodný panel na pulz konkrétnej hviezde (zdieľaný pre most, súhvezdia aj SoulLinks). */
@Composable
private fun PulseSheet(
    starId: String?,
    onClose: () -> Unit,
) {
    val id = starId ?: return
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        SendPulseSheet(
            target = SignalTarget.Star(id),
            onSend = { emotion, emoji, effect, urgent ->
                PulseOutbox.send(
                    target = SignalTarget.Star(id),
                    emotion = emotion,
                    visibleStarIds = emptyList(),
                    emoji = emoji,
                    effect = effect,
                    urgent = urgent,
                )
                onClose()
            },
        )
    }
}

/** Serendipity (bod 10): dnešný svetelný most s jednou náhodnou hviezdou sveta. */
@Composable
fun SerendipityScreen(
    onBack: () -> Unit,
    onOpenStar: (String) -> Unit,
) {
    val context = LocalContext.current
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val match = remember(today) { Serendipity.matchFor(OnboardingPrefs.myStarId(context), today) }
    val links by ConnectionStore.links.collectAsState()
    var pulseTo by remember { mutableStateOf<String?>(null) }

    SkyColumn(
        tr("🌉 Svetelný most dňa"),
        tr("Každý deň ťa galaxia spojí s jednou hviezdou. Most zhasne o polnoci."),
        onBack,
    ) {
        val id = match ?: return@SkyColumn
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                LightSignatureView(starId = id)
                Column(modifier = Modifier.padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(QuantName.of(id), style = MaterialTheme.typography.titleLarge)
                    Text(tr("Žiari: %s", tr(starEmotionForId(id).label())), color = starEmotionForId(id).color())
                    Text(tr("Jazyk: %s", WorldLanguages.chip(starLangForId(id))))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { pulseTo = id }) { Text(tr("✦ Pošli pulz")) }
            FilledTonalButton(onClick = { onOpenStar(id) }) { Text(tr("Profil")) }
        }
        SoulLinkButton(id, links)
    }
    PulseSheet(pulseTo) { pulseTo = null }
}

@Composable
private fun SoulLinkButton(
    starId: String,
    links: SoulLinks,
) {
    when (links.of(starId)?.status) {
        LinkStatus.Active -> Text(tr("💞 Máte SoulLink"), color = onSky)
        LinkStatus.Pending -> Text(tr("⏳ SoulLink čaká na súhlas druhej hviezdy"), color = onSky)
        null ->
            TextButton(
                onClick = { ConnectionStore.requestLink(starId) },
                enabled = links.links.size < SoulLinks.MAX_LINKS,
            ) { Text(tr("💞 Ponúknuť SoulLink (%s/%s)", links.links.size, SoulLinks.MAX_LINKS), color = onSky) }
    }
}

/** Constellations (bod 22): dočasné súhvezdia do 10 hviezd na tému – žijú 3 dni. */
@Composable
fun ConstellationsScreen(onBack: () -> Unit) {
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val joined by ConnectionStore.joined.collectAsState()
    val list = remember(today) { (0L until Constellation.LIFE_DAYS).flatMap { Constellation.ofDay(today - it) } }
    var pulseTo by remember { mutableStateOf<String?>(null) }

    val subtitle = tr("Malé skupiny do %s hviezd na jednu tému. Po 3 dňoch zhasnú.", Constellation.MAX_MEMBERS)
    SkyColumn(tr("✨ Súhvezdia"), subtitle, onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list.filter { it.isAlive(today) }, key = { it.id }) { c ->
                val inside = c.id in joined
                val count = c.members.size + if (inside) 1 else 0
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(c.theme, style = MaterialTheme.typography.titleMedium, color = c.emotion.color())
                        val left = c.daysLeft(today)
                        val emotion = tr(c.emotion.label())
                        Text(tr("%s · %s/%s hviezd · ešte %s d", emotion, count, Constellation.MAX_MEMBERS, left))
                        if (inside) {
                            c.members.take(5).forEach { m ->
                                TextButton(onClick = { pulseTo = m }) { Text(tr("✦ %s", QuantName.of(m))) }
                            }
                        }
                        TextButton(
                            onClick = { ConnectionStore.toggleConstellation(c.id) },
                            enabled = inside || count < Constellation.MAX_MEMBERS,
                        ) { Text(if (inside) tr("Opustiť súhvezdie") else tr("Vstúpiť do súhvezdia")) }
                    }
                }
            }
        }
    }
    PulseSheet(pulseTo) { pulseTo = null }
}

/** SoulLinks (bod 23): moje trvalé spojenia 1:1 – najviac 5, až po obojstrannom súhlase. */
@Composable
fun SoulLinksScreen(
    onBack: () -> Unit,
    onOpenStar: (String) -> Unit,
) {
    val links by ConnectionStore.links.collectAsState()
    var pulseTo by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { ConnectionStore.refresh() }

    val subtitle = tr("Najviac %s trvalých spojení. Vznikajú len keď súhlasia obe hviezdy.", SoulLinks.MAX_LINKS)
    SkyColumn(tr("💞 SoulLinks"), subtitle, onBack) {
        if (links.links.isEmpty()) {
            Text(tr("Zatiaľ žiadne. Ponúkni SoulLink v profile hviezdy alebo na svetelnom moste."), color = onSky)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(links.links, key = { it.starId }) { l ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(QuantName.of(l.starId), style = MaterialTheme.typography.titleMedium)
                            Text(if (l.status == LinkStatus.Active) tr("💞 spojení") else tr("⏳ čaká na súhlas"))
                        }
                        if (l.status == LinkStatus.Active) TextButton(onClick = { pulseTo = l.starId }) { Text("✦") }
                        TextButton(onClick = { onOpenStar(l.starId) }) { Text(tr("Profil")) }
                        TextButton(onClick = { ConnectionStore.removeLink(l.starId) }) { Text("✕") }
                    }
                }
            }
        }
    }
    PulseSheet(pulseTo) { pulseTo = null }
}

/** Tlačidlo SoulLink pre profil hviezdy. */
@Composable
fun SoulLinkOffer(starId: String) {
    val links by ConnectionStore.links.collectAsState()
    SoulLinkButton(starId, links)
}
