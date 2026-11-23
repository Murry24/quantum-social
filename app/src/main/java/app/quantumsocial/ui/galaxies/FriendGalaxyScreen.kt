@file:OptIn(ExperimentalMaterial3Api::class)

package app.quantumsocial.ui.galaxies

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.BlackHoleInvite
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.canvas.FireflyLayer
import app.quantumsocial.ui.canvas.NebulaBackground
import app.quantumsocial.ui.canvas.PulseFlight
import app.quantumsocial.ui.canvas.PulseFlightLayer
import app.quantumsocial.ui.canvas.StarCanvas
import app.quantumsocial.ui.model.StarPoint
import app.quantumsocial.ui.model.starEmotionForId
import app.quantumsocial.ui.model.starLangForId
import app.quantumsocial.ui.model.starTopicForId
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import app.quantumsocial.ui.perf.LocalPerformanceMode
import app.quantumsocial.ui.screens.color
import app.quantumsocial.ui.signals.SendPulseSheet
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Malá galaxia priateľov (bod 31): len jej členovia, farba podľa jej emócie.
 * Ťuk na hviezdu = pulz, biela diera = odchod, červie diery = skok do mojich ďalších galaxií.
 */
@Composable
fun FriendGalaxyScreen(
    galaxyId: String,
    onLeave: () -> Unit,
    onJump: (String) -> Unit,
) {
    val context = LocalContext.current
    val me = remember { OnboardingPrefs.myStarId(context) }
    val store = GalaxyStore.shared
    val all by store.galaxies.collectAsState()
    val favorites by store.favorites.collectAsState()
    val galaxy = all.firstOrNull { it.id == galaxyId }
    val perf = LocalPerformanceMode.current
    val scope = rememberCoroutineScope()

    // prílet: obrazovka sa „rozbalí“ zo stredu
    val warp = remember { Animatable(1f) }
    LaunchedEffect(Unit) { warp.animateTo(0f, tween(warpMillis(perf))) }

    // odišiel som (biela diera) alebo galaxia zhasla → späť do veľkej galaxie
    if (galaxy == null || me !in galaxy.members) {
        LaunchedEffect(Unit) { onLeave() }
        return
    }

    val now = System.currentTimeMillis()
    val isCore = galaxy.coreStarId == me
    val light = galaxy.brightness(now).coerceAtLeast(0.25f)
    val stars = remember(galaxy.members, light) { galaxy.members.map { memberPoint(it).copy(brightness = light) } }
    val wormholes = remember(all) { store.wormholesFrom(galaxyId, me, now) }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val hitPx = with(LocalDensity.current) { 36.dp.toPx() }
    val flights = remember { mutableStateListOf<PulseFlight>() }
    var sendTo by remember { mutableStateOf<String?>(null) }
    var expelId by remember { mutableStateOf<String?>(null) }
    var confirmLeave by remember { mutableStateOf(false) }
    var invite by remember { mutableStateOf<BlackHoleInvite?>(null) }

    fun flyAway(action: () -> Unit) {
        scope.launch {
            warp.animateTo(1f, tween(warpMillis(perf)))
            action()
        }
    }

    Box(modifier = Modifier.fillMaxSize().warp(warp.value, perf)) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(canvasSize, stars, isCore) {
                        detectTapGestures(
                            onTap = {
                                    tap ->
                                hit(tap, canvasSize, stars, hitPx)?.takeIf { it != me }?.let { sendTo = it }
                            },
                            onLongPress = { tap ->
                                if (isCore) {
                                    hit(
                                        tap,
                                        canvasSize,
                                        stars,
                                        hitPx,
                                    )?.takeIf { it != me }?.let { expelId = it }
                                }
                            },
                        )
                    },
        ) {
            NebulaBackground(offsetX = 0f, offsetY = 0f, scale = 1f)
            // nálada galaxie zafarbí oblohu
            Box(Modifier.fillMaxSize().background(galaxy.emotion.color().copy(alpha = 0.18f)))
            FireflyLayer()
            StarCanvas(
                stars = stars,
                offsetX = 0f,
                offsetY = 0f,
                scale = 1f,
                reduceMotion = !perf.twinkle,
                // jemný pulzujúci kruh = to si ty
                focusStarId = me,
                focusVisible = true,
            )
            PulseFlightLayer(
                flights = flights,
                stars = stars,
                offsetX = 0f,
                offsetY = 0f,
                scale = 1f,
                onFinished = { id -> flights.removeAll { it.id == id } },
            )
        }

        CompositionLocalProvider(LocalContentColor provides Color(GalaxyPalette.ON_SKY)) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(galaxy.name, style = MaterialTheme.typography.titleLarge)
                Text("${galaxy.members.size} hviezd" + if (isCore) tr(" · si jadro") else "")
                GalaxyHeaderExtras(galaxy, me, isFavorite = galaxy.id in favorites)
                if (isCore) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { invite = store.createInvite(galaxyId, me) },
                            enabled = galaxy.open,
                        ) { Text(tr("Vytvoriť čiernu dieru")) }
                        Switch(checked = galaxy.open, onCheckedChange = { store.setOpen(galaxyId, me, it) })
                    }
                    Text(if (galaxy.open) tr("Čierna diera otvorená") else tr("Čierna diera zatvorená"))
                }
            }
        }

        HoleButton(
            kind = HoleKind.White,
            label = tr("Odletieť"),
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
        ) { confirmLeave = true }

        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
            wormholes.forEach { g ->
                HoleButton(kind = HoleKind.Worm, label = g.name, size = 40.dp) { flyAway { onJump(g.id) } }
            }
        }
    }

    sendTo?.let { target ->
        ModalBottomSheet(
            onDismissRequest = { sendTo = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            SendPulseSheet(
                target = SignalTarget.Star(target),
                onSend = { emotion, emoji, effect, urgent ->
                    PulseOutbox.send(
                        target = SignalTarget.Star(target),
                        emotion = emotion,
                        visibleStarIds = galaxy.members.toList(),
                        emoji = emoji,
                        effect = effect,
                        urgent = urgent,
                    )?.let { sent ->
                        flights.add(PulseFlight.of(sent, emotion.color()))
                    }
                    // signál galaxiu znova rozsvieti
                    store.recordSignal(galaxyId)
                    sendTo = null
                },
            )
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text(tr("Odletieť späť do veľkej galaxie?")) },
            text = { Text(tr("Biela diera ťa potichu vráti – nikto nedostane upozornenie.")) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLeave = false
                    flyAway { store.leave(galaxyId, me) }
                }) { Text(tr("Odletieť")) }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text(tr("Ostať")) } },
        )
    }

    invite?.let { inv ->
        AlertDialog(
            onDismissRequest = { invite = null },
            title = { Text(tr("Čierna diera: %s", inv.code)) },
            text = { Text(tr("Platí 24 hodín alebo pre %s hviezd.", inv.maxUses)) },
            confirmButton = {
                TextButton(onClick = {
                    val send =
                        Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, tr("Vleť do mojej galaxie v Quantum Social: %s", inv.code))
                    context.startActivity(Intent.createChooser(send, null))
                }) { Text(tr("Zdieľať")) }
            },
            dismissButton = { TextButton(onClick = { invite = null }) { Text(tr("Zavrieť")) } },
        )
    }

    expelId?.let { id ->
        AlertDialog(
            onDismissRequest = { expelId = null },
            title = { Text(tr("Vypľuť z galaxie?")) },
            confirmButton = {
                TextButton(onClick = {
                    store.expel(galaxyId, me, id)
                    expelId = null
                }) { Text(tr("Vypľuť")) }
            },
            dismissButton = { TextButton(onClick = { expelId = null }) { Text(tr("Nechať")) } },
        )
    }
}

