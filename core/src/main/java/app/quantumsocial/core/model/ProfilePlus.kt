package app.quantumsocial.core.model

/** Dnešná aura: farba a krátky odkaz, platí len do polnoci (efemérne ako všetko). */
data class DailyAura(
    val emotion: Emotion,
    val text: String,
    val day: Long,
) {
    fun isActive(today: Long): Boolean = day == today

    fun encode(): String = "${emotion.name}\t$day\t$text"

    companion object {
        const val MAX_TEXT = 40

        fun of(
            emotion: Emotion,
            text: String,
            today: Long,
        ) = DailyAura(emotion, text.replace('\n', ' ').trim().take(MAX_TEXT).trim(), today)

        fun decode(stored: String?): DailyAura? {
            val p = stored?.split('\t', limit = 3) ?: return null
            if (p.size < 3) return null
            val e = Emotion.values().firstOrNull { it.name == p[0] } ?: return null
            return DailyAura(e, p[2], p[1].toLongOrNull() ?: return null)
        }
    }
}

/** Čísla, z ktorých sa počítajú odznaky. */
data class BadgeStats(
    val lifetimeDust: Int = 0,
    val gratitudeDays: Set<Long> = emptySet(),
    val seeds: Int = 0,
    val galaxies: Int = 0,
    val activeSoulLinks: Int = 0,
    val flowers: Int = 0,
    val today: Long = 0,
) {
    /** Koľko dní v rade (končiac dnes alebo včera) som napísal vďačnosť. */
    val gratitudeStreak: Int
        get() {
            var d = if (today in gratitudeDays) today else today - 1
            var n = 0
            while (d in gratitudeDays) {
                n++
                d--
            }
            return n
        }
}

enum class Badge(val emoji: String, val label: String, val hint: String, val earned: (BadgeStats) -> Boolean) {
    FirstLight("🌟", "Prvé svetlo", "nazbieraj prvý StarDust", { it.lifetimeDust >= 1 }),
    Thankful("🙏", "Vďačné srdce", "3 dni vďačnosti v rade", { it.gratitudeStreak >= 3 }),
    ThankfulWeek("💛", "Týždeň vďaky", "7 dní vďačnosti v rade", { it.gratitudeStreak >= 7 }),
    Gardener("🌱", "Záhradník", "zasaď 3 semienka budúcnosti", { it.seeds >= 3 }),
    Florist("🌸", "Kvetinár", "dotiahni cieľ zo semienka do konca", { it.flowers >= 1 }),
    GalaxyBuilder("🌌", "Staviteľ galaxií", "buď v malej galaxii", { it.galaxies >= 1 }),
    SoulFriend("💞", "Spriaznená duša", "maj aktívny SoulLink", { it.activeSoulLinks >= 1 }),
    Shining("✨", "Žiariaca", "nazbieraj 100 StarDust", { it.lifetimeDust >= 100 }),
    Supernova("💥", "Supernova", "nazbieraj 1000 StarDust", { it.lifetimeDust >= 1000 }),
    ;

    companion object {
        fun earnedBy(stats: BadgeStats): List<Badge> = values().filter { it.earned(stats) }
    }
}
