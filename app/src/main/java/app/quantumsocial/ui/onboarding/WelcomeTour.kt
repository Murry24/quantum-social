package app.quantumsocial.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.ui.theme.GalaxyPalette

private data class TourPage(
    val emoji: String,
    val title: String,
    val text: String,
)

// texty sú kľúče slovníka (preklad cez tr)
private val pages =
    listOf(
        TourPage("⭐", "Si hviezda", "Bez mena a bez fotky. V galaxii ťa ostatní spoznajú len podľa svetla a nálady."),
        TourPage("✦", "Pošli pocit", "Ťukni na hviezdu na mape a pošli jej pulz pocitu. Signály po chvíli zhasnú."),
        TourPage("💫", "WishNet", "Napíš prianie do vesmíru. Žije 24 hodín a iní ti môžu držať palce."),
        TourPage("🌌", "Tlačidlo ✦", "Stredné tlačidlo dole skrýva ticho, sny, semienka, galaxie priateľov a ďalšie."),
        TourPage("✨", "StarDust", "Za dobré skutky zbieraš hviezdny prach a rastieš: od Iskry až po Supernovu."),
    )

/** Sprievodca prvými krokmi – 5 krátkych obrazoviek po zrodení hviezdy. */
@Composable
fun WelcomeTour(onDone: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val page = pages[index]
    val onSky = Color(GalaxyPalette.ON_SKY)
    val last = index == pages.lastIndex

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(GalaxyPalette.zenBrush)
                // nech ťuknutia neprepadnú na mapu pod sprievodcom
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(page.emoji, fontSize = 64.sp)
            Text(tr(page.title), style = MaterialTheme.typography.headlineSmall, color = onSky)
            Text(tr(page.text), color = onSky, textAlign = TextAlign.Center)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { i ->
                    Box(
                        modifier =
                            Modifier
                                .size(if (i == index) 10.dp else 7.dp)
                                .background(onSky.copy(alpha = if (i == index) 1f else 0.4f), CircleShape),
                    )
                }
            }
            Button(onClick = { if (last) onDone() else index++ }) {
                Text(if (last) tr("Poďme svietiť ✦") else tr("Ďalej"))
            }
            if (!last) TextButton(onClick = onDone) { Text(tr("Preskočiť"), color = onSky) }
        }
    }
}
