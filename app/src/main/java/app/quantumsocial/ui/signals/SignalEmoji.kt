package app.quantumsocial.ui.signals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.EmojiSignal

@Composable
fun SignalEmoji(onSubmit: (EmojiSignal) -> Unit) {
    Column(Modifier.padding(8.dp)) {
        Text("Ťukni na emoji – hneď ho pošleš:", modifier = Modifier.padding(horizontal = 8.dp))
        EmojiGrid { e -> onSubmit(EmojiSignal(emoji = e)) }
    }
}
