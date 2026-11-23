package app.quantumsocial.core.model

/**
 * Lingua Bridges (bod 25): preklad textov medzi jazykmi hviezd priamo v telefóne.
 * Tu je len prevod našich kódov na ISO kódy prekladača a zoznam, čo vie preložiť.
 */
object LinguaBridges {
    // jazyky, ktoré vie prekladač v telefóne (ML Kit) – ISO 639-1
    private val supportedIso =
        setOf(
            "af", "ar", "be", "bg", "bn", "ca", "cs", "cy", "da", "de", "el", "en", "eo", "es", "et", "fa",
            "fi", "fr", "ga", "gl", "gu", "he", "hi", "hr", "ht", "hu", "id", "is", "it", "ja", "ka", "kn",
            "ko", "lt", "lv", "mk", "mr", "ms", "mt", "nl", "no", "pl", "pt", "ro", "ru", "sk", "sl", "sq",
            "sv", "sw", "ta", "te", "th", "tl", "tr", "uk", "ur", "vi", "zh",
        )

    /** Náš kód jazyka → ISO kód (CZ je čeština = „cs“, ostatné sú rovnaké, len malými písmenami). */
    fun toIso(code: String): String = if (code.equals("CZ", ignoreCase = true)) "cs" else code.lowercase()

    fun canTranslate(code: String): Boolean = toIso(code) in supportedIso

    /** Má zmysel prekladať? Len keď sa jazyky líšia a oba vie prekladač. */
    fun shouldOffer(
        fromIso: String?,
        myCode: String,
    ): Boolean = fromIso != null && fromIso != toIso(myCode) && fromIso in supportedIso && canTranslate(myCode)
}
