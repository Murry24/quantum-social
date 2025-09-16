package app.quantumsocial.core.model

import java.util.UUID

sealed interface Signal {
    val id: String
    val createdAt: Long
}

/** Textový signál (max 200 znakov sa kontroluje v UI). */
data class TextSignal(
    val text: String,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
) : Signal

/** Emodži signál – jedna emoji. */
data class EmojiSignal(
    val emoji: String,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
) : Signal

/** Audio signál – cesta k súboru a dĺžka v ms (limit 15 s sa rieši v UI). */
data class AudioSignal(
    val filePath: String,
    val durationMs: Int,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
) : Signal

/** Obrázkový signál – URI obrázka (Photo Picker). */
data class ImageSignal(
    val imageUri: String,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
) : Signal

/** Svetelný záblesk – intenzita 0f..1f a trvanie v ms. */
data class FlashSignal(
    val intensity: Float,
    val durationMs: Int = 300,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
) : Signal

/** Mix viacerých signálov. */
data class MixSignal(
    val parts: List<Signal>,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
) : Signal
