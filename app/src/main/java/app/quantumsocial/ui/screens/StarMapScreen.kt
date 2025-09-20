package app.quantumsocial.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.core.ui.theme.GalaxyGradient
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

        StarCanvas(stars = filtered)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StarCanvas(stars: List<StarPoint>) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformState =
        rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(0.5f, 5f)
            offsetX += panChange.x
            offsetY += panChange.y
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .transformable(transformState),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeColor = Color.White.copy(alpha = 0.15f)
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
                    val sizePx = if (s.isWishNet) 6f + 6f * s.intensity else 3f + 4f * s.intensity
                    drawCircle(
                        color = color,
                        radius = sizePx,
                        center =
                            androidx.compose.ui.geometry.Offset(
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
                0 -> TextSignal(text = "Hello #$i")
                1 -> EmojiSignal(emoji = listOf("✨", "⭐", "🚀", "💙").random())
                2 -> ImageSignal(imageUri = "content://demo/$i")
                else -> MixSignal(parts = emptyList())
            }
    }
    return out
}
