package app.quantumsocial.core.model

import kotlin.random.Random

/** Druh odkazu v snovom kruhu. */
enum class DreamNoteKind(
    val emoji: String,
    val label: String,
) {
    Meaning("💭", "Význam"),
    Path("🧭", "Kam ma vedie"),
    Feeling("💗", "Pocit"),
}

/** Odkaz v kruhu. Autor je len anonymné číslo – nikto nevie, kto je kto. */
data class DreamNote(
    val id: String,
    // 0 = ja, inak číslo snívajúcej hviezdy v kruhu
    val author: Int,
    val kind: DreamNoteKind,
    val emotion: Emotion?,
    val text: String,
    val day: Long,
)

/** Snový kruh: hviezdy, ktoré v tom istom 3-dňovom okne snívali o rovnakom symbole. */
data class DreamCircle(
    val symbol: DreamSymbol,
    val window: Long,
    val myNotes: List<DreamNote> = emptyList(),
    val resonated: Set<String> = emptySet(),
) {
    val key: String get() = "${symbol.name}@$window"

    fun daysLeft(today: Long): Long = window + DreamCircles.LIFE_DAYS - today

    fun isAlive(today: Long): Boolean = daysLeft(today) > 0
}

/**
 * Snové kruhy (rozšírenie bodu 12). Kým nie je server, ostatných snívajúcich hrajú ukážkové hviezdy –
 * ich odkazy sú vždy rovnaké pre ten istý kruh a deň.
 */
object DreamCircles {
    const val LIFE_DAYS = 3
    const val MAX_NOTE = 160

    // ukážkové odkazy (preklad cez tr v UI)
    val demoNotes: Map<DreamNoteKind, List<String>> =
        mapOf(
            DreamNoteKind.Meaning to
                listOf(
                    "Myslím, že ten sen hovorí o slobode, ktorá mi chýba.",
                    "Pre mňa to znamenalo, že sa niečo končí a niečo začína.",
                    "Cítim, že mi sen pripomína niečo z detstva.",
                    "Asi je to o tom, že sa bojím zmeny – ale aj sa na ňu teším.",
                    "Myslím, že ten sen chcel, aby som spomalil.",
                ),
            DreamNoteKind.Path to
                listOf(
                    "Po tom sne som sa rozhodol skúsiť niečo nové.",
                    "Ten sen ma vedie k tomu, aby som sa ozval starému priateľovi.",
                    "Chcem viac času pre seba – tam ma ten sen ťahá.",
                    "Asi je čas prestať sa báť a urobiť prvý krok.",
                ),
            DreamNoteKind.Feeling to
                listOf(
                    "Zobudil som sa pokojný, ako po dobrom spánku.",
                    "Bolo to zvláštne, ale nebál som sa.",
                    "Ešte ráno som cítil ľahkosť.",
                    "Trochu ma to rozrušilo, ale bolo to krásne.",
                ),
        )

    fun windowOf(day: Long): Long = day - Math.floorMod(day, LIFE_DAYS.toLong())

    private fun rnd(
        symbol: DreamSymbol,
        window: Long,
        salt: Long = 0,
    ) = Random(symbol.ordinal * 1_000_003L + window * 31 + salt)

    /** Koľko hviezd okrem mňa je v kruhu (3–12). */
    fun others(
        symbol: DreamSymbol,
        window: Long,
    ): Int = rnd(symbol, window).nextInt(3, 13)

    /** Odkazy ostatných snívajúcich od otvorenia kruhu po dnešok (2 denne). */
    fun othersNotes(
        circle: DreamCircle,
        today: Long,
    ): List<DreamNote> {
        val members = others(circle.symbol, circle.window)
        val last = minOf(today, circle.window + LIFE_DAYS - 1)
        return (circle.window..last).flatMap { day ->
            val r = rnd(circle.symbol, circle.window, day)
            List(2) { i ->
                val kind = DreamNoteKind.values()[r.nextInt(DreamNoteKind.values().size)]
                val pool = demoNotes.getValue(kind)
                DreamNote(
                    id = "d${circle.key}-$day-$i",
                    author = r.nextInt(1, members + 1),
                    kind = kind,
                    emotion = Emotion.values()[r.nextInt(Emotion.values().size)],
                    text = pool[r.nextInt(pool.size)],
                    day = day,
                )
            }
        }
    }

