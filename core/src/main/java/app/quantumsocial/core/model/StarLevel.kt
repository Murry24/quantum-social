package app.quantumsocial.core.model

/** Svetelné levely (bod 16): hviezda rastie s nazbieraným StarDust – nie s počtom sledovateľov. */
enum class StarLevel(
    val label: String,
    val emoji: String,
    val minDust: Int,
) {
    Spark("Iskra", "✦", 0),
    Star("Hviezda", "⭐", 20),
    Pulsar("Pulzár", "💫", 60),
    Nebula("Hmlovina", "🌌", 150),
    Supernova("Supernova", "🌟", 400),
    ;

    val next: StarLevel? get() = values().getOrNull(ordinal + 1)

    companion object {
        fun of(dust: Int): StarLevel = values().last { dust >= it.minDust }

        /** 0..1 – ako ďaleko je hviezda k ďalšiemu levelu (Supernova = 1). */
        fun progress(dust: Int): Float {
            val level = of(dust)
            val next = level.next ?: return 1f
            return ((dust - level.minDust).toFloat() / (next.minDust - level.minDust)).coerceIn(0f, 1f)
        }
    }
}
