package app.quantumsocial.core.model

/**
 * ZenNet (bod 8): bublina ticha – jedna minúta pokoja bez slov.
 * Čisto výpočtová trieda, UI si len pýta zostávajúci čas a priebeh.
 */
data class ZenSession(
    val startedAt: Long,
    val durationMs: Long = DEFAULT_DURATION_MS,
) {
    fun remainingMs(now: Long): Long = (startedAt + durationMs - now).coerceIn(0L, durationMs)

    /** 0f na začiatku, 1f na konci. */
    fun progress(now: Long): Float = 1f - remainingMs(now).toFloat() / durationMs

    fun isFinished(now: Long): Boolean = remainingMs(now) == 0L

    companion object {
        const val DEFAULT_DURATION_MS = 60_000L
    }
}
