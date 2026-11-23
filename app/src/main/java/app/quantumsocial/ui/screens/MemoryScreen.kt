package app.quantumsocial.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Memory
import app.quantumsocial.core.model.MemoryNebula
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.security.SealedText
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.security.KeyVault
import app.quantumsocial.ui.signals.EmotionOrb
import java.util.UUID

/** Uloženie archívu spomienok – len zašifrovane (Android Keystore) v trezore, ktorý sa nezálohuje. */
private object MemoryVault {
    private const val VAULT = "quantum_vault"
    private const val KEY = "memories"

    fun load(context: Context): MemoryNebula {
        val stored = prefs(context).getString(KEY, null) ?: return MemoryNebula()
        val plain = SealedText.decode(stored)?.let(KeyVault::open) ?: return MemoryNebula()
        return MemoryNebula.decode(plain)
    }

    fun save(
        context: Context,
        nebula: MemoryNebula,
    ) {
        prefs(context).edit().putString(KEY, KeyVault.seal(nebula.encode()).encode()).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(VAULT, Context.MODE_PRIVATE)
}

/** Memory Nebula (bod 24): súkromný šifrovaný archív výnimočných chvíľ. Nezhasína a nikam neodchádza. */
@Composable
fun MemoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var nebula by remember { mutableStateOf(MemoryNebula()) }
    var text by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf(Emotion.Love) }
    var filter by remember { mutableStateOf<Emotion?>(null) }
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val onSky = Color(GalaxyPalette.ON_SKY)

    LaunchedEffect(Unit) { nebula = MemoryVault.load(context) }

    fun update(next: MemoryNebula) {
        nebula = next
        MemoryVault.save(context, next)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextButton(onClick = onBack) { Text(tr("‹ Späť"), color = onSky) }
        Text(tr("🔮 Memory Nebula"), style = MaterialTheme.typography.titleLarge, color = onSky)
        Text(tr("Tvoje výnimočné chvíle. Zašifrované, len v tomto telefóne, nikto iný ich neuvidí."), color = onSky)

        nebula.onThisDay(today).firstOrNull()?.let { m ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    tr("🌠 V tento deň pred %s r.: %s", (today - m.day) / 365, m.text),
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(MemoryNebula.MAX_TEXT) },
            label = { Text(tr("výnimočná chvíľa (max %s)", MemoryNebula.MAX_TEXT)) },
            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Emotion.values().forEach { e -> EmotionOrb(emotion = e, selected = e == emotion) { emotion = e } }
        }
        Button(
            onClick = {
                nebula.add(Memory(UUID.randomUUID().toString(), today, emotion, text))?.let {
                    update(it)
                    text = ""
                }
            },
            enabled = text.isNotBlank(),
        ) { Text(tr("Uložiť do hmloviny 🔮")) }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item { FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text(tr("Všetky")) }) }
            items(Emotion.values()) { e ->
                FilterChip(
                    selected = filter == e,
                    onClick = { filter = if (filter == e) null else e },
                    label = { Text(e.label()) },
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nebula.byEmotion(filter), key = { it.id }) { m ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.text)
                            val ago = today - m.day
                            Text(
                                tr("%s · ", tr(m.emotion.label())) + if (ago == 0L) "dnes" else "pred $ago d",
                                style = MaterialTheme.typography.bodySmall,
                                color = m.emotion.color(),
                            )
                        }
                        TextButton(onClick = { update(nebula.remove(m.id)) }) { Text(tr("🗑")) }
                    }
                }
            }
        }
    }
}
