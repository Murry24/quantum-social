package app.quantumsocial.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.model.Signal
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
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text", "Emoji", "Audio", "Záblesk", "Obrázok", "Mix")

    Column {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { i, t ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(t) })
            }
        }

        when (tabIndex) {
            0 -> SignalText { InMemorySignalRepository.send(it) }
            1 -> SignalEmoji { InMemorySignalRepository.send(it) }
            2 -> SignalAudioRecorder { InMemorySignalRepository.send(it) }
            3 -> SignalFlash { InMemorySignalRepository.send(it) }
            4 -> SignalImagePicker { InMemorySignalRepository.send(it) }
            5 -> SignalMix(available = signals) { InMemorySignalRepository.send(it) }
        }

        LazyColumn {
            items(signals, key = Signal::id) { s -> WishNetCard(signal = s) }
        }
    }
}
