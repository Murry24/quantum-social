package app.quantumsocial.ui.signals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.EmojiSignal

@Composable
fun SignalEmoji(onSubmit: (EmojiSignal) -> Unit) {
    val emojis = listOf("✨", "💙", "🤍", "🌌", "⭐", "🚀", "🪐")
    Column(Modifier.padding(16.dp)) {
        Text("Vyber emoji:", modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            emojis.forEach { e ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.clickable { onSubmit(EmojiSignal(emoji = e)) },
                ) { Text(e, modifier = Modifier.padding(12.dp)) }
            }
        }
    }
}
