package app.quantumsocial.core.model

/**
 * Kvanti-meno hviezdy (bod 5) – anonymné, poetické meno vygenerované z id hviezdy,
 * napr. „Tichá Iskra 7“. Rovnaké id dá vždy rovnaké meno (aj po reštarte aplikácie).
 */
object QuantName {
    // všetky podstatné mená sú ženského rodu, aby sedeli s prídavnými menami
    private val adjectives =
        listOf(
            "Tichá", "Jasná", "Snivá", "Hravá", "Teplá", "Modrá",
            "Zlatá", "Jemná", "Veselá", "Smelá", "Ružová", "Strieborná",
        )
    private val nouns =
        listOf(
            "Iskra", "Žiara", "Kométa", "Hmlovina", "Vlna", "Aurora",
            "Svetluška", "Planéta", "Dúha", "Hviezda", "Perla", "Ozvena",
        )

    const val MAX_NUMBER = 99

    fun of(starId: String): String {
        val h = StableHash.of(starId)
        val adjective = adjectives[(h % adjectives.size.toULong()).toInt()]
        val noun = nouns[((h / 13u) % nouns.size.toULong()).toInt()]
        val number = ((h / 997u) % MAX_NUMBER.toULong()).toInt() + 1
        return "$adjective $noun $number"
    }
}

/** Stabilný hash (FNV-1a 64-bit) – nezávisí od verzie JVM ani od náhody. */
internal object StableHash {
    fun of(text: String): ULong {
        var h = 0xcbf29ce484222325uL
        for (b in text.encodeToByteArray()) {
            h = h xor b.toUByte().toULong()
            h *= 0x100000001b3uL
        }
        return h
    }
}
