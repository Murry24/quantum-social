package app.quantumsocial.core.model

/**
 * Životný cyklus signálu (bod 3 plánu – efemérne správy).
 *
 * Alive     – signál normálne svieti
 * Expiring  – posledných [SignalLifecycle.EXPIRING_WINDOW_MS] pred koncom, postupne bledne
 * FadingOut – vypršal alebo bol prečítaný; [SignalLifecycle.FADE_OUT_MS] zhasína, potom zmizne
 */
enum class LifePhase { Alive, Expiring, FadingOut }

object SignalLifecycle {
    /** Ako dlho pred koncom sa signál označí ako „končí“. */
    const val EXPIRING_WINDOW_MS = 5_000L

    /** Ako dlho trvá animácia zhasnutia, kým signál úplne zmizne. */
    const val FADE_OUT_MS = 1_200L

    /** WishNet signály žijú dlhšie – 24 h (rovnako ako MixSignal). */
    const val WISHNET_TTL_MS = 86_400_000L

    /** Najnižší jas počas fázy Expiring (aby bol signál stále viditeľný). */
    private const val EXPIRING_MIN_BRIGHTNESS = 0.35f

    /**
     * @param fadingSince čas, odkedy signál zhasína (z InMemorySignalRepository.fadingSince),
     * alebo null, ak ešte nezačal zhasínať.
     */
    fun phase(
        signal: Signal,
        now: Long,
        fadingSince: Long?,
    ): LifePhase =
        when {
            fadingSince != null -> LifePhase.FadingOut
            signal.isExpired(now) -> LifePhase.FadingOut
            signal.remainingMillis(now)?.let { it <= EXPIRING_WINDOW_MS } == true -> LifePhase.Expiring
            else -> LifePhase.Alive
        }

    /** Jas signálu: 1f = plné svetlo, 0f = úplne zhasnutý. */
    fun brightness(
        signal: Signal,
        now: Long,
        fadingSince: Long?,
    ): Float {
        val fadeStart = fadingSince ?: signal.expiresAt?.takeIf { now >= it }
        if (fadeStart != null) {
            return (1f - (now - fadeStart).toFloat() / FADE_OUT_MS).coerceIn(0f, 1f)
        }
        val remaining = signal.remainingMillis(now) ?: return 1f
        if (remaining > EXPIRING_WINDOW_MS) return 1f
        val t = remaining.toFloat() / EXPIRING_WINDOW_MS
        return EXPIRING_MIN_BRIGHTNESS + (1f - EXPIRING_MIN_BRIGHTNESS) * t
    }
}
