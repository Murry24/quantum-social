package app.quantumsocial.core.model

/**
 * Spojenia medzi hviezdami bez servera – zatiaľ s ukážkovými hviezdami (server ich neskôr nahradí skutočnými).
 * 10 Serendipity, 22 Constellations, 23 SoulLinks.
 */
object DemoStars {
    /** Ukážkové hviezdy sveta – stabilné id, aby mali stále rovnaké meno, podpis a melódiu. */
    val ids: List<String> = List(60) { "demo-star-${it + 1}" }
}

/** Serendipity (bod 10): každý deň jeden náhodný „svetelný most“ 1:1 na 24 hodín. */
object Serendipity {
    fun matchFor(
        myStarId: String,
        epochDay: Long,
        candidates: List<String> = DemoStars.ids,
    ): String? {
        val pool = candidates.filter { it != myStarId }
        if (pool.isEmpty()) return null
        val seed = myStarId.hashCode().toLong() * 31 + epochDay
        return pool[Math.floorMod(seed, pool.size.toLong()).toInt()]
    }
}

/** Constellations (bod 22): dočasné súhvezdie do 10 hviezd na tému a emóciu, žije 3 dni. */
data class Constellation(
    val id: String,
    val theme: String,
    val emotion: Emotion,
    val members: List<String>,
    val createdDay: Long,
) {
    val isFull: Boolean get() = members.size >= MAX_MEMBERS

    fun isAlive(today: Long): Boolean = today - createdDay < LIFE_DAYS

    fun daysLeft(today: Long): Long = (LIFE_DAYS - (today - createdDay)).coerceAtLeast(0)

    fun join(starId: String): Constellation? =
        if (starId in members || isFull) null else copy(members = members + starId)

    fun leave(starId: String): Constellation = copy(members = members - starId)

    companion object {
        const val MAX_MEMBERS = 10
        const val LIFE_DAYS = 3L

        private val THEMES =
            listOf(
                "Ranné svetlo", "Nočné myšlienky", "Malé radosti", "Odvaha", "Ticho v meste",
                "Hudba duše", "Cestovanie", "Nový začiatok", "Príroda", "Vďačnosť",
            )

        /** Súhvezdia dňa – 3 nové každý deň, každé už má pár ukážkových hviezd. */
        fun ofDay(epochDay: Long): List<Constellation> =
            List(3) { i ->
                val k = Math.floorMod(epochDay * 3 + i, THEMES.size.toLong()).toInt()
                val start = Math.floorMod(epochDay * 7 + i * 11, DemoStars.ids.size.toLong()).toInt()
                Constellation(
                    id = "c-$epochDay-$i",
                    theme = THEMES[k],
                    emotion = Emotion.values()[(k + i) % Emotion.values().size],
                    members = List(3 + (k % 5)) { DemoStars.ids[(start + it) % DemoStars.ids.size] },
                    createdDay = epochDay,
                )
            }
    }
}

/** SoulLinks (bod 23): dlhodobé spojenie 1:1 až po obojstrannom súhlase, najviac 5. */
enum class LinkStatus { Pending, Active }

data class SoulLink(
    val starId: String,
    val status: LinkStatus,
    val sinceDay: Long,
)

data class SoulLinks(
    val links: List<SoulLink> = emptyList(),
) {
    fun of(starId: String): SoulLink? = links.firstOrNull { it.starId == starId }

    fun request(
        starId: String,
        today: Long,
    ): SoulLinks? {
        if (of(starId) != null || links.size >= MAX_LINKS) return null
        return copy(links = links + SoulLink(starId, LinkStatus.Pending, today))
    }

    /** Druhá hviezda súhlasila (s ukážkovými hviezdami: na druhý deň). */
    fun accept(
        starId: String,
        today: Long,
    ): SoulLinks =
        copy(
            links = links.map { if (it.starId == starId) it.copy(status = LinkStatus.Active, sinceDay = today) else it },
        )

    /** Ukážkové hviezdy súhlasia deň po žiadosti – server to neskôr nahradí skutočnou odpoveďou. */
    fun demoAcceptances(today: Long): SoulLinks =
        copy(
            links =
                links.map {
                    if (it.status == LinkStatus.Pending && today > it.sinceDay) it.copy(status = LinkStatus.Active) else it
                },
        )

    fun remove(starId: String): SoulLinks = copy(links = links.filter { it.starId != starId })

    fun encode(): String = links.joinToString("\n") { "${it.starId}\t${it.status.name}\t${it.sinceDay}" }

    companion object {
        const val MAX_LINKS = 5

        fun decode(stored: String?): SoulLinks =
            SoulLinks(
                stored.orEmpty().lines().mapNotNull { line ->
                    val p = line.split("\t")
                    if (p.size != 3) return@mapNotNull null
                    val status = LinkStatus.values().firstOrNull { it.name == p[1] } ?: return@mapNotNull null
                    SoulLink(p[0], status, p[2].toLongOrNull() ?: return@mapNotNull null)
                },
            )
    }
}
