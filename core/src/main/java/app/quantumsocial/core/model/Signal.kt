package app.quantumsocial.core.model

import java.util.UUID

enum class SignalCategory { Ephemeral, WishNet }

sealed interface Signal {
    val id: String
    val createdAt: Long
    val category: SignalCategory
    val expireOnRead: Boolean
    val ttlMillis: Long?

    val expiresAt: Long? get() = ttlMillis?.let { createdAt + it }

    fun remainingMillis(now: Long = System.currentTimeMillis()): Long? = expiresAt?.let { it - now }

    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = remainingMillis(now)?.let { it <= 0 } ?: false
}

/** Text (max 200 znakov sa kontroluje v UI). */
data class TextSignal(
    val text: String,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.Ephemeral,
    override val expireOnRead: Boolean = true,
    override val ttlMillis: Long? = 30_000L,
) : Signal

/** Jedna emoji. */
data class EmojiSignal(
    val emoji: String,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.Ephemeral,
    override val expireOnRead: Boolean = true,
    override val ttlMillis: Long? = 30_000L,
) : Signal

/** Audio: cesta k súboru + dĺžka v ms (limit nahrávania rieši UI). */
data class AudioSignal(
    val filePath: String,
    val durationMs: Int,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.Ephemeral,
    override val expireOnRead: Boolean = true,
    override val ttlMillis: Long? = 60_000L,
) : Signal

/** Obrázok: URI reťazec (Photo Picker). */
data class ImageSignal(
    val imageUri: String,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.Ephemeral,
    override val expireOnRead: Boolean = true,
    override val ttlMillis: Long? = 60_000L,
) : Signal

/** Svetelný záblesk. */
data class FlashSignal(
    val intensity: Float,
    val durationMs: Int = 300,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.Ephemeral,
    override val expireOnRead: Boolean = true,
    override val ttlMillis: Long? = 15_000L,
) : Signal

/** Mix – špeciálny (WishNet), neexpiruje čítaním, dlho trvá. */
data class MixSignal(
    val parts: List<Signal>,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.WishNet,
    override val expireOnRead: Boolean = false,
    override val ttlMillis: Long? = 86_400_000L,
) : Signal
