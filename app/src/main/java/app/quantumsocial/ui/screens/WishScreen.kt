@file:OptIn(ExperimentalMaterial3Api::class)

package app.quantumsocial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.ModerationStore
import app.quantumsocial.core.data.ReplyStore
import app.quantumsocial.core.data.WishBoardStore
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.WishSort
import app.quantumsocial.core.model.WishTheme
import app.quantumsocial.core.model.asWishNet
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.signals.ReplySheet
import app.quantumsocial.ui.signals.SignalAudioRecorder
import app.quantumsocial.ui.signals.SignalEmoji
import app.quantumsocial.ui.signals.SignalFlash
import app.quantumsocial.ui.signals.SignalImagePicker
import app.quantumsocial.ui.signals.SignalMix
import app.quantumsocial.ui.signals.SignalText
import app.quantumsocial.ui.signals.WishCardExtras
import app.quantumsocial.ui.signals.WishNetCard
import app.quantumsocial.ui.signals.WishThemeRow

@Composable
fun WishScreen() {
    val signals by InMemorySignalRepository.signals.collectAsState()
    val moderation by ModerationStore.state.collectAsState()
    val fadingSince by InMemorySignalRepository.fadingSince.collectAsState()
    val threads by ReplyStore.threads.collectAsState()
    var replyTo by remember { mutableStateOf<String?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val board by WishBoardStore.board.collectAsState()
    var sendTheme by remember { mutableStateOf<WishTheme?>(null) }
    var filterTheme by remember { mutableStateOf<WishTheme?>(null) }
    var sort by remember { mutableStateOf(WishSort.Newest) }
    val post: (Signal) -> Unit = {
        val wish = it.asWishNet()
        InMemorySignalRepository.send(wish)
        WishBoardStore.post(wish.id, sendTheme)
    }
    val tabs = listOf("Text", "Emoji", "Zvuk", "Záblesk", "Obrázok", "Mix")

    // jemná pastelová obloha, aby WishNet sedel k veselej galaxii a tmavý text ostal čitateľný
    Column(modifier = Modifier.fillMaxSize().background(GalaxyPalette.softSkyBrush)) {
        // posúvateľné karty – názvy sa nelámu
        ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 8.dp) {
            tabs.forEachIndexed { i, t ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(t) })
            }
        }

        Text("Téma priania", modifier = Modifier.padding(start = 12.dp, top = 6.dp))
        WishThemeRow(selected = sendTheme, none = "Bez témy") { sendTheme = it }

        when (tabIndex) {
            // signály z WishNet žijú ako WishNet (24 h), nie 30 s
            0 -> SignalText { post(it) }
            1 -> SignalEmoji { post(it) }
            2 -> SignalAudioRecorder { post(it) }
            3 -> SignalFlash { post(it) }
            4 -> SignalImagePicker { post(it) }
            5 -> SignalMix(available = signals) { post(it) }
        }

        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Priania")
            WishSort.values().forEach { s ->
                TextButton(onClick = { sort = s }) { Text(if (sort == s) "● ${s.label}" else s.label) }
            }
        }
        WishThemeRow(selected = filterTheme, none = "Všetko") { filterTheme = it }

        val shown = board.arrange(moderation.visible(signals) { it.id }, { it.id }, { it.createdAt }, filterTheme, sort)
        LazyColumn {
            items(shown, key = Signal::id) { s ->
                WishNetCard(
                    signal = s,
                    fadingSince = fadingSince[s.id],
                    replies = threads[s.id]?.replies.orEmpty(),
                    onReply = { replyTo = s.id },
                    extras = {
                        WishCardExtras(
                            wishId = s.id,
                            board = board,
                            onCheer = { WishBoardStore.toggleCheer(s.id) },
                            onFulfill = { WishBoardStore.fulfill(s.id) },
                        )
                    },
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
