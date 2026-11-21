package app.quantumsocial.core.model

/**
 * LightSignature (bod 21): jedinečný svetelný podpis hviezdy odvodený z jej id –
 * 3 farby (emócie), počet prstencov a rytmus pulzovania. Rovnaké id = rovnaký podpis.
 */
data class LightSignature(
    val colors: List<Emotion>,
    val rings: Int,
    // dĺžky úderov v ms – rytmus, ktorým podpis pulzuje
    val rhythmMs: List<Int>,
) {
    companion object {
        fun of(starId: String): LightSignature {
            // vlastný jednoduchý hash (FNV-1a), aby bol rovnaký na každom zariadení
            var h = 0x811C9DC5L
            for (ch in starId) {
                h = (h xor ch.code.toLong()) * 0x01000193L and 0xFFFFFFFFL
            }
            val emotions = Emotion.values()
            fun pick(shift: Int) = ((h shr shift) and 0xFF).toInt()
            return LightSignature(
                colors = listOf(pick(0), pick(8), pick(16)).map { emotions[it % emotions.size] },
                rings = 2 + pick(24) % 3,
                rhythmMs = List(4) { i -> 300 + ((h shr (i * 7)) and 0x7).toInt() * 100 },
            )
        }
    }
}
