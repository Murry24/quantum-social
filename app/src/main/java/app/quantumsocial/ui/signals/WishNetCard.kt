package app.quantumsocial.ui.signals

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.model.AudioSignal
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.FlashSignal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.LifePhase
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.PulseSignal
import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.ReplyThread
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.SignalLifecycle
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.lingua.LinguaText
import app.quantumsocial.ui.screens.color
import app.quantumsocial.ui.screens.label
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/**
 * Karta signálu so životným cyklom (bod 3):
 * - Alive: normálne
 * - Expiring: posledných 5 s bledne, štítok „končí“
 * - FadingOut: po prečítaní / vypršaní zhasne (stmavne a zmenší sa), potom ju repo odstráni
 */
@Composable
fun WishNetCard(
    signal: Signal,
    fadingSince: Long? = null,
    // bod 7: odpovede – iskry sú v karte, preto zhasínajú spolu so signálom
    replies: List<Reply> = emptyList(),
    onReply: (() -> Unit)? = null,
    // WishNet+: téma, palce, splnené
    extras: (@Composable () -> Unit)? = null,
    onRead: (Signal) -> Unit,
) {
    val now by produceState(initialValue = System.currentTimeMillis(), signal.id) {
        while (true) {
            value = System.currentTimeMillis()
            delay(100)
        }
    }
    val phase = SignalLifecycle.phase(signal, now, fadingSince)
    val brightness = SignalLifecycle.brightness(signal, now, fadingSince)

    val alpha by animateFloatAsState(
        targetValue = brightness,
        animationSpec = tween(durationMillis = 150),
        label = "signal-brightness",
    )
    val scale by animateFloatAsState(
        targetValue = if (phase == LifePhase.FadingOut) 0.92f + 0.08f * brightness else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "signal-scale",
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier =
            Modifier
                .padding(8.dp)
                .scale(scale)
                .alpha(alpha)
                .clickable(enabled = phase != LifePhase.FadingOut) { onRead(signal) },
    ) {
        Column(Modifier.padding(12.dp)) {
            val badge =
                when {
                    phase == LifePhase.FadingOut -> "✨ zhasína"
                    phase == LifePhase.Expiring -> "⌛ končí"
                    signal.category == SignalCategory.WishNet -> "💫 WishNet"
                    else -> "⏳ mizne"
                }
            Text("$badge • ${signal.typeLabel()}")

            when (signal) {
                // bod 25: cudzí text rovno preložený do môjho jazyka
                is TextSignal -> LinguaText(signal.text, modifier = Modifier.padding(top = 6.dp))
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
                is MixSignal ->
                    Text(
                        "Mix ${signal.parts.size} prvkov",
                        modifier = Modifier.padding(top = 6.dp),
                    )
                is PulseSignal ->
                    Text(
                        "✦ Pulz: ${signal.emotion.label()}",
                        modifier = Modifier.padding(top = 6.dp),
                    )
            }

            val remaining = signal.remainingMillis(now)
            if (phase != LifePhase.FadingOut && remaining != null && remaining > 0) {
                Text("Zostáva: ${remainingText(remaining)}", modifier = Modifier.padding(top = 6.dp))
            }

            if (phase != LifePhase.FadingOut) extras?.invoke()

            replies.forEach { r ->
                Row(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                    // farebná iskra, text v bežnej farbe (žltá na bielej by bola nečitateľná)
                    Text("✦ ", color = r.emotion?.color() ?: Color.Unspecified, fontSize = 13.sp)
                    Text(r.text ?: r.emotion?.label().orEmpty(), fontSize = 13.sp)
                }
            }
            if (onReply != null && phase != LifePhase.FadingOut && replies.size < ReplyThread.MAX_REPLIES) {
                TextButton(onClick = onReply) { Text("Odpovedať svetlom") }
            }
        }
    }
}

/** Slovenský názov typu signálu (namiesto technického názvu triedy). */
fun Signal.typeLabel(): String =
    when (this) {
        is TextSignal -> "Text"
        is EmojiSignal -> "Emoji"
        is AudioSignal -> "Zvuk"
        is ImageSignal -> "Obrázok"
        is FlashSignal -> "Záblesk"
        is MixSignal -> "Mix"
        is PulseSignal -> "Pulz"
    }

// sekundy len pri krátkych signáloch, WishNet žije hodiny
private fun remainingText(ms: Long): String {
    val s = (ms + 999) / 1000
    return when {
        s < 60 -> "$s s"
        s < 3600 -> "${s / 60} min"
        else -> "${s / 3600} h"
    }
}
