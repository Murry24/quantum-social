package app.quantumsocial.core.model

/**
 * StarChords (bod 28): osobná melódia hviezdy – 5 tónov z pentatoniky (vždy znie príjemne).
 * Odvodená z id hviezdy a „variantu“; variant sa smie zmeniť najviac raz za štvrťrok.
 */
data class StarChord(
    // frekvencie tónov v Hz
    val notesHz: List<Double>,
    // dĺžky tónov v ms
    val durationsMs: List<Int>,
) {
    companion object {
        // C dur pentatonika, dve oktávy (C4 … A5)
        private val SCALE =
            listOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00)

        fun of(
            starId: String,
            variant: Int = 0,
        ): StarChord {
            var h = 0x811C9DC5L
            for (ch in "$starId#$variant") {
                h = (h xor ch.code.toLong()) * 0x01000193L and 0xFFFFFFFFL
            }
            val notes = List(5) { i -> SCALE[((h shr (i * 5)) and 0x1F).toInt() % SCALE.size] }
            val durations = List(5) { i -> if (((h shr (25 + i)) and 1L) == 1L) 380 else 240 }
            return StarChord(notes, durations)
        }

        /** Štvrťrok od roku 0 – rok * 4 + (mesiac-1) / 3. */
        fun quarter(
            year: Int,
            month: Int,
        ): Int = year * 4 + (month - 1) / 3

        fun canChange(
            lastChangedQuarter: Int?,
            nowQuarter: Int,
        ): Boolean = lastChangedQuarter == null || nowQuarter > lastChangedQuarter
    }
}

/** Vlastný zvuk hviezdy (bod 28): krátky a malý, aby bol jemný a nezaťažil telefón. */
object CustomSound {
    const val MAX_DURATION_MS = 15_000L
    const val MAX_BYTES = 2_000_000L

    /** @return null ak je zvuk v poriadku, inak dôvod po slovensky */
    fun problem(
        durationMs: Long?,
        bytes: Long,
    ): String? =
        when {
            durationMs == null || durationMs <= 0 -> "Toto nie je zvuk, ktorý sa dá prehrať."
            durationMs > MAX_DURATION_MS -> "Zvuk je dlhší ako 15 sekúnd – vyber kratší."
            bytes > MAX_BYTES -> "Súbor je väčší ako 2 MB – vyber menší."
            else -> null
        }
}