    /** Všetky odkazy, najnovšie hore. */
    fun notes(
        circle: DreamCircle,
        today: Long,
    ): List<DreamNote> = (othersNotes(circle, today) + circle.myNotes).sortedByDescending { it.day }

    /** Ukážkové rezonancie odkazu (0–6) + moja. */
    fun resonance(
        circle: DreamCircle,
        note: DreamNote,
    ): Int {
        val demo = if (note.author == 0) 0 else Math.floorMod(note.id.hashCode(), 7)
        return demo + if (note.id in circle.resonated) 1 else 0
    }

    /** Spoločná niť: nálada, ktorú v kruhu cítilo najviac hviezd, a koľko ich bolo. */
    fun sharedThread(notes: List<DreamNote>): Pair<Emotion, Int>? =
        notes.mapNotNull { it.emotion }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.toPair()
}

/** Moje snové kruhy – čisté pravidlá + zápis na uloženie. */
data class DreamCirclesState(
    val circles: List<DreamCircle> = emptyList(),
) {
    fun alive(today: Long) = DreamCirclesState(circles.filter { it.isAlive(today) })

    fun joined(
        symbol: DreamSymbol,
        today: Long,
    ): DreamCircle? = circles.firstOrNull { it.symbol == symbol && it.window == DreamCircles.windowOf(today) }

    fun join(
        symbol: DreamSymbol,
        today: Long,
    ): DreamCirclesState =
        if (joined(symbol, today) != null) {
            this
        } else {
            copy(circles = alive(today).circles + DreamCircle(symbol, DreamCircles.windowOf(today)))
        }

    private fun change(
        key: String,
        f: (DreamCircle) -> DreamCircle,
    ) = copy(circles = circles.map { if (it.key == key) f(it) else it })

    /** @return null ak je text prázdny alebo dlhý */
    fun post(
        key: String,
        id: String,
        kind: DreamNoteKind,
        emotion: Emotion?,
        text: String,
        today: Long,
    ): DreamCirclesState? {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.isEmpty() || clean.length > DreamCircles.MAX_NOTE || circles.none { it.key == key }) return null
        return change(key) { it.copy(myNotes = it.myNotes + DreamNote(id, 0, kind, emotion, clean, today)) }
    }

    fun toggleResonance(
        key: String,
        noteId: String,
    ) = change(key) { c ->
        c.copy(resonated = if (noteId in c.resonated) c.resonated - noteId else c.resonated + noteId)
    }

    /** Riadky: C symbol okno | N kľúč id druh nálada deň text | R kľúč id. */
    fun encode(): String =
        circles.flatMap { c ->
            listOf("C\t${c.symbol.name}\t${c.window}") +
                c.myNotes.map { n ->
                    listOf("N", c.key, n.id, n.kind.name, n.emotion?.name ?: "", n.day, n.text).joinToString("\t")
                } +
                c.resonated.map { "R\t${c.key}\t$it" }
        }.joinToString("\n")

    companion object {
        fun decode(stored: String?): DreamCirclesState {
            var s = DreamCirclesState()
            stored.orEmpty().lines().forEach { line ->
                val p = line.split("\t", limit = 7)
                runCatching {
                    s =
                        when (p[0]) {
                            "C" -> s.copy(circles = s.circles + DreamCircle(DreamSymbol.valueOf(p[1]), p[2].toLong()))
                            "N" -> {
                                val note =
                                    DreamNote(
                                        p[2], 0, DreamNoteKind.valueOf(p[3]),
                                        Emotion.values().firstOrNull { it.name == p[4] }, p[6], p[5].toLong(),
                                    )
                                s.change(p[1]) { it.copy(myNotes = it.myNotes + note) }
                            }
                            "R" -> s.change(p[1]) { it.copy(resonated = it.resonated + p[2]) }
                            else -> s
                        }
                }
            }
            return s
        }
    }
}
