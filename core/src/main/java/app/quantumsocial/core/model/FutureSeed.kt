package app.quantumsocial.core.model

import kotlin.random.Random

/** Veľkosť cieľa: koľko dní zalievania treba, kým semienko rozkvitne. */
enum class SeedSize(
    val label: String,
    val emoji: String,
    val days: Int,
) {
    Small("Malé", "🌰", 7),
    Bush("Ker", "🌿", 21),
    Tree("Strom", "🌳", 30),
}

/** Ako sa cieľ skončil. „Ešte nie“ nie je výsledok – len predĺži rast. */
enum class SeedOutcome { Done, Changed }

/** Jeden malý krok k cieľu – zapíše sa pri zaliatí. */
data class SeedStep(
    val day: Long,
    val emotion: Emotion?,
    val text: String,
)

enum class SeedHelpKind { Rain, Tip }

/** Pomoc od inej hviezdy: 🌧️ dážď podpory alebo 💡 rada. */
data class SeedHelp(
    val kind: SeedHelpKind,
    val day: Long,
    val text: String = "",
)

/**
 * FutureSeeds → FutureBlooms (bod 30): semienko je cieľ alebo malý sen. Zalieva sa raz denne
 * malým krokom; po [SeedSize.days] zaliatiach rozkvitne a používateľ úprimne povie, ako to dopadlo.
 */
