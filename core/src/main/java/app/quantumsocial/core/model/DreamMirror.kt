package app.quantumsocial.core.model

import app.quantumsocial.core.i18n.tr
import java.text.Normalizer
import kotlin.random.Random

/**
 * DreamMirror (bod 12): zapíšeš sen a nájde sa „snový most“ – podobný sen inej hviezdy.
 * Sny sú citlivé: ostávajú len v pamäti telefónu, nikam sa neukladajú ani neposielajú.
 */
/** Symboly snov – vyberajú sa ťuknutím, takže fungujú v každom jazyku. Význam je len pre radosť. */
enum class DreamSymbol(
    val emoji: String,
    val label: String,
    val meaning: String,
) {
    Flying("🕊️", "Lietanie", "Túžba po slobode a ľahkosti."),
    Water("🌊", "Voda", "Emócie – pokojné alebo búrlivé ako more."),
    Teeth("🦷", "Zuby", "Obava, ako pôsobíš na druhých."),
    Falling("⬇️", "Pád", "Strata kontroly – alebo chuť sa pustiť."),
    Chase("🏃", "Útek", "Niečo, čomu sa vyhýbaš, chce tvoju pozornosť."),
    Exam("📝", "Skúška", "Pocit, že ťa niekto hodnotí."),
    Journey("🚆", "Cesta", "Zmena, ktorá je na ceste."),
    House("🏠", "Dom", "Ty sám – každá izba je kúsok teba."),
    Key("🔑", "Kľúč", "Otvára sa nová možnosť."),
    Animal("🐾", "Zviera", "Tvoje inštinkty a sila."),
    Person("👤", "Známy človek", "Vzťah, na ktorý práve myslíš."),
    Light("✨", "Svetlo", "Nádej a jasná myšlienka."),
}

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

    // symboly k ukážkovým snom (v rovnakom poradí ako otherDreams)
    private val dreamSymbols: List<Set<DreamSymbol>> =
        with(DreamSymbol) {
            listOf(
                setOf(Flying, House, Light), setOf(Water, Animal), setOf(Chase, House), setOf(Person, Light),
                setOf(Falling), setOf(Teeth, Person), setOf(Flying, Light), setOf(Journey),
                setOf(Key, House), setOf(Water, Light), setOf(Flying, Animal), setOf(Exam),
            )
        }

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

    fun symbolSimilarity(
        a: Set<DreamSymbol>,
        b: Set<DreamSymbol>,
    ): Float = if (a.isEmpty() || b.isEmpty()) 0f else (a intersect b).size.toFloat() / (a union b).size

    /** Najpodobnejší sen inej hviezdy (podľa slov alebo symbolov), alebo null ak žiadny nemá nič spoločné. */
    fun bridge(
        dream: String,
        symbols: Set<DreamSymbol> = emptySet(),
    ): Pair<String, Float>? =
        otherDreams
            .mapIndexed { i, d -> d to maxOf(similarity(dream, d), symbolSimilarity(symbols, dreamSymbols[i])) }
            .filter { it.second > 0f }
            .maxByOrNull { it.second }
            ?.let { tr(it.first) to it.second }

    /** O čom sa dnes v galaxii snívalo – 3 najčastejšie symboly (ukážkové hviezdy, kým nie je server). */
    fun galaxyToday(day: Long): List<Pair<DreamSymbol, Int>> {
        val rnd = Random(day * 7919)
        return DreamSymbol.values().map { it to rnd.nextInt(3, 40) }.sortedByDescending { it.second }.take(3)
    }
}
