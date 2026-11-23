package app.quantumsocial.ui.galaxies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.EnterResult
import app.quantumsocial.core.model.GalaxyRules
import app.quantumsocial.ui.screens.color
import app.quantumsocial.ui.signals.EmotionOrb
import kotlin.math.roundToInt

private enum class Section { None, Create, Enter }

/** Panel „Moje galaxie“ (bod 31): zoznam, založenie novej, vstup kódom čiernej diery. */
@Composable
fun MyGalaxiesSheet(
    myStarId: String,
    onOpen: (String) -> Unit,
) {
    val store = GalaxyStore.shared
    val all by store.galaxies.collectAsState()
    val now = System.currentTimeMillis()
    val favorites by store.favorites.collectAsState()
    // ⭐ obľúbené hore
    val mine = remember(all, favorites) { store.galaxiesOf(myStarId, now).sortedByDescending { it.id in favorites } }

    var section by remember { mutableStateOf(Section.None) }
    var name by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf(Emotion.Joy) }
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(tr("Moje galaxie"), style = MaterialTheme.typography.titleLarge)
        if (mine.isEmpty()) Text(tr("Zatiaľ nežiariš v žiadnej malej galaxii."))
        mine.forEach { g ->
            val light = g.brightness(now)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onOpen(g.id) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(g.emotion.color().copy(alpha = 0.3f + 0.7f * light), CircleShape),
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text((if (g.id in favorites) tr("⭐ ") else "") + g.name, fontWeight = FontWeight.SemiBold)
                    val role = if (g.coreStarId == myStarId) tr(" · si jadro") else ""
                    Text(tr("%s hviezd · jas %s %%s", g.members.size, (light * 100).roundToInt(), role))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { section = Section.Create }) { Text(tr("+ Založiť galaxiu")) }
            TextButton(onClick = { section = Section.Enter }) { Text(tr("Vstúpiť čiernou dierou")) }
        }

        when (section) {
            Section.Create -> {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(GalaxyRules.MAX_NAME) },
                    label = { Text(tr("Meno galaxie (max %s)", GalaxyRules.MAX_NAME)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Emotion.values().forEach { e -> EmotionOrb(emotion = e, selected = e == emotion) { emotion = e } }
                }
                Button(onClick = {
                    val g = store.create(name, emotion, myStarId)
                    if (g != null) {
                        StarDust.earn(DustAction.Galaxy)
                        onOpen(g.id)
                    } else {
                        message = tr("Nedá sa: meno nesmie byť prázdne, max 2 vlastné a 5 galaxií.")
                    }
                }) { Text(tr("Založiť")) }
            }
            Section.Enter -> {
                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        code = input.uppercase().filter(Char::isLetterOrDigit).take(GalaxyRules.CODE_LENGTH)
                    },
                    label = { Text(tr("Kód čiernej diery")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = {
                    when (val r = store.enter(code, myStarId)) {
                        EnterResult.Entered, EnterResult.AlreadyMember -> store.galaxyIdForCode(code)?.let(onOpen)
                        else -> message = r.message()
                    }
                }) { Text(tr("Vletieť")) }
            }
            Section.None -> Unit
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

private fun EnterResult.message(): String =
    when (this) {
        EnterResult.Entered, EnterResult.AlreadyMember -> ""
        EnterResult.Expired -> tr("Táto čierna diera už zhasla.")
        EnterResult.Full -> tr("Galaxia je plná (%s hviezd).", GalaxyRules.MAX_MEMBERS)
        EnterResult.GalaxyClosed -> tr("Čierna diera je práve zatvorená.")
        EnterResult.TooManyGalaxies ->
            tr("Už žiariš v %s galaxiách – najprv z jednej odleť.", GalaxyRules.MAX_GALAXIES_PER_STAR)
        EnterResult.Unknown -> tr("Taký kód vesmír nepozná.")
    }
