package app.quantumsocial.ui.signals

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.AudioSignal
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.FlashSignal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.TextSignal
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun WishNetCard(
    signal: Signal,
    onRead: (Signal) -> Unit,
) {
    val now by produceState(initialValue = System.currentTimeMillis(), signal.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(500)
        }
    }
    val remaining = signal.remainingMillis(now)
    val expiringWindow = 5_000L
    val isExpiring = remaining != null && remaining in 1..expiringWindow

    val alpha by animateFloatAsState(
        targetValue = if (isExpiring) (remaining!!.toFloat() / expiringWindow).coerceIn(0.2f, 1f) else 1f,
        label = "fade-out",
    )

    AnimatedVisibility(
        visible = !signal.isExpired(now),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier =
                Modifier
                    .padding(8.dp)
                    .alpha(alpha)
                    .clickable { onRead(signal) },
        ) {
            Column(Modifier.padding(12.dp)) {
                val badge =
                    when (signal.category) {
                        SignalCategory.Ephemeral -> "⏳ mizne"
                        SignalCategory.WishNet -> "💫 WishNet"
                    }
                Text("$badge • ${signal::class.simpleName}")

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

                remaining?.let {
                    if (it > 0) {
                        Text("Zostáva: ${it / 1000}s", modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}
