@file:OptIn(ExperimentalMaterial3Api::class)

package app.quantumsocial.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.core.ui.theme.GalaxyGradient
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private enum class Emotion { Joy, Calm, Energy, Love, Mystery }

/** openFilters – ak true, hneď otvorí sheet (použité zo spodného baru). */
@Composable
fun StarMapScreen(openFilters: Boolean = false) {
    val signals by InMemorySignalRepository.signals.collectAsState()
    val baseSignals = if (signals.isEmpty()) remember { demoFakeSignals(80) } else signals
    val stars = remember(baseSignals) { baseSignals.map { it.toStarPoint() } }

    val langs = listOf("SK", "EN", "CZ")
    val topics = listOf("Tech", "Life", "Art")
    val emotions = Emotion.values().toList()

    val selLangs = remember { mutableStateListOf<String>() }
    val selEmotions = remember { mutableStateListOf<Emotion>() }
    val selTopics = remember { mutableStateListOf<String>() }
    var minIntensity by remember { mutableFloatStateOf(0f) }
    var maxDistance by remember { mutableFloatStateOf(1f) }
    var wishNetOnly by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }

    // pan/zoom – animovateľný
    val animScale = remember { Animatable(1f) }
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }
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

    val filtered =
        remember(stars, selLangs, selEmotions, selTopics, minIntensity, maxDistance, wishNetOnly) {
            stars.filter { s ->
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

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(GalaxyGradient),
    ) {
        // MAPA + gestá
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(filtered, canvasSize, reduceMotion) {
                        detectTapGestures(
                            onDoubleTap = { tap ->
                                val width = canvasSize.width.toFloat()
                                val height = canvasSize.height.toFloat()
                                val centerX = width / 2f
                                val centerY = height / 2f
                                val worldX =
                                    (tap.x - centerX - animOffsetX.value) / animScale.value
                                val worldY =
                                    (tap.y - centerY - animOffsetY.value) / animScale.value
                                val targetScale = if (animScale.value < 1.8f) 2.2f else 1f
                                val targetOffsetX = -targetScale * worldX
                                val targetOffsetY = -targetScale * worldY
                                scope.launch {
                                    animScale.animateTo(
                                        targetValue = targetScale,
                                        animationSpec = tween(240, easing = LinearEasing),
                                    )
                                }
                                scope.launch {
                                    animOffsetX.animateTo(
                                        targetValue = targetOffsetX,
                                        animationSpec = tween(240, easing = LinearEasing),
                                    )
                                }
                                scope.launch {
                                    animOffsetY.animateTo(
                                        targetValue = targetOffsetY,
                                        animationSpec = tween(240, easing = LinearEasing),
                                    )
                                }
                            },
                            onTap = { tap ->
                                val width = canvasSize.width.toFloat()
                                val height = canvasSize.height.toFloat()
                                val centerX = width / 2f
                                val centerY = height / 2f
                                val baseRadius = min(width, height) / 2f * 0.9f

                                var best: Pair<StarPoint, Float>? = null
                                for (s in filtered) {
                                    val r = baseRadius * s.distance
                                    val xw = r * cos(s.angleRad.toDouble()).toFloat()
                                    val yw = r * sin(s.angleRad.toDouble()).toFloat()
                                    val sx = centerX + animOffsetX.value + animScale.value * xw
                                    val sy = centerY + animOffsetY.value + animScale.value * yw
                                    val dx = tap.x - sx
                                    val dy = tap.y - sy
                                    val d2 = dx * dx + dy * dy
                                    if (best == null || d2 < best!!.second) best = s to d2
                                }

                                val thresholdPx = with(density) { 24.dp.toPx() }
                                val isHit = best != null && best!!.second <= thresholdPx * thresholdPx
                                if (isHit) {
                                    val s = best!!.first
                                    val r = baseRadius * s.distance
                                    val xw = r * cos(s.angleRad.toDouble()).toFloat()
                                    val yw = r * sin(s.angleRad.toDouble()).toFloat()
                                    val targetOffsetX = -animScale.value * xw
                                    val targetOffsetY = -animScale.value * yw
                                    scope.launch {
                                        animOffsetX.animateTo(
                                            targetValue = targetOffsetX,
                                            animationSpec = tween(260, easing = LinearEasing),
                                        )
                                    }
                                    scope.launch {
                                        animOffsetY.animateTo(
                                            targetValue = targetOffsetY,
                                            animationSpec = tween(260, easing = LinearEasing),
                                        )
                                    }
                                }
                            },
                        )
                    },
        ) {
            NebulaBackground(
                modifier = Modifier.fillMaxSize(),
                offsetX = animOffsetX.value,
                offsetY = animOffsetY.value,
                scale = animScale.value,
            )
            StarCanvas(
                stars = filtered,
                transformState = transformState,
                offsetX = animOffsetX.value,
                offsetY = animOffsetY.value,
                scale = animScale.value,
                reduceMotion = reduceMotion,
            )
        }

        // ───── MINI SLIDERY (vľavo dole) ─────
        MiniKnobs(
            minIntensity = minIntensity,
            onIntensityChange = { minIntensity = it },
            maxDistance = maxDistance,
            onDistanceChange = { maxDistance = it },
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
        )

        // ───── LEGENDA EMÓCIÍ (vpravo hore) ─────
        LegendEmotions(
            selected = selEmotions,
            onToggle = { e, selected ->
                if (selected) selEmotions.add(e) else selEmotions.remove(e)
            },
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
        )
    }

    // „Veľké“ filtre – v sheete (otvárané zo spodného baru cez openFilters)
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

