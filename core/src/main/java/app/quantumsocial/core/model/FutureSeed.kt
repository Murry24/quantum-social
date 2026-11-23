package app.quantumsocial.core.model

/**
 * FutureSeeds → FutureBlooms (bod 30): zasadíš semienko budúcnosti (prianie) a zalievaš ho
 * raz denne. Po [WATERINGS_TO_BLOOM] zaliatiach rozkvitne. Najviac [MAX_SEEDS] naraz.
 */
data class FutureSeed(
    val id: String,
    val text: String,
    val plantedDay: Long,
    val waterings: Int = 0,
    val lastWateredDay: Long? = null,
) {
    val stage: SeedStage
        get() =
            when {
                waterings >= WATERINGS_TO_BLOOM -> SeedStage.Bloom
                waterings >= 3 -> SeedStage.Bud
                waterings >= 1 -> SeedStage.Sprout
                else -> SeedStage.Seed
            }

    fun canWater(today: Long): Boolean = stage != SeedStage.Bloom && lastWateredDay != today

    fun water(today: Long): FutureSeed? =
        if (canWater(today)) copy(waterings = waterings + 1, lastWateredDay = today) else null

    companion object {
        const val WATERINGS_TO_BLOOM = 5
        const val MAX_SEEDS = 3
        const val MAX_TEXT = 100
    }
}

enum class SeedStage(
    val label: String,
    val emoji: String,
) {
    Seed("Semienko", "🌰"),
    Sprout("Klíček", "🌱"),
    Bud("Púčik", "🌿"),
    Bloom("Rozkvitnuté", "🌸"),
}

/** Záhrada semienok – čisté pravidlá + jednoduchý zápis na uloženie. */
data class SeedGarden(
    val seeds: List<FutureSeed> = emptyList(),
) {
    fun plant(
        id: String,
        text: String,
        today: Long,
    ): SeedGarden? {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        val growing = seeds.count { it.stage != SeedStage.Bloom }
        if (clean.isEmpty() || clean.length > FutureSeed.MAX_TEXT || growing >= FutureSeed.MAX_SEEDS) return null
        return copy(seeds = listOf(FutureSeed(id, clean, today)) + seeds)
    }

    fun water(
        id: String,
        today: Long,
    ): SeedGarden? {
        val seed = seeds.firstOrNull { it.id == id }?.water(today) ?: return null
        return copy(seeds = seeds.map { if (it.id == id) seed else it })
    }

    fun encode(): String =
        seeds.joinToString("\n") { s ->
            listOf(s.id, s.plantedDay, s.waterings, s.lastWateredDay ?: "", s.text).joinToString("\t")
        }

    companion object {
        fun decode(stored: String?): SeedGarden =
            SeedGarden(
                stored.orEmpty().lines().mapNotNull { line ->
                    val p = line.split("\t", limit = 5)
                    if (p.size != 5) return@mapNotNull null
                    FutureSeed(
                        id = p[0],
                        text = p[4],
                        plantedDay = p[1].toLongOrNull() ?: return@mapNotNull null,
                        waterings = p[2].toIntOrNull() ?: return@mapNotNull null,
                        lastWateredDay = p[3].toLongOrNull(),
                    )
                },
            )
    }
}
