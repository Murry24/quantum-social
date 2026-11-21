package app.quantumsocial.core.model

/** GratNet (bod 14): jeden zápis vďačnosti denne; zlaté iskry žijú 7 dní a potom zhasnú. */
data class GratitudeEntry(
    val day: Long,
    val text: String,
)

data class GratitudeJournal(
    val entries: List<GratitudeEntry> = emptyList(),
) {
    fun canWrite(today: Long): Boolean = entries.none { it.day == today }

    /** @return nový denník, alebo null ak dnes už zápis je, alebo text je prázdny / dlhý */
    fun write(
        text: String,
        today: Long,
    ): GratitudeJournal? {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty() || clean.length > MAX_TEXT || !canWrite(today)) return null
        return copy(entries = listOf(GratitudeEntry(today, clean)) + entries).visible(today)
    }

    /** Len iskry z posledných [KEEP_DAYS] dní – staršie zhasli. */
    fun visible(today: Long): GratitudeJournal = copy(entries = entries.filter { today - it.day < KEEP_DAYS })

    fun encode(): String = entries.joinToString("\n") { "${it.day}\t${it.text}" }

    companion object {
        const val MAX_TEXT = 140
        const val KEEP_DAYS = 7

        fun decode(stored: String?): GratitudeJournal =
            GratitudeJournal(
                stored.orEmpty().lines().mapNotNull { line ->
                    val tab = line.indexOf('\t')
                    if (tab <= 0) return@mapNotNull null
                    val day = line.substring(0, tab).toLongOrNull() ?: return@mapNotNull null
                    GratitudeEntry(day, line.substring(tab + 1))
                },
            )
    }
}
