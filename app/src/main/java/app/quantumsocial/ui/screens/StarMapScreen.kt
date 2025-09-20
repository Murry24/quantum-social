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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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

@Composable
fun StarMapScreen() {
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

    // --- shared pan/zoom stav (animovateľný) ---
    val animScale = remember { Animatable(1f) }
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }

    val transformState: TransformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            // okamžitá reakcia na gesto (bez animácie)
            val nextScale = (animScale.value * zoomChange).coerceIn(0.5f, 5f)
            val nextX = animOffsetX.value + panChange.x
            val nextY = animOffsetY.value + panChange.y
            // snapTo je "bez animácie"
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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(GalaxyGradient),
    ) {
        Text(
            text = "Filtre",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

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

        LabeledSlider(
            label = "Min. intenzita",
            value = minIntensity,
            onValueChange = { minIntensity = it },
        )

        LabeledSlider(
            label = "Max. vzdialenosť",
            value = maxDistance,
            onValueChange = { maxDistance = it },
        )

        ChipsRow(
            label = "WishNet",
            items = listOf("Len WishNet"),
            selected = if (wishNetOnly) listOf("Len WishNet") else emptyList(),
            onToggle = { _, selected ->
                wishNetOnly = selected
            },
        )

        ChipsRow(
            label = "Animácie",
            items = listOf("Menej animácií"),
            selected = if (reduceMotion) listOf("Menej animácií") else emptyList(),
            onToggle = { _, selected ->
                reduceMotion = selected
            },
        )

        // --- Map + gestá ---
        val scope = rememberCoroutineScope()
        var canvasSize by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(filtered, canvasSize, reduceMotion) {
                        detectTapGestures(
                            onDoubleTap = { tap ->
                                // double-tap: zoom toggle a centrovanie na miesto dotyku
                                val width = canvasSize.width.toFloat()
                                val height = canvasSize.height.toFloat()
                                val centerX = width / 2f
                                val centerY = height / 2f

                                val worldX =
                                    (tap.x - centerX - animOffsetX.value) / animScale.value
                                val worldY =
                                    (tap.y - centerY - animOffsetY.value) / animScale.value

                                val targetScale =
                                    if (animScale.value < 1.8f) 2.2f else 1f
                                val targetOffsetX = -targetScale * worldX
                                val targetOffsetY = -targetScale * worldY

                                scope.launch {
                                    animScale.animateTo(
                                        targetValue = targetScale,
                                        animationSpec = tween(durationMillis = 240, easing = LinearEasing),
                                    )
                                }
                                scope.launch {
                                    animOffsetX.animateTo(
                                        targetValue = targetOffsetX,
                                        animationSpec = tween(durationMillis = 240, easing = LinearEasing),
                                    )
                                }
                                scope.launch {
                                    animOffsetY.animateTo(
                                        targetValue = targetOffsetY,
                                        animationSpec = tween(durationMillis = 240, easing = LinearEasing),
                                    )
                                }
                            },
                            onTap = { tap ->
                                // single tap: ak trafíme hviezdu (blízko), centrovať na ňu
                                val width = canvasSize.width.toFloat()
                                val height = canvasSize.height.toFloat()
                                val centerX = width / 2f
                                val centerY = height / 2f
                                val baseRadius = min(width, height) / 2f * 0.9f

                                // nájdi najbližšiu hviezdu (v screen súradniciach)
                                var best: Pair<StarPoint, Float>? = null
                                stars@ for (s in filtered) {
                                    val r = baseRadius * s.distance
                                    val xw = r * cos(s.angleRad)
                                    val yw = r * sin(s.angleRad)
                                    val sx = centerX + animOffsetX.value + animScale.value * xw
                                    val sy = centerY + animOffsetY.value + animScale.value * yw
                                    val dx = tap.x - sx
                                    val dy = tap.y - sy
                                    val d2 = dx * dx + dy * dy
                                    if (best == null || d2 < best!!.second) {
                                        best = s to d2
                                    }
                                }

                                // prah: ~24dp
                                val thresholdPx = with(density) { 24.dp.toPx() }
                                val isHit = best != null && best!!.second <= thresholdPx * thresholdPx
                                if (isHit) {
                                    val s = best!!.first
                                    val r = baseRadius * s.distance
                                    val xw = r * cos(s.angleRad)
                                    val yw = r * sin(s.angleRad)

                                    val targetOffsetX = -animScale.value * xw
                                    val targetOffsetY = -animScale.value * yw

                                    scope.launch {
                                        animOffsetX.animateTo(
                                            targetValue = targetOffsetX,
                                            animationSpec = tween(durationMillis = 260, easing = LinearEasing),
                                        )
                                    }
                                    scope.launch {
                                        animOffsetY.animateTo(
                                            targetValue = targetOffsetY,
                                            animationSpec = tween(durationMillis = 260, easing = LinearEasing),
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
    }
}

@Composable
private fun ChipsRow(
    label: String,
    items: List<String>,
    selected: List<String>,
    onToggle: (String, Boolean) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(items) { item ->
            val isSel = selected.contains(item)
            FilterChip(
                selected = isSel,
                onClick = { onToggle(item, !isSel) },
                label = { Text(item) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                    ),
            )
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Text(
        text = "$label: ${(value * 100).toInt()}%",
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        steps = 0,
        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
    )
}

/** Nebula pozadie s parallaxom. */
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
                colors =
                    listOf(
                        Color(0xFF1A1457),
                        Color(0xFF0B0B2B),
                    ),
                center =
                    Offset(
                        x = w * 0.60f,
                        y = h * 0.40f,
                    ),
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
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center =
                    Offset(
                        x = px,
                        y = py,
                    ),
            )
        }

        haze(
            cx = w * 0.22f,
            cy = h * 0.30f,
            radius = r * 0.40f,
            color = Color(0xFF7B1FA2),
            alpha = 0.25f,
            parallax = 0.02f,
        )
        haze(
            cx = w * 0.78f,
            cy = h * 0.22f,
            radius = r * 0.30f,
            color = Color(0xFF3949AB),
            alpha = 0.20f,
            parallax = 0.03f,
        )
        haze(
            cx = w * 0.58f,
            cy = h * 0.66f,
            radius = r * 0.42f,
            color = Color(0xFF512DA8),
            alpha = 0.22f,
            parallax = 0.035f,
        )
        haze(
            cx = w * 0.40f,
            cy = h * 0.78f,
            radius = r * 0.28f,
            color = Color(0xFFAD1457),
            alpha = 0.18f,
            parallax = 0.045f,
        )
        haze(
            cx = w * 0.66f,
            cy = h * 0.46f,
            radius = r * 0.22f,
            color = Color(0xFF00ACC1),
            alpha = 0.12f,
            parallax = 0.05f,
        )

        val rnd = Random(seed)
        withTransform({
            translate(
                left = -offsetX * 0.015f,
                top = -offsetY * 0.015f,
            )
        }) {
            repeat(350) {
                val x = rnd.nextFloat() * w
                val y = rnd.nextFloat() * h
                val rad = 0.5f + rnd.nextFloat() * 1.5f
                val a = 0.35f + rnd.nextFloat() * 0.45f
                drawCircle(
                    color = Color.White.copy(alpha = a),
                    radius = rad,
                    center =
                        Offset(
                            x = x,
                            y = y,
                        ),
                )
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
            val state =
                rememberInfiniteTransition(
                    label = "twinkle",
                ).animateFloat(
                    initialValue = 0f,
                    targetValue = (2f * PI).toFloat(),
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis = 2000,
                                    easing = LinearEasing,
                                ),
                        ),
                    label = "tick",
                )
            state.value
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
                    val x = r * cos(s.angleRad)
                    val y = r * sin(s.angleRad)

                    val base = if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity
                    val phase = ((s.id.hashCode() % 628) / 100f)
                    val twinkle = ((kotlin.math.sin(tick + phase) + 1f) / 2f)
                    val haloRadius = base * if (s.isWishNet) 3f else 2f
                    val haloAlpha =
                        if (reduceMotion) 0.10f * s.intensity else 0.08f + 0.20f * twinkle * s.intensity

                    drawCircle(
                        color = color.copy(alpha = haloAlpha),
                        radius = haloRadius,
                        center =
                            Offset(
                                x = x,
                                y = y,
                            ),
                        blendMode = BlendMode.Plus,
                    )
                    drawCircle(
                        color = color.copy(alpha = 0.95f),
                        radius = base,
                        center =
                            Offset(
                                x = x,
                                y = y,
                            ),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.90f),
                        radius = base * 0.55f,
                        center =
                            Offset(
                                x = x,
                                y = y,
                            ),
                    )
                }
            }
        }
    }
}

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
                0 ->
                    TextSignal(
                        text = "Hello #$i",
                    )
                1 ->
                    EmojiSignal(
                        emoji = listOf("✨", "⭐", "🚀", "💙").random(),
                    )
                2 ->
                    ImageSignal(
                        imageUri = "content://demo/$i",
                    )
                else ->
                    MixSignal(
                        parts = emptyList(),
                    )
            }
    }
    return out
}
