package app.quantumsocial.ui.galaxies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.FriendGalaxy
import app.quantumsocial.core.model.GalaxyCampfire
import app.quantumsocial.core.model.GalaxyRules
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.ui.signals.EmotionOrb

/** Motto, ⭐ obľúbená, ✎ úprava (len jadro) a otázka dňa z táboráka. */
@Composable
fun GalaxyHeaderExtras(
    galaxy: FriendGalaxy,
    me: String,
    isFavorite: Boolean,
) {
    val store = GalaxyStore.shared
    var editing by remember { mutableStateOf(false) }

    if (galaxy.motto.isNotEmpty()) Text("„${galaxy.motto}“", fontStyle = FontStyle.Italic)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = { store.toggleFavorite(galaxy.id) }) {
            Text(if (isFavorite) "⭐ Obľúbená" else "☆ Obľúbiť")
        }
        if (galaxy.coreStarId == me) TextButton(onClick = { editing = true }) { Text("✎ Upraviť") }
    }
    val today = StarDustState.epochDay(System.currentTimeMillis())
    Text("🔥 Táborák: ${GalaxyCampfire.questionFor(galaxy.id, today)}", style = MaterialTheme.typography.bodyMedium)

    if (editing) {
        var name by remember { mutableStateOf(galaxy.name) }
        var motto by remember { mutableStateOf(galaxy.motto) }
        var emotion by remember { mutableStateOf(galaxy.emotion) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Upraviť galaxiu") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(GalaxyRules.MAX_NAME) },
                        label = { Text("Meno") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = motto,
                        onValueChange = { motto = it.take(GalaxyRules.MAX_MOTTO) },
                        label = { Text("Motto (max ${GalaxyRules.MAX_MOTTO})") },
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Emotion.values().forEach { e ->
                            EmotionOrb(emotion = e, selected = e == emotion) { emotion = e }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { if (store.edit(galaxy.id, me, name, motto, emotion)) editing = false },
                    enabled = GalaxyRules.cleanName(name) != null,
                ) { Text("Uložiť") }
            },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Zrušiť") } },
        )
    }
}
