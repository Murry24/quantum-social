package app.quantumsocial.ui.signals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.ReplyThread

/** Odpovedať svetlom (bod 7): vyber emóciu alebo napíš pár slov, potom „Poslať iskru“. */
@Composable
fun ReplySheet(onSend: (Reply) -> Unit) {
    var text by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf<Emotion?>(null) }
    val reply = Reply.text(text) ?: emotion?.let { Reply.emotion(it) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(tr("Odpovedať svetlom"), style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Emotion.values().forEach { e ->
                EmotionOrb(emotion = e, selected = e == emotion) { emotion = e }
            }
        }
        // emoji ide ako krátky text odpovede
        EmojiStrip(selected = text.takeIf { it.isNotEmpty() }) { text = it.orEmpty() }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(ReplyThread.MAX_TEXT) },
            label = { Text(tr("alebo pár slov (max %s)", ReplyThread.MAX_TEXT)) },
            modifier = Modifier.fillMaxWidth(),
        )
        // text má prednosť, inak vybraná emócia
        Button(onClick = { reply?.let(onSend) }, enabled = reply != null) { Text(tr("Poslať iskru ✦")) }
    }
}
