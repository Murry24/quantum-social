@file:OptIn(ExperimentalMaterial3Api::class)

package app.quantumsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.ReplyStore
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.asWishNet
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.signals.ReplySheet
import app.quantumsocial.ui.signals.SignalAudioRecorder
import app.quantumsocial.ui.signals.SignalEmoji
import app.quantumsocial.ui.signals.SignalFlash
import app.quantumsocial.ui.signals.SignalImagePicker
import app.quantumsocial.ui.signals.SignalMix
import app.quantumsocial.ui.signals.SignalText
import app.quantumsocial.ui.signals.WishNetCard

@Composable
fun WishScreen() {
    val signals by InMemorySignalRepository.signals.collectAsState()
    val fadingSince by InMemorySignalRepository.fadingSince.collectAsState()
    val threads by ReplyStore.threads.collectAsState()
    var replyTo by remember { mutableStateOf<String?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text", "Emoji", "Zvuk", "Záblesk", "Obrázok", "Mix")

    // jemná pastelová obloha, aby WishNet sedel k veselej galaxii a tmavý text ostal čitateľný
    Column(modifier = Modifier.fillMaxSize().background(GalaxyPalette.softSkyBrush)) {
        // posúvateľné karty – názvy sa nelámu
        ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 8.dp) {
            tabs.forEachIndexed { i, t ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(t) })
            }
        }

        when (tabIndex) {
            // signály z WishNet žijú ako WishNet (24 h), nie 30 s
            0 -> SignalText { InMemorySignalRepository.send(it.asWishNet()) }
            1 -> SignalEmoji { InMemorySignalRepository.send(it.asWishNet()) }
            2 -> SignalAudioRecorder { InMemorySignalRepository.send(it.asWishNet()) }
            3 -> SignalFlash { InMemorySignalRepository.send(it.asWishNet()) }
            4 -> SignalImagePicker { InMemorySignalRepository.send(it.asWishNet()) }
            5 -> SignalMix(available = signals) { InMemorySignalRepository.send(it.asWishNet()) }
        }

        LazyColumn {
            items(signals, key = Signal::id) { s ->
                WishNetCard(
                    signal = s,
                    fadingSince = fadingSince[s.id],
                    replies = threads[s.id]?.replies.orEmpty(),
                    onReply = { replyTo = s.id },
                ) { read ->
                    InMemorySignalRepository.markRead(read.id)
                }
            }
        }
    }

    // bod 7: odpovedať svetlom
    replyTo?.let { id ->
        ModalBottomSheet(
            onDismissRequest = { replyTo = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            ReplySheet { reply ->
                ReplyStore.reply(id, reply)
                replyTo = null
            }
        }
    }
}
