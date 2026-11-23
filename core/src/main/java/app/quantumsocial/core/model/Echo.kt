package app.quantumsocial.core.model

import app.quantumsocial.core.i18n.tr

/**
 * EchoNet (bod 11): zvuky dňa – krátka nahrávka (5–15 s) s náladou.
 * Ozveny žijú len do konca dňa a za deň ich môže byť najviac [MAX_PER_DAY].
 */
data class Echo(
    val id: String,
    val filePath: String,
    val durationMs: Long,
    val mood: Emotion,
    val day: Long,
)

data class EchoDay(
    val echoes: List<Echo> = emptyList(),
) {
    fun today(day: Long): List<Echo> = echoes.filter { it.day == day }

    fun canAdd(day: Long): Boolean = today(day).size < MAX_PER_DAY

    fun add(echo: Echo): EchoDay? {
        if (problem(echo.durationMs) != null || !canAdd(echo.day)) return null
        return copy(echoes = listOf(echo) + today(echo.day))
    }

    /** Staré ozveny (iný deň) – app zmaže aj ich súbory. */
    fun expired(day: Long): List<Echo> = echoes.filter { it.day != day }

    fun encode(): String =
        echoes.joinToString("\n") { e -> listOf(e.id, e.day, e.durationMs, e.mood.name, e.filePath).joinToString("\t") }

    companion object {
        const val MIN_MS = 5_000L
        const val MAX_MS = 15_000L
        const val MAX_PER_DAY = 3

        fun problem(durationMs: Long): String? =
            when {
                durationMs < MIN_MS -> tr("Ozvena má mať aspoň 5 sekúnd.")
                durationMs > MAX_MS + 500 -> tr("Ozvena môže mať najviac 15 sekúnd.")
                else -> null
            }

        fun decode(stored: String?): EchoDay =
            EchoDay(
                stored.orEmpty().lines().mapNotNull { line ->
                    val p = line.split("\t", limit = 5)
                    if (p.size != 5) return@mapNotNull null
                    Echo(
                        id = p[0],
                        day = p[1].toLongOrNull() ?: return@mapNotNull null,
                        durationMs = p[2].toLongOrNull() ?: return@mapNotNull null,
                        mood = Emotion.values().firstOrNull { it.name == p[3] } ?: return@mapNotNull null,
                        filePath = p[4],
                    )
                },
            )
    }
}