// ------------------------------ SHEET UI --------------------------------

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
            text = "Filtre",
            fontWeight = FontWeight.SemiBold,
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally),
        )

        // Jazyk – presunuté do sheetu
        ChipsRow(
            label = "Jazyk",
            items = langs,
            selected = selLangs,
            onToggle = { item, selected ->
                if (selected) selLangs.add(item) else selLangs.remove(item)
            },
        )

        ChipsRow(
            label = "Emócia",
            items = emotions.map { it.name },
            selected = selEmotions.map { it.name },
            onToggle = { name, selected ->
                val e = Emotion.valueOf(name)
                if (selected) selEmotions.add(e) else selEmotions.remove(e)
            },
        )

        ChipsRow(
            label = "Téma",
            items = topics,
            selected = selTopics,
            onToggle = { item, selected ->
                if (selected) selTopics.add(item) else selTopics.remove(item)
            },
        )

        ChipsRow(
            label = "WishNet",
            items = listOf("Len WishNet"),
            selected = if (wishNetOnly) listOf("Len WishNet") else emptyList(),
            onToggle = { _, selected -> onWishToggle(selected) },
        )

        ChipsRow(
            label = "Animácie",
            items = listOf("Menej animácií"),
            selected = if (reduceMotion) listOf("Menej animácií") else emptyList(),
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
) {
    Text(text = label, modifier = Modifier.padding(start = 12.dp, top = 8.dp))
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp)) {
        items(items) { item ->
            val isSel = selected.contains(item)
            FilterChip(
                selected = isSel,
                onClick = { onToggle(item, !isSel) },
                label = { Text(item) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White.copy(alpha = 0.18f),
                    ),
            )
        }
    }
}

// --------------------------- MINI OVERLAY UI ----------------------------

@Composable
private fun MiniKnobs(
    minIntensity: Float,
    onIntensityChange: (Float) -> Unit,
    maxDistance: Float,
    onDistanceChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x66000000))
                .padding(8.dp),
    ) {
        Column {
            Text(text = "Min. intenzita: ${(minIntensity * 100).toInt()}%")
            Slider(
                value = minIntensity,
                onValueChange = onIntensityChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(text = "Max. vzdialenosť: ${(maxDistance * 100).toInt()}%")
            Slider(
                value = maxDistance,
                onValueChange = onDistanceChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// --------------------------- OVERLAY LEGENDA ----------------------------

@Composable
private fun LegendEmotions(
    selected: List<Emotion>,
    onToggle: (Emotion, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x66000000))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(Emotion.values()) { e ->
            val isSel = selected.contains(e)
            FilterChip(
                selected = isSel,
                onClick = { onToggle(e, !isSel) },
                label = { Text(e.label()) },
                leadingIcon = {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .background(e.color(), CircleShape),
                    )
                },
                modifier = Modifier.padding(horizontal = 2.dp),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = e.color().copy(alpha = 0.25f),
                    ),
            )
        }
    }
}

// --------------------------- MAPA & POZADIE -----------------------------

