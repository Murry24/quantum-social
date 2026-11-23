@file:OptIn(ExperimentalMaterial3Api::class)

package app.quantumsocial.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.ModerationStore
import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.data.WatchedStars
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalLifecycle
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.core.model.WorldLanguages
import app.quantumsocial.ui.canvas.FireflyLayer
import app.quantumsocial.ui.canvas.NebulaBackground
import app.quantumsocial.ui.canvas.PulseFlight
import app.quantumsocial.ui.canvas.PulseFlightLayer
import app.quantumsocial.ui.canvas.StarCanvas
import app.quantumsocial.ui.components.LegendEmotions
import app.quantumsocial.ui.components.MiniKnobs
import app.quantumsocial.ui.components.TopDrawer
import app.quantumsocial.ui.galaxies.HoleButton
import app.quantumsocial.ui.galaxies.HoleKind
import app.quantumsocial.ui.galaxies.MyGalaxiesSheet
import app.quantumsocial.ui.galaxies.warp
import app.quantumsocial.ui.galaxies.warpMillis
import app.quantumsocial.ui.model.StarPoint
import app.quantumsocial.ui.model.toStarPoint
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import app.quantumsocial.ui.perf.LocalGalaxyTheme
import app.quantumsocial.ui.perf.LocalPerformanceMode
import app.quantumsocial.ui.signals.SendPulseSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun StarMapScreen(
    openFilters: Boolean = false,
    onOpenStar: (String) -> Unit = {},
    focusStarId: String? = null,
    onOpenGalaxy: (String) -> Unit = {},
) {
    val signals by InMemorySignalRepository.signals.collectAsState()
    val fadingSince by InMemorySignalRepository.fadingSince.collectAsState()
    val watchedIds by WatchedStars.watched.collectAsState()
    val isDemo = signals.isEmpty()
    val baseSignals = if (isDemo) remember { demoFakeSignals(80) } else signals

    // Bod 3: skutočné signály blednú a zhasínajú podľa životnosti (demo hviezdy svietia stále)
    val now by produceState(initialValue = System.currentTimeMillis(), isDemo) {
        while (!isDemo) {
            value = System.currentTimeMillis()
            delay(100)
        }
    }
    val stars =
        remember(baseSignals, fadingSince, now, isDemo) {
            baseSignals.map { sig ->
                val p = sig.toStarPoint()
                if (isDemo) p else p.copy(brightness = SignalLifecycle.brightness(sig, now, fadingSince[sig.id]))
            }
        }

    val langs = WorldLanguages.codes
    val topics = listOf("Tech", "Life", "Art")
    val emotions = Emotion.values().toList()

    val selLangs = remember { mutableStateListOf<String>() }
    val selEmotions = remember { mutableStateListOf<Emotion>() }
    val selTopics = remember { mutableStateListOf<String>() }
    var minIntensity by remember { mutableFloatStateOf(0f) }
    var maxDistance by remember { mutableFloatStateOf(1f) }
    var wishNetOnly by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }

    // bod 6: v šetriči výkonu sa hviezdy netrblietajú (menej prekresľovania)
    val perf = LocalPerformanceMode.current
    val calmMotion = reduceMotion || !perf.twinkle

    // Viewport (perzistentný)
    var savedScale by rememberSaveable { mutableFloatStateOf(1f) }
    var savedOffsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var savedOffsetY by rememberSaveable { mutableFloatStateOf(0f) }

    val animScale = remember { Animatable(savedScale) }
    val animOffsetX = remember { Animatable(savedOffsetX) }
    val animOffsetY = remember { Animatable(savedOffsetY) }

    LaunchedEffect(animScale.value) { savedScale = animScale.value }
    LaunchedEffect(animOffsetX.value) { savedOffsetX = animOffsetX.value }
    LaunchedEffect(animOffsetY.value) { savedOffsetY = animOffsetY.value }

    val transformState: TransformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            val nextScale = (animScale.value * zoomChange).coerceIn(0.5f, 5f)
            val nextX = animOffsetX.value + panChange.x
            val nextY = animOffsetY.value + panChange.y
            kotlinx.coroutines.runBlocking {
                animScale.snapTo(nextScale)
                animOffsetX.snapTo(nextX)
                animOffsetY.snapTo(nextY)
            }
        }

    val moderation by ModerationStore.state.collectAsState()
    val filtered =
        remember(stars, selLangs, selEmotions, selTopics, minIntensity, maxDistance, wishNetOnly, moderation) {
            moderation.visible(stars) { it.id }.filter { s ->
                (selLangs.isEmpty() || selLangs.contains(s.lang)) &&
                    (selEmotions.isEmpty() || selEmotions.contains(s.emotion)) &&
                    (selTopics.isEmpty() || selTopics.contains(s.topic)) &&
                    s.intensity >= minIntensity &&
                    s.distance <= maxDistance &&
                    (!wishNetOnly || s.isWishNet)
            }
        }

    var showFilters by remember { mutableStateOf(openFilters) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val scope = rememberCoroutineScope()
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    var focusVisible by remember { mutableStateOf(false) }

    // Bod 8: posielanie pocitových pulzov
    var sendTarget by remember { mutableStateOf<SignalTarget?>(null) }
    val sendSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val flights = remember { mutableStateListOf<PulseFlight>() }

    // Bod 31: čierna diera → Moje galaxie; prelet = obrazovka sa zatočí a stiahne do stredu
    val context = LocalContext.current
    val myStarId = remember { OnboardingPrefs.myStarId(context) }
    var showGalaxies by remember { mutableStateOf(false) }
    val warpOut = remember { Animatable(0f) }

    fun flyToGalaxy(id: String) {
        showGalaxies = false
        scope.launch {
            warpOut.animateTo(1f, tween(warpMillis(perf)))
            onOpenGalaxy(id)
        }
    }

    // ✦ v spodnej lište: Do chaosu / Moje galaxie
    val request by MapRequests.pending.collectAsState()
    LaunchedEffect(request) {
        when (request) {
            MapRequest.Chaos -> sendTarget = SignalTarget.Chaos
            MapRequest.Galaxies -> showGalaxies = true
            null -> return@LaunchedEffect
        }
        MapRequests.consume()
    }

    // bod 6: horná lišta je skrytá, kým ju netreba
    var topOpen by remember { mutableStateOf(false) }
    val filtersActive = selEmotions.isNotEmpty() || minIntensity > 0f || maxDistance < 1f
    val zoomed = abs(animScale.value - 1f) > 0.01f || abs(animOffsetX.value) > 1f || abs(animOffsetY.value) > 1f

    // aktuálny zoznam pre gestá – pointerInput sa tak nereštartuje pri každej zmene jasu hviezd
    val currentFiltered by rememberUpdatedState(filtered)

    // Soft-focus pri návrate z profilu
    LaunchedEffect(focusStarId, canvasSize, stars) {
        val id = focusStarId ?: return@LaunchedEffect
        if (canvasSize.width == 0 || canvasSize.height == 0) return@LaunchedEffect
        val s = stars.firstOrNull { it.id == id } ?: return@LaunchedEffect

        val baseRadius = min(canvasSize.width.toFloat(), canvasSize.height.toFloat()) / 2f * 0.9f
        val r = baseRadius * s.distance
        val xw = r * cos(s.angleRad.toDouble()).toFloat()
        val yw = r * sin(s.angleRad.toDouble()).toFloat()

        val targetScale = 1.8f
        launch { animScale.animateTo(targetScale, tween(320, easing = LinearEasing)) }
        launch { animOffsetX.animateTo(-targetScale * xw, tween(320, easing = LinearEasing)) }
        launch { animOffsetY.animateTo(-targetScale * yw, tween(320, easing = LinearEasing)) }

        focusVisible = true
        delay(if (calmMotion) 1500 else 3000)
        focusVisible = false
    }

    // --- Screen-space hit test (podľa kreslenia) ---
    fun nearestHitStarScreen(
        tap: Offset,
        canvas: IntSize,
        scale: Float,
        offX: Float,
        offY: Float,
        density: Density,
        stars: List<StarPoint>,
    ): StarPoint? {
        if (canvas.width == 0 || canvas.height == 0) return null
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val baseRadius = min(w, h) / 2f * 0.9f

        // väčší dotykový polomer – drobné hviezdy sa ľahšie trafia
        val minPx = with(density) { 40.dp.toPx() }
        val maxPx = with(density) { 120.dp.toPx() }

        var best: StarPoint? = null
        var bestScore = Float.MAX_VALUE

        for (s in stars) {
            if (s.brightness < 0.1f) continue
            val r = baseRadius * s.distance
            val xw = r * cos(s.angleRad.toDouble()).toFloat()
            val yw = r * sin(s.angleRad.toDouble()).toFloat()

            val sx = cx + offX + scale * xw
            val sy = cy + offY + scale * yw

            val core = if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity
            val corePx = core * scale
            val hitR = (corePx * 3.0f).coerceIn(minPx, maxPx)

            val dx = tap.x - sx
            val dy = tap.y - sy
            val d2 = dx * dx + dy * dy
            if (d2 <= hitR * hitR) {
                val score = d2 / (hitR * hitR)
                if (score < bestScore) {
                    bestScore = score
                    best = s
                }
            }
        }
        return best
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .warp(warpOut.value, perf)
                .background(LocalGalaxyTheme.current.skyBrush),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    // 1) TAPY PRED transformable – stable keys (žiadne animScale/offset v kľúčoch)
                    .pointerInput(canvasSize) {
                        detectTapGestures(
                            onTap = { tap ->
                                val scaleNow = animScale.value
                                val offXNow = animOffsetX.value
                                val offYNow = animOffsetY.value
                                nearestHitStarScreen(
                                    tap = tap,
                                    canvas = canvasSize,
                                    scale = scaleNow,
                                    offX = offXNow,
                                    offY = offYNow,
                                    density = density,
                                    stars = currentFiltered,
                                )?.let { sendTarget = SignalTarget.Star(it.id) }
                            },
                            onLongPress = { tap ->
                                val scaleNow = animScale.value
                                val offXNow = animOffsetX.value
                                val offYNow = animOffsetY.value
                                nearestHitStarScreen(
                                    tap = tap,
                                    canvas = canvasSize,
                                    scale = scaleNow,
                                    offX = offXNow,
                                    offY = offYNow,
                                    density = density,
                                    stars = currentFiltered,
                                )?.let { onOpenStar(it.id) }
                            },
                        )
                    }
                    // 2) PINCH/PAN AŽ POTOM
                    .transformable(transformState),
        ) {
            NebulaBackground(
                offsetX = animOffsetX.value,
                offsetY = animOffsetY.value,
                scale = animScale.value,
            )
            FireflyLayer()
            StarCanvas(
                stars = filtered,
                offsetX = animOffsetX.value,
                offsetY = animOffsetY.value,
                scale = animScale.value,
                reduceMotion = calmMotion,
                focusStarId = focusStarId,
                focusVisible = focusVisible,
                watchedIds = watchedIds,
            )
            PulseFlightLayer(
                flights = flights,
                stars = stars,
                offsetX = animOffsetX.value,
                offsetY = animOffsetY.value,
                scale = animScale.value,
                onFinished = { id -> flights.removeAll { it.id == id } },
            )
        }

        // Bod 31: čierna diera na okraji veľkej galaxie (ľavý horný roh) – nič ju neprekrýva
        HoleButton(
            kind = HoleKind.Black,
            label = tr("Moje galaxie"),
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        ) { showGalaxies = true }

        // zoom je dvoma prstami; „1×“ sa ukáže, len keď je mapa priblížená alebo posunutá
        if (zoomed) {
            AssistChip(
                onClick = {
                    scope.launch {
                        launch { animScale.animateTo(1f, tween(200, easing = LinearEasing)) }
                        launch { animOffsetX.animateTo(0f, tween(200, easing = LinearEasing)) }
                        launch { animOffsetY.animateTo(0f, tween(200, easing = LinearEasing)) }
                    }
                },
                label = { Text("1×") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            )
        }

        // filtre emócií a posuvníky – skrytá horná lišta
        TopDrawer(
            open = topOpen,
            active = filtersActive,
            onOpenChange = { topOpen = it },
        ) {
            LegendEmotions(
                selected = selEmotions,
                onToggle = { e, selected -> if (selected) selEmotions.add(e) else selEmotions.remove(e) },
            )
            MiniKnobs(
                minIntensity = minIntensity,
                onIntensityChange = { minIntensity = it },
                maxDistance = maxDistance,
                onDistanceChange = { maxDistance = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }

    // Bod 8: panel na odoslanie pulzu
    sendTarget?.let { target ->
        ModalBottomSheet(
            onDismissRequest = { sendTarget = null },
            sheetState = sendSheetState,
        ) {
            SendPulseSheet(
                target = target,
                onSend = { emotion, emoji, effect, urgent ->
                    PulseOutbox.send(
                        target = target,
                        emotion = emotion,
                        visibleStarIds = currentFiltered.filter { it.brightness > 0.1f }.map { it.id },
                        emoji = emoji,
                        effect = effect,
                        urgent = urgent,
                    )?.let { sent ->
                        flights.add(PulseFlight.of(sent, emotion.color()))
                    }
                    sendTarget = null
                },
                onOpenProfile = { id ->
                    sendTarget = null
                    onOpenStar(id)
                },
            )
        }
    }

    // Bod 31: Moje galaxie
    if (showGalaxies) {
        ModalBottomSheet(
            onDismissRequest = { showGalaxies = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            MyGalaxiesSheet(myStarId = myStarId, onOpen = ::flyToGalaxy)
        }
    }

    // Veľké filtre – sheet
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
        ) {
            FilterSheetContent(
                langs = langs,
                emotions = emotions,
                topics = topics,
                selLangs = selLangs,
                selEmotions = selEmotions,
                selTopics = selTopics,
                minIntensity = minIntensity,
                onIntensityChange = { minIntensity = it },
                maxDistance = maxDistance,
                onDistanceChange = { maxDistance = it },
                wishNetOnly = wishNetOnly,
                onWishToggle = { wishNetOnly = it },
                reduceMotion = reduceMotion,
                onReduceToggle = { reduceMotion = it },
            )
        }
    }
}

// --------------------------- Filter UI ---------------------------

@Composable
private fun FilterSheetContent(
    langs: List<String>,
    emotions: List<Emotion>,
    topics: List<String>,
    selLangs: MutableList<String>,
    selEmotions: MutableList<Emotion>,
    selTopics: MutableList<String>,
    minIntensity: Float,
    onIntensityChange: (Float) -> Unit,
    maxDistance: Float,
    onDistanceChange: (Float) -> Unit,
    wishNetOnly: Boolean,
    onWishToggle: (Boolean) -> Unit,
    reduceMotion: Boolean,
    onReduceToggle: (Boolean) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
    ) {
        Text(
            text = tr("Filtre"),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
        )

        ChipsRow(
            label = tr("Jazyk"),
            items = langs,
            selected = selLangs,
            onToggle = { item, selected -> if (selected) selLangs.add(item) else selLangs.remove(item) },
            labelOf = WorldLanguages::chip,
        )

        ChipsRow(
            label = tr("Emócia"),
            items = emotions.map { it.label() },
            selected = selEmotions.map { it.label() },
            onToggle = { name, selected ->
                val e = emotions.first { it.label() == name }
                if (selected) selEmotions.add(e) else selEmotions.remove(e)
            },
        )

        ChipsRow(
            label = tr("Téma"),
            items = topics,
            selected = selTopics,
            onToggle = { item, selected -> if (selected) selTopics.add(item) else selTopics.remove(item) },
        )

        ChipsRow(
            label = tr("WishNet"),
            items = listOf(tr("Len WishNet")),
            selected = if (wishNetOnly) listOf(tr("Len WishNet")) else emptyList(),
            onToggle = { _, selected -> onWishToggle(selected) },
        )

        ChipsRow(
            label = tr("Animácie"),
            items = listOf(tr("Menej animácií")),
            selected = if (reduceMotion) listOf(tr("Menej animácií")) else emptyList(),
            onToggle = { _, selected -> onReduceToggle(selected) },
        )
    }
}

@Composable
private fun ChipsRow(
    label: String,
    items: List<String>,
    selected: List<String>,
    onToggle: (String, Boolean) -> Unit,
    labelOf: (String) -> String = { it },
) {
    Text(text = label, modifier = Modifier.padding(start = 12.dp, top = 8.dp))
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(items) { item ->
            val isSel = selected.contains(item)
            FilterChip(
                selected = isSel,
                onClick = { onToggle(item, !isSel) },
                label = { Text(labelOf(item)) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f),
                    ),
            )
        }
    }
}

// --------------------------- Demo dáta ---------------------------

private fun demoFakeSignals(count: Int): List<Signal> {
    val out = mutableListOf<Signal>()
    repeat(count) { i ->
        out +=
            when (i % 4) {
                0 -> TextSignal(text = "Hello #$i")
                1 -> EmojiSignal(emoji = listOf(tr("✨"), tr("⭐"), tr("🚀"), tr("💙")).random())
                2 -> ImageSignal(imageUri = "content://demo/$i")
                else -> MixSignal(parts = emptyList())
            }
    }
    return out
}