// poloha a vzhľad hviezdy len z jej id – v každej galaxii rovnaké
private fun memberPoint(id: String): StarPoint {
    val rnd = Random(id.hashCode())
    return StarPoint(
        id = id,
        lang = starLangForId(id),
        emotion = starEmotionForId(id),
        topic = starTopicForId(id),
        intensity = 0.6f + rnd.nextFloat() * 0.4f,
        distance = 0.25f + rnd.nextFloat() * 0.6f,
        isWishNet = false,
        angleRad = (rnd.nextFloat() * 2f * PI).toFloat(),
    )
}

// najbližšia hviezda pod prstom (galaxia je bez zoomu, preto stačí jednoduchý výpočet ako v StarCanvas)
private fun hit(
    tap: Offset,
    canvas: IntSize,
    stars: List<StarPoint>,
    maxPx: Float,
): String? {
    val base = min(canvas.width, canvas.height) / 2f * 0.9f
    val cx = canvas.width / 2f
    val cy = canvas.height / 2f
    return stars
        .map { s ->
            val r = base * s.distance
            val dx = cx + r * cos(s.angleRad) - tap.x
            val dy = cy + r * sin(s.angleRad) - tap.y
            s.id to dx * dx + dy * dy
        }.filter { it.second <= maxPx * maxPx }
        .minByOrNull { it.second }
        ?.first
}