data class FutureSeed(
    val id: String,
    val text: String,
    val plantedDay: Long,
    val waterings: Int = 0,
    val lastWateredDay: Long? = null,
    // rozkvitnuté semienko pustené do WishNetu
    val released: Boolean = false,
    val size: SeedSize = SeedSize.Small,
    val steps: List<SeedStep> = emptyList(),
    // „Ešte nie“ pridá ďalšie dni rastu
    val extraDays: Int = 0,
    val outcome: SeedOutcome? = null,
    // od kedy galaxia pomáha (null = nezdieľané)
    val sharedDay: Long? = null,
) {
    val target: Int get() = size.days + extraDays

    val stage: SeedStage
        get() =
            when {
                waterings >= target -> SeedStage.Bloom
                waterings * 2 >= target -> SeedStage.Bud
                waterings >= 1 -> SeedStage.Sprout
                else -> SeedStage.Seed
            }

    /** Nezaliate [THIRSTY_DAYS] dní – jemne pripomenieme, nič sa nestratí. */
    fun isThirsty(today: Long): Boolean =
        stage != SeedStage.Bloom && today - (lastWateredDay ?: plantedDay) >= THIRSTY_DAYS

    fun canWater(today: Long): Boolean = stage != SeedStage.Bloom && lastWateredDay != today

    fun water(
        today: Long,
        emotion: Emotion? = null,
        note: String = "",
    ): FutureSeed? {
        if (!canWater(today)) return null
        val clean = clean(note).take(MAX_STEP)
        val step = if (clean.isEmpty() && emotion == null) null else SeedStep(today, emotion, clean)
        return copy(waterings = waterings + 1, lastWateredDay = today, steps = steps + listOfNotNull(step))
    }

    /** Čaká na odpoveď „Splnilo sa?“ */
    val awaitsAnswer: Boolean get() = stage == SeedStage.Bloom && outcome == null

    /** Farby kvetu = nálady, s ktorými sa zalievalo (aspoň jedna). */
    val flowerEmotions: List<Emotion>
        get() = steps.mapNotNull { it.emotion }.distinct().ifEmpty { listOf(Emotion.Joy) }

    companion object {
        const val MAX_SEEDS = 3
        const val MAX_TEXT = 100
        const val MAX_STEP = 80
        const val THIRSTY_DAYS = 2
        const val NOT_YET_DAYS = 7

        // bez tabulátorov a nových riadkov – kvôli zápisu
        fun clean(text: String) = text.replace(Regex("\\s+"), " ").trim()
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

/**
 * Pomoc z galaxie k zdieľanému semienku. Kým nie je server, pomáhajú ukážkové hviezdy –
 * každý deň 0–2 pomoci, vždy rovnako pre to isté semienko a deň.
 */
object SeedHelpers {
    val tips =
        listOf(
            "Rozdeľ to na menšie kroky – jeden denne stačí.",
            "Povedz o svojom cieli niekomu blízkemu.",
            "Napíš si, prečo to chceš – pomôže to v ťažkých dňoch.",
            "Aj 5 minút denne je viac ako nič.",
            "Keď vynecháš deň, nevadí. Pokračuj zajtra.",
            "Odmeň sa za každý malý pokrok.",
            "Priprav si veci večer, ráno pôjde ľahšie.",
            "Spoj to s niečím, čo robíš každý deň.",
            "Predstav si, ako sa budeš cítiť, keď to dokážeš.",
            "Nesnaž sa o dokonalosť, stačí byť lepší ako včera.",
            "Nájdi si niekoho s podobným cieľom.",
            "Zapíš si, čo ti dnes pomohlo a čo prekážalo.",
        )

    fun helpFor(
        seed: FutureSeed,
        today: Long,
    ): List<SeedHelp> {
        val from = seed.sharedDay ?: return emptyList()
        val last = if (seed.outcome != null) minOf(today, seed.lastWateredDay ?: today) else today
        return (maxOf(from, last - MAX_HELP_DAYS)..last).flatMap { day ->
            val rnd = Random(seed.id.hashCode() * 31L + day)
            List(rnd.nextInt(3)) {
                if (rnd.nextInt(10) < 6) {
                    SeedHelp(SeedHelpKind.Rain, day)
                } else {
                    SeedHelp(SeedHelpKind.Tip, day, tips[rnd.nextInt(tips.size)])
                }
            }
        }
    }

    private const val MAX_HELP_DAYS = 60
}

/** Záhrada semienok – čisté pravidlá + jednoduchý zápis na uloženie. */
data class SeedGarden(
    val seeds: List<FutureSeed> = emptyList(),
) {
    val growing: List<FutureSeed> get() = seeds.filter { it.outcome == null }

    /** Záhrada kvetov: splnené ciele ostávajú navždy. */
    val flowers: List<FutureSeed> get() = seeds.filter { it.outcome == SeedOutcome.Done }

    fun plant(
        id: String,
        text: String,
        today: Long,
        size: SeedSize = SeedSize.Small,
    ): SeedGarden? {
        val clean = FutureSeed.clean(text)
        val active = seeds.count { it.stage != SeedStage.Bloom }
        if (clean.isEmpty() || clean.length > FutureSeed.MAX_TEXT || active >= FutureSeed.MAX_SEEDS) return null
        return copy(seeds = listOf(FutureSeed(id, clean, today, size = size)) + seeds)
    }

    private fun change(
        id: String,
        f: (FutureSeed) -> FutureSeed?,
    ): SeedGarden? {
        val next = seeds.firstOrNull { it.id == id }?.let(f) ?: return null
        return copy(seeds = seeds.map { if (it.id == id) next else it })
    }

    fun water(
        id: String,
        today: Long,
        emotion: Emotion? = null,
        note: String = "",
    ): SeedGarden? = change(id) { it.water(today, emotion, note) }

    /** Požiadať galaxiu o pomoc (raz). */
    fun share(
        id: String,
        today: Long,
    ): SeedGarden? = change(id) { s -> s.takeIf { it.sharedDay == null }?.copy(sharedDay = today) }

    /** „Ešte nie“ – semienko dostane ďalších [FutureSeed.NOT_YET_DAYS] dní. */
    fun notYet(id: String): SeedGarden? =
        change(id) { s -> s.takeIf { it.awaitsAnswer }?.copy(extraDays = s.extraDays + FutureSeed.NOT_YET_DAYS) }

    /** Splnilo sa → kvet v záhrade. Zmenilo sa → kompost (semienko zmizne). */
    fun finish(
        id: String,
        outcome: SeedOutcome,
    ): SeedGarden? {
        val seed = seeds.firstOrNull { it.id == id }?.takeIf { it.awaitsAnswer } ?: return null
        return when (outcome) {
            SeedOutcome.Done -> change(id) { seed.copy(outcome = SeedOutcome.Done) }
            SeedOutcome.Changed -> remove(id)
        }
    }

    fun remove(id: String): SeedGarden = copy(seeds = seeds.filterNot { it.id == id })

    /** Splnený cieľ sa dá raz pustiť do WishNetu. */
    fun release(id: String): SeedGarden? =
        change(id) { s -> s.takeIf { it.outcome == SeedOutcome.Done && !it.released }?.copy(released = true) }

    /** Riadky: S (semienko, text na konci) a P (krok). Starý zápis bez S sa ešte načíta. */
    fun encode(): String =
        seeds.flatMap { s ->
            val head =
                listOf(
                    "S", s.id, s.plantedDay, s.waterings, s.lastWateredDay ?: "", if (s.released) "1" else "0",
                    s.size.name, s.extraDays, s.outcome?.name ?: "", s.sharedDay ?: "", s.text,
                ).joinToString("\t")
            listOf(head) + s.steps.map { listOf("P", s.id, it.day, it.emotion?.name ?: "", it.text).joinToString("\t") }
        }.joinToString("\n")

    companion object {
        fun decode(stored: String?): SeedGarden {
            val seeds = mutableListOf<FutureSeed>()
            val steps = mutableMapOf<String, MutableList<SeedStep>>()
            stored.orEmpty().lines().forEach { line ->
                runCatching {
                    when {
                        line.startsWith("S\t") -> {
                            val p = line.split("\t", limit = 11)
                            seeds +=
                                FutureSeed(
                                    id = p[1],
                                    plantedDay = p[2].toLong(),
                                    waterings = p[3].toInt(),
                                    lastWateredDay = p[4].toLongOrNull(),
                                    released = p[5] == "1",
                                    size = SeedSize.valueOf(p[6]),
                                    extraDays = p[7].toInt(),
                                    outcome = p[8].takeIf { it.isNotEmpty() }?.let { SeedOutcome.valueOf(it) },
                                    sharedDay = p[9].toLongOrNull(),
                                    text = p[10],
                                )
                        }
                        line.startsWith("P\t") -> {
                            val p = line.split("\t", limit = 5)
                            val emotion = Emotion.values().firstOrNull { it.name == p[3] }
                            steps.getOrPut(p[1]) { mutableListOf() } += SeedStep(p[2].toLong(), emotion, p[4])
                        }
                        else -> {
                            // starý zápis: id, deň, zaliatia, posledné, text[, pustené]
                            val p = line.split("\t", limit = 6)
                            if (p.size >= 5) {
                                seeds +=
                                    FutureSeed(
                                        id = p[0],
                                        text = p[4],
                                        plantedDay = p[1].toLong(),
                                        waterings = p[2].toInt(),
                                        lastWateredDay = p[3].toLongOrNull(),
                                        released = p.getOrNull(5) == "1",
                                    )
                            }
                        }
                    }
                }
            }
            return SeedGarden(seeds.map { it.copy(steps = steps[it.id].orEmpty()) })
        }
    }
}
