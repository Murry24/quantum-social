@file:OptIn(ExperimentalMaterial3Api::class)

package app.quantumsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.data.WatchedStars
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.QuantName
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.core.model.WorldLanguages
import app.quantumsocial.lingua.LinguaText
import app.quantumsocial.ui.model.starEmotionForId
import app.quantumsocial.ui.model.starLangForId
import app.quantumsocial.ui.model.starTopicForId
import app.quantumsocial.ui.signals.SendPulseSheet
import app.quantumsocial.ui.signals.WishNetCard
import app.quantumsocial.ui.signals.typeLabel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun StarProfileScreen(
    starId: String,
    onBack: () -> Unit,
    onViewOnMap: (String) -> Unit,
) {
    val allSignals by InMemorySignalRepository.signals.collectAsState()
    val starSignals = remember(allSignals, starId) { filterForStar(allSignals, starId) }
    val wishArchive = remember(starSignals) { starSignals.filter { it.category == SignalCategory.WishNet } }

    // bod 5: prvá hodnota = tá istá, akú má hviezda na mape (farba, jazyk, téma)
    val dominant = remember(starId) { starEmotionForId(starId) }
    val langs = remember(starId) { (listOf(starLangForId(starId)) + langsForStar(starId)).distinct().take(3) }
    val topics = remember(starId) { (listOf(starTopicForId(starId)) + topicsForStar(starId)).distinct().take(3) }
    val domEmotions = remember(starId) { (listOf(dominant) + emotionsForStar(starId)).distinct().take(3) }

    var showReply by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // bod 5: pošli pulz + sleduj žiarenie
    val watchedIds by WatchedStars.watched.collectAsState()
    val isWatched = starId in watchedIds
    var showPulse by remember { mutableStateOf(false) }
    val pulseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pulseSentAt by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(pulseSentAt) {
        if (pulseSentAt != null) {
            delay(2500)
            pulseSentAt = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = tr("Profil hviezdy")) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = tr("Späť"),
                    )
                }
            },
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .height(64.dp)
                            .fillMaxWidth(0.18f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                Color.White,
                                                dominant.color(),
                                                Color(0x00000000),
                                            ),
                                    ),
                                ),
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = QuantName.of(starId), fontWeight = FontWeight.SemiBold)
                    Text(text = tr("Jazyky: %s", langs.joinToString()))
                    Text(text = tr("Prevláda: %s", tr(dominant.label())), color = dominant.color())
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(text = tr("Emócie"), fontWeight = FontWeight.SemiBold)
            LazyRow(contentPadding = PaddingValues(vertical = 6.dp)) {
                items(domEmotions) { e ->
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text(e.label()) },
                        modifier = Modifier.padding(end = 6.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = e.color().copy(alpha = 0.25f),
                            ),
                    )
                }
            }

            Text(text = tr("Témy"), fontWeight = FontWeight.SemiBold)
            LazyRow(contentPadding = PaddingValues(vertical = 6.dp)) {
                items(topics) { t ->
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text(t) },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider()

        // ----------- ARCHÍV PRIANÍ -----------
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = tr("Archív prianí"), fontWeight = FontWeight.SemiBold)
            if (wishArchive.isEmpty()) {
                Text(text = tr("Zatiaľ žiadne priania."))
            } else {
                LazyRow(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(wishArchive) { s ->
                        ElevatedCard(modifier = Modifier.padding(end = 8.dp)) {
                            WishNetCard(
                                signal = s,
                                onRead = {
                                    // lokálne demo
                                },
                            )
                        }
                    }
                }
            }
        }

        // Posledné signály
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = tr("Posledné signály"), fontWeight = FontWeight.SemiBold)
            if (starSignals.isEmpty()) {
                Text(text = tr("Zatiaľ žiadne signály."))
            } else {
                starSignals.take(6).forEach { s ->
                    ElevatedCard(
                        modifier =
                            Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            when (s) {
                                is TextSignal -> LinguaText(s.text)
                                is MixSignal -> Text(text = tr("Mix (%s)", s.parts.size))
                                is EmojiSignal -> Text(text = s.emoji)
                                else -> Text(text = s.typeLabel())
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (pulseSentAt != null) {
            Text(
                text = tr("✦ Pulz letí k hviezde"),
                color = dominant.color(),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        // bod 5: pošli pulz + sleduj žiarenie (nie follow)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalButton(
                onClick = { showPulse = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = tr("✦ Pošli pulz"))
            }
            FilterChip(
                selected = isWatched,
                onClick = { WatchedStars.toggle(starId) },
                label = { Text(text = if (isWatched) tr("◉ Sleduješ žiarenie") else tr("○ Sleduj žiarenie")) },
                modifier = Modifier.weight(1f),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = dominant.color().copy(alpha = 0.25f),
                    ),
            )
        }

        // bod 23: trvalé spojenie 1:1 po obojstrannom súhlase
        Row(modifier = Modifier.padding(horizontal = 8.dp)) { SoulLinkOffer(starId) }

        // bod 19: nahlásiť / zablokovať – po zablokovaní sa vrátime späť
        ModerationRow(starId, onBlocked = onBack)

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { showReply = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = tr("Odpovedať"))
            }
            Button(
                onClick = { onViewOnMap(starId) },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = tr("Zobraziť na mape"))
            }
        }
    }

    if (showPulse) {
        ModalBottomSheet(
            onDismissRequest = { showPulse = false },
            sheetState = pulseSheetState,
        ) {
            SendPulseSheet(
                target = SignalTarget.Star(starId),
                onSend = { emotion, emoji, effect, urgent ->
                    PulseOutbox.send(
                        target = SignalTarget.Star(starId),
                        emotion = emotion,
                        visibleStarIds = emptyList(),
                        emoji = emoji,
                        effect = effect,
                        urgent = urgent,
                    )
                    showPulse = false
                    pulseSentAt = System.currentTimeMillis()
                },
            )
        }
    }

    if (showReply) {
        ReplySheet(
            onDismiss = { showReply = false },
            onPick = { _ ->
                showReply = false
            },
            sheetState = sheetState,
        )
    }
}