@Composable
private fun NebulaBackground(
    modifier: Modifier = Modifier,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    seed: Int = 1337,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val r = maxOf(w, h)

        val sky =
            Brush.radialGradient(
                colors = listOf(Color(0xFF1A1457), Color(0xFF0B0B2B)),
                center = Offset(w * 0.60f, h * 0.40f),
                radius = r,
            )
        drawRect(brush = sky, size = size)

        fun haze(
            cx: Float,
            cy: Float,
            radius: Float,
            color: Color,
            alpha: Float,
            parallax: Float,
        ) {
            val px = cx - offsetX * parallax
            val py = cy - offsetY * parallax
            drawCircle(color = color.copy(alpha = alpha), radius = radius, center = Offset(px, py))
        }

        haze(w * 0.22f, h * 0.30f, r * 0.40f, Color(0xFF7B1FA2), 0.25f, 0.02f)
        haze(w * 0.78f, h * 0.22f, r * 0.30f, Color(0xFF3949AB), 0.20f, 0.03f)
        haze(w * 0.58f, h * 0.66f, r * 0.42f, Color(0xFF512DA8), 0.22f, 0.035f)
        haze(w * 0.40f, h * 0.78f, r * 0.28f, Color(0xFFAD1457), 0.18f, 0.045f)
        haze(w * 0.66f, h * 0.46f, r * 0.22f, Color(0xFF00ACC1), 0.12f, 0.05f)

        val rnd = Random(seed)
        withTransform({ translate(left = -offsetX * 0.015f, top = -offsetY * 0.015f) }) {
            repeat(350) {
                val x = rnd.nextFloat() * w
                val y = rnd.nextFloat() * h
                val rad = 0.5f + rnd.nextFloat() * 1.5f
                val a = 0.35f + rnd.nextFloat() * 0.45f
                drawCircle(color = Color.White.copy(alpha = a), radius = rad, center = Offset(x, y))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StarCanvas(
    stars: List<StarPoint>,
    transformState: TransformableState,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    reduceMotion: Boolean,
) {
    val tick =
        if (reduceMotion) {
            0f
        } else {
            rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = (2f * PI).toFloat(),
                animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
            ).value
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .transformable(transformState),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeColor = Color.White.copy(alpha = 0.10f)
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val baseRadius = min(size.width, size.height) / 2f * 0.9f

            withTransform({
                translate(left = centerX + offsetX, top = centerY + offsetY)
                scale(scaleX = scale, scaleY = scale)
            }) {
                drawCircle(color = strokeColor, radius = baseRadius)

                stars.forEach { s ->
                    val color = s.emotionColor()
                    val r = baseRadius * s.distance
                    val x = r * cos(s.angleRad.toDouble()).toFloat()
                    val y = r * sin(s.angleRad.toDouble()).toFloat()

                    val base = if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity
                    val phase = (s.id.hashCode() % 628) / 100f
                    val twinkle = (kotlin.math.sin(tick + phase) + 1f) / 2f
                    val haloRadius = base * if (s.isWishNet) 3f else 2f
                    val haloAlpha =
                        if (reduceMotion) 0.10f * s.intensity else 0.08f + 0.20f * twinkle * s.intensity

                    drawCircle(
                        color = color.copy(alpha = haloAlpha),
                        radius = haloRadius,
                        center = Offset(x, y),
                        blendMode = BlendMode.Plus,
                    )
                    drawCircle(color = color.copy(alpha = 0.95f), radius = base, center = Offset(x, y))
                    drawCircle(
                        color = Color.White.copy(alpha = 0.90f),
                        radius = base * 0.55f,
                        center = Offset(x, y),
                    )
                }
            }
        }
    }
}

// -------------------------- DATA & HELPERS ------------------------------

private data class StarPoint(
    val id: String,
    val lang: String,
    val emotion: Emotion,
    val topic: String,
    val intensity: Float,
    val distance: Float,
    val isWishNet: Boolean,
    val angleRad: Float,
)

private fun Signal.toStarPoint(): StarPoint {
    val seed = id.hashCode()
    val rnd = Random(seed)
    val langs = listOf("SK", "EN", "CZ")
    val topics = listOf("Tech", "Life", "Art")
    val emotions = Emotion.values()

    val lang = langs[absMod(seed, langs.size)]
    val topic = topics[absMod(seed / 3, topics.size)]
    val emotion = emotions[absMod(seed / 5, emotions.size)]
    val intensity = 0.2f + rnd.nextFloat() * 0.8f
    val dist = 0.1f + rnd.nextFloat() * 0.9f
    val angle = (rnd.nextFloat() * 2f * PI).toFloat()
    val wish =
        when (this) {
            is MixSignal -> true
            else -> this.category == SignalCategory.WishNet
        }

    return StarPoint(
        id = id,
        lang = lang,
        emotion = emotion,
        topic = topic,
        intensity = intensity.coerceIn(0f, 1f),
        distance = dist.coerceIn(0f, 1f),
        isWishNet = wish,
        angleRad = angle,
    )
}

private fun Emotion.label(): String =
    when (this) {
        Emotion.Joy -> "Joy"
        Emotion.Calm -> "Calm"
        Emotion.Energy -> "Energy"
        Emotion.Love -> "Love"
        Emotion.Mystery -> "Mystery"
    }

private fun Emotion.color(): Color =
    when (this) {
        Emotion.Joy -> Color(0xFFFFD54F)
        Emotion.Calm -> Color(0xFF64B5F6)
        Emotion.Energy -> Color(0xFFEF5350)
        Emotion.Love -> Color(0xFFBA68C8)
        Emotion.Mystery -> Color(0xFFA1887F)
    }

private fun StarPoint.emotionColor(): Color = emotion.color()

private fun absMod(
    x: Int,
    m: Int,
): Int {
    val r = x % m
    return if (r < 0) r + m else r
}

private fun demoFakeSignals(count: Int): List<Signal> {
    val out = mutableListOf<Signal>()
    repeat(count) { i ->
        out +=
            when (i % 4) {
                0 -> TextSignal(text = "Hello #$i")
                1 -> EmojiSignal(emoji = listOf("✨", "⭐", "🚀", "💙").random())
                2 -> ImageSignal(imageUri = "content://demo/$i")
                else -> MixSignal(parts = emptyList())
            }
    }
    return out
}
