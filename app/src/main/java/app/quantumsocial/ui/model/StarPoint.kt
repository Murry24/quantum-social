package app.quantumsocial.ui.model

import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.WorldLanguages
import kotlin.math.PI
import kotlin.random.Random

data class StarPoint(
    val id: String,
    val lang: String,
    val emotion: Emotion,
    val topic: String,
    // 0f..1f
    val intensity: Float,
    // 0f..1f (polomer)
    val distance: Float,
    val isWishNet: Boolean,
    // 0..2π
    val angleRad: Float,
    // 1f = plné svetlo, 0f = zhasnutá (bod 3 – životnosť signálu)
    val brightness: Float = 1f,
)

fun Signal.toStarPoint(): StarPoint {
    val seed = id.hashCode()
    val rnd = Random(seed)

    val lang = starLangForId(id)
    val topic = starTopicForId(id)
    val emotion = starEmotionForId(id)
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

// Vlastnosti hviezdy len z jej id – rovnaké na mape aj v profile (bod 5)
private val starLangs = WorldLanguages.codes
private val starTopics = listOf("Tech", "Life", "Art")

fun starLangForId(id: String): String = starLangs[absMod(id.hashCode(), starLangs.size)]

fun starTopicForId(id: String): String = starTopics[absMod(id.hashCode() / 3, starTopics.size)]

fun starEmotionForId(id: String): Emotion {
    val emotions = Emotion.values()
    return emotions[absMod(id.hashCode() / 5, emotions.size)]
}

private fun absMod(
    x: Int,
    m: Int,
): Int {
    val r = x % m
    return if (r < 0) r + m else r
}
