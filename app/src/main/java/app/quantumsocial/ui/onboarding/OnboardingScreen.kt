package app.quantumsocial.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.OnboardingState
import app.quantumsocial.core.model.OnboardingStep
import app.quantumsocial.core.model.QuantName
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.canvas.FireflyLayer
import app.quantumsocial.ui.canvas.NebulaBackground
import app.quantumsocial.ui.signals.EmotionOrb
import java.util.UUID

// rovnaké jazyky ako filter na mape
// predvolený jazyk; výber je z celého sveta (WorldLanguages)
private const val DEFAULT_LANGUAGE = "SK"

private val stateSaver =
    listSaver<OnboardingState, Any>(
        save = { listOf(it.step.name, it.keyConfirmed) },
        restore = { OnboardingState(step = OnboardingStep.valueOf(it[0] as String), keyConfirmed = it[1] as Boolean) },
    )

/** Onboarding „Zrodenie hviezdy“ (bod 18) – kroky a pravidlá sú v core [OnboardingState]. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var state by rememberSaveable(stateSaver = stateSaver) { mutableStateOf(OnboardingState()) }
    var language by rememberSaveable { mutableStateOf(DEFAULT_LANGUAGE) }
    var emotion by rememberSaveable { mutableStateOf(Emotion.Joy) }
    var starId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var notifications by rememberSaveable { mutableStateOf(false) }
    val key = remember { OnboardingPrefs.recoveryKey(context) }

    LaunchedEffect(state.done) {
        if (state.done) {
            OnboardingPrefs.finish(context, language, emotion, starId, notifications)
            onDone()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NebulaBackground(offsetX = 0f, offsetY = 0f, scale = 1f)
        FireflyLayer()
        CompositionLocalProvider(LocalContentColor provides Color(GalaxyPalette.ON_SKY)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                when (state.step) {
                    OnboardingStep.Birth -> BirthStep()
                    OnboardingStep.Language -> {
                        Title("Akým jazykom žiariš?")
                        // celý svet, nielen SK / EN / CZ
                        LanguagePicker(selected = language) { language = it }
                    }
                    OnboardingStep.Emotion -> {
                        Title("Aký pocit dnes vyžaruješ?")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Emotion.values().forEach { e ->
                                EmotionOrb(emotion = e, selected = e == emotion, onClick = { emotion = e })
                            }
                        }
                    }
                    OnboardingStep.Name -> {
                        Title("Tvoje kvanti-meno")
                        Text(QuantName.of(starId), style = MaterialTheme.typography.headlineMedium)
                        OutlinedButton(onClick = { starId = UUID.randomUUID().toString() }) { Text("Iné meno") }
                    }
                    OnboardingStep.RecoveryKey -> {
                        Title("Tvoj obnovovací kľúč")
                        ElevatedCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                key.chunked(2).forEachIndexed { row, pair ->
                                    Text(pair.mapIndexed { col, w -> "${row * 2 + col + 1}. $w" }.joinToString("     "))
                                }
                            }
                        }
                        Body("Zapíš si ich – podľa nich obnovíš svoju hviezdu.")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = state.keyConfirmed, onCheckedChange = { state = state.confirmKey(it) })
                            Text("Zapísal som si ich")
                        }
                    }
                    OnboardingStep.Notifications -> NotificationsStep(notifications) { notifications = it }
                    OnboardingStep.Tutorial -> {
                        Title("Ako sa tu svieti")
                        TutorialCard("✦ Ťukni na hviezdu", "Pošleš jej pulz pocitu.")
                        TutorialCard("☄ Podrž hviezdu", "Otvorí sa jej profil.")
                        TutorialCard("○ ZenNet", "Minúta ticha len pre teba.")
                    }
                }

                Spacer(Modifier.height(8.dp))
                if (state.done) {
                    // kým sa načíta mapa, nech je vidieť, že sa niečo deje
                    CircularProgressIndicator(color = Color(GalaxyPalette.ON_SKY))
                } else {
                    Button(onClick = { state = state.next() }, enabled = state.canContinue) {
                        Text(if (state.step == OnboardingStep.Birth) "Zrodiť hviezdu ✦" else "Pokračovať")
                    }
                }
                if (state.canSkip) {
                    // biela, nie modrá tlačidla – na modrej oblohe by nebola čitateľná
                    TextButton(
                        onClick = { state = state.skip() },
                    ) { Text("Preskočiť", color = Color(GalaxyPalette.ON_SKY)) }
                }
            }
        }
    }
}

@Composable
private fun BirthStep() {
    // iskra sa pomaly rozžiari na hviezdu
    val grow = remember { Animatable(0f) }
    LaunchedEffect(Unit) { grow.animateTo(1f, tween(2200, easing = FastOutSlowInEasing)) }
    val glow = Color(GalaxyPalette.fireflies.first())
    Canvas(modifier = Modifier.size(180.dp)) {
        val r = size.minDimension / 2 * (0.05f + 0.95f * grow.value)
        drawCircle(Brush.radialGradient(listOf(Color.White, glow, Color.Transparent), center, r), r)
    }
    Title("Vo vesmíre sa rodí nová hviezda")
    Body("Bez mena, bez fotky – len tvoje svetlo.")
}

@Composable
private fun NotificationsStep(
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onChange(it) }
    Title("Jemné pulzy")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Jemné pulzy, keď k tebe letí signál", modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = { on ->
                val needsAsk =
                    on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                if (needsAsk) permission.launch(Manifest.permission.POST_NOTIFICATIONS) else onChange(on)
            },
        )
    }
}

@Composable
private fun TutorialCard(
    title: String,
    text: String,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text)
        }
    }
}

@Composable
private fun Title(text: String) =
    Text(text, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

@Composable
private fun Body(text: String) = Text(text, textAlign = TextAlign.Center)
