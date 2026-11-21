package app.quantumsocial.core.model

import java.text.Normalizer

/**
 * DreamMirror (bod 12): zapíšeš sen a nájde sa „snový most“ – podobný sen inej hviezdy.
 * Sny sú citlivé: ostávajú len v pamäti telefónu, nikam sa neukladajú ani neposielajú.
 */
object DreamMirror {
    const val MAX_TEXT = 300

    // shortcut: ukážkové sny iných hviezd, kým nie je server (bod 19)
    val otherDreams: List<String> =
        listOf(
            "Lietal som nad mestom a strechy svietili ako hviezdy.",
            "Plávala som v teplom mori a ryby spievali.",
            "Bežal som chodbou bez konca a dvere sa stále vzďaľovali.",
            "Stretla som babku v záhrade plnej kvetov a smiali sme sa.",
            "Padal som z výšky, ale pristál som mäkko v snehu.",
            "Moje zuby sa drobili a ja som sa hanbila prehovoriť.",
            "Hral som futbal na mesiaci a lopta sa vznášala.",
            "Zmeškala som vlak a stanica bola úplne prázdna.",
            "Našiel som kľúč od domu, ktorý som nikdy nevidel.",
            "Tancovala som v daždi a voda bola fialová.",
            "Lietal som s vtákmi nad lesom a bolo ticho.",
            "Skúška v škole a nič som sa nenaučil.",
        )

    /** Slová bez diakritiky, malými písmenami, aspoň 4 znaky. */
    fun words(text: String): Set<String> =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 4 }
            .map { it.take(5) }
            .toSet()

    /** Podobnosť 0..1 (Jaccard nad koreňmi slov). */
    fun similarity(
        a: String,
        b: String,
    ): Float {
        val wa = words(a)
        val wb = words(b)
        if (wa.isEmpty() || wb.isEmpty()) return 0f
        return (wa intersect wb).size.toFloat() / (wa union wb).size
    }

    /** Najpodobnejší sen inej hviezdy, alebo null ak žiadny nemá nič spoločné. */
    fun bridge(dream: String): Pair<String, Float>? =
        otherDreams
            .map { it to similarity(dream, it) }
            .filter { it.second > 0f }
            .maxByOrNull { it.second }
}
