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
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.ReplyThread

/** Odpovedať svetlom (bod 7): ťuk na emóciu ju hneď pošle, alebo napíš krátky text. */
@Composable
fun ReplySheet(onSend: (Reply) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Odpovedať svetlom", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Emotion.values().forEach { e ->
                EmotionOrb(emotion = e, selected = false) { onSend(Reply.emotion(e)) }
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(ReplyThread.MAX_TEXT) },
            label = { Text("alebo pár slov (max ${ReplyThread.MAX_TEXT})") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = { Reply.text(text)?.let(onSend) }, enabled = text.isNotBlank()) { Text("Poslať iskru ✦") }
    }
}
