package app.quantumsocial.core.model

import java.util.UUID
import kotlin.random.Random

/** Kam signál letí (bod 8): konkrétnej hviezde, alebo náhodne do chaosu. */
sealed interface SignalTarget {
    data class Star(val starId: String) : SignalTarget

    data object Chaos : SignalTarget
}

/**
 * Pocitový pulz – signál bez slov, len emócia a svetlo (bod 8).
 * Voliteľne nesie 1 emoji z [EmojiCatalog] a 1 svetelný efekt.
 * Zhasne po prečítaní alebo po 30 s.
 */
data class PulseSignal(
    val emotion: Emotion,
    val target: SignalTarget,
    val emoji: String? = null,
    val effect: PulseEffect? = null,
    // bod 27: naliehavý pulz – zvýraznený, stojí StarDust, max 1× denne
    val urgent: Boolean = false,
    override val id: String = UUID.randomUUID().toString(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val category: SignalCategory = SignalCategory.Ephemeral,
    override val expireOnRead: Boolean = true,
    override val ttlMillis: Long? = 30_000L,
) : Signal {
    init {
        require(emoji == null || EmojiCatalog.isAllowed(emoji)) { "pulz nesie len emoji z katalógu" }
    }
}

/** Výber náhodnej hviezdy pre signál „do chaosu“. */
object ChaosPicker {
    /**
     * @param candidates id hviezd, ktoré sú práve viditeľné
     * @param exclude id, ktoré sa vybrať nemá (napr. vlastná hviezda)
     * @return id vybranej hviezdy alebo null, ak nie je z čoho vyberať
     */
    fun pick(
        candidates: List<String>,
        exclude: String? = null,
        random: Random = Random.Default,
    ): String? {
        val pool = candidates.filter { it != exclude }.distinct()
        if (pool.isEmpty()) return null
        return pool[random.nextInt(pool.size)]
    }
}