private enum class ReplyType { Text, Emoji, Audio15s, Flash, Image, Mix, Wish }

@Composable
private fun ReplySheet(
    onDismiss: () -> Unit,
    onPick: (ReplyType) -> Unit,
    sheetState: androidx.compose.material3.SheetState,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(text = tr("Odpovedať ako"), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            val options =
                listOf(
                    ReplyType.Text to tr("Text (200)"),
                    ReplyType.Emoji to tr("Emotikon"),
                    ReplyType.Audio15s to tr("Audio 15s"),
                    ReplyType.Flash to tr("Záblesk"),
                    ReplyType.Image to tr("Mini vizuál"),
                    ReplyType.Mix to tr("Mix"),
                    ReplyType.Wish to tr("WishNet prianie"),
                )

            options.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { (type, label) ->
                        Button(
                            onClick = { onPick(type) },
                            modifier = Modifier.weight(1f),
                        ) { Text(text = label) }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ----------------- mock prepojenia „hviezda ↔ signál“ -------------------

private fun filterForStar(
    all: List<Signal>,
    starId: String,
): List<Signal> {
    val k = 7
    val key = abs(starId.hashCode() % k)
    return all.filter { abs(it.id.hashCode() % k) == key }
}

private fun langsForStar(starId: String): List<String> {
    val langs = WorldLanguages.codes
    val rnd = Random(starId.hashCode())
    return langs.shuffled(rnd).take(1 + rnd.nextInt(3))
}

private fun topicsForStar(starId: String): List<String> {
    val t = listOf("Tech", "Life", "Art", "Nature", "Music")
    val rnd = Random(starId.hashCode() xor 0x7777)
    return t.shuffled(rnd).take(2)
}

private fun emotionsForStar(starId: String): List<Emotion> {
    val rnd = Random(starId.hashCode() xor 0x12345)
    return Emotion.values().toList().shuffled(rnd).take(3)
}
