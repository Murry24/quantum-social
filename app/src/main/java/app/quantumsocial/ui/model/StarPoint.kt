package app.quantumsocial.ui.model

import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalCategory
import kotlin.math.PI
import kotlin.random.Random

data class StarPoint(
    val id: String,
    val lang: String,
    val emotion: Emotion,
    val topic: String,
    val intensity: Float, // 0f..1f
    val distance: Float, // 0f..1f (polomer)
    val isWishNet: Boolean,
    val angleRad: Float, // 0..2π
)

fun Signal.toStarPoint(): StarPoint {
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

private fun absMod(
    x: Int,
    m: Int,
): Int {
    val r = x % m
    return if (r < 0) r + m else r
}
