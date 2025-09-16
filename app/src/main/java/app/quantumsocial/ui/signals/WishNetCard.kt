package app.quantumsocial.ui.signals

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.AudioSignal
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.FlashSignal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.TextSignal
import coil.compose.AsyncImage

@Composable
fun WishNetCard(signal: Signal) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.padding(8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = signal::class.simpleName ?: "Signal")
            when (signal) {
                is TextSignal -> Text(signal.text, modifier = Modifier.padding(top = 6.dp))
                is EmojiSignal -> Text(signal.emoji, modifier = Modifier.padding(top = 6.dp))
                is ImageSignal ->
                    AsyncImage(
                        model = Uri.parse(signal.imageUri),
                        contentDescription = "obrázok",
                        modifier = Modifier.padding(top = 6.dp),
                    )
                is AudioSignal ->
                    Text(
                        "🎧 Audio: ${signal.durationMs / 1000}s",
                        modifier = Modifier.padding(top = 6.dp),
                    )
                is FlashSignal ->
                    Text(
                        "⚡ Záblesk: ${(signal.intensity * 100).toInt()}%",
                        modifier = Modifier.padding(top = 6.dp),
                    )
                is MixSignal -> Text("Mix ${signal.parts.size} prvkov", modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
