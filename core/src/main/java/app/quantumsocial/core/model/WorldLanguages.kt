package app.quantumsocial.core.model

/** Jazyky celého sveta (Quantum Social je pre celú planétu). Kód je skratka, ktorú používa mapa a filtre. */
data class WorldLanguage(
    val code: String,
    val nativeName: String,
    val flag: String,
)

object WorldLanguages {
    val all: List<WorldLanguage> =
        listOf(
            WorldLanguage("SK", "Slovenčina", "🇸🇰"),
            WorldLanguage("CZ", "Čeština", "🇨🇿"),
            WorldLanguage("EN", "English", "🇬🇧"),
            WorldLanguage("DE", "Deutsch", "🇩🇪"),
            WorldLanguage("FR", "Français", "🇫🇷"),
            WorldLanguage("ES", "Español", "🇪🇸"),
            WorldLanguage("PT", "Português", "🇵🇹"),
            WorldLanguage("IT", "Italiano", "🇮🇹"),
            WorldLanguage("PL", "Polski", "🇵🇱"),
            WorldLanguage("HU", "Magyar", "🇭🇺"),
            WorldLanguage("UK", "Українська", "🇺🇦"),
            WorldLanguage("RU", "Русский", "🇷🇺"),
            WorldLanguage("RO", "Română", "🇷🇴"),
            WorldLanguage("BG", "Български", "🇧🇬"),
            WorldLanguage("HR", "Hrvatski", "🇭🇷"),
            WorldLanguage("SR", "Српски", "🇷🇸"),
            WorldLanguage("SL", "Slovenščina", "🇸🇮"),
            WorldLanguage("NL", "Nederlands", "🇳🇱"),
            WorldLanguage("SV", "Svenska", "🇸🇪"),
            WorldLanguage("NO", "Norsk", "🇳🇴"),
            WorldLanguage("DA", "Dansk", "🇩🇰"),
            WorldLanguage("FI", "Suomi", "🇫🇮"),
            WorldLanguage("ET", "Eesti", "🇪🇪"),
            WorldLanguage("LV", "Latviešu", "🇱🇻"),
            WorldLanguage("LT", "Lietuvių", "🇱🇹"),
            WorldLanguage("EL", "Ελληνικά", "🇬🇷"),
            WorldLanguage("TR", "Türkçe", "🇹🇷"),
            WorldLanguage("AR", "العربية", "🇸🇦"),
            WorldLanguage("HE", "עברית", "🇮🇱"),
            WorldLanguage("FA", "فارسی", "🇮🇷"),
            WorldLanguage("HI", "हिन्दी", "🇮🇳"),
            WorldLanguage("BN", "বাংলা", "🇧🇩"),
            WorldLanguage("UR", "اردو", "🇵🇰"),
            WorldLanguage("ZH", "中文", "🇨🇳"),
            WorldLanguage("JA", "日本語", "🇯🇵"),
            WorldLanguage("KO", "한국어", "🇰🇷"),
            WorldLanguage("VI", "Tiếng Việt", "🇻🇳"),
            WorldLanguage("TH", "ไทย", "🇹🇭"),
            WorldLanguage("ID", "Bahasa Indonesia", "🇮🇩"),
            WorldLanguage("MS", "Bahasa Melayu", "🇲🇾"),
            WorldLanguage("TL", "Filipino", "🇵🇭"),
            WorldLanguage("SW", "Kiswahili", "🇰🇪"),
            WorldLanguage("AM", "አማርኛ", "🇪🇹"),
            WorldLanguage("HA", "Hausa", "🇳🇬"),
            WorldLanguage("YO", "Yorùbá", "🇳🇬"),
            WorldLanguage("ZU", "isiZulu", "🇿🇦"),
            WorldLanguage("AF", "Afrikaans", "🇿🇦"),
            WorldLanguage("GA", "Gaeilge", "🇮🇪"),
            WorldLanguage("IS", "Íslenska", "🇮🇸"),
            WorldLanguage("KA", "ქართული", "🇬🇪"),
            WorldLanguage("HY", "Հայերեն", "🇦🇲"),
            WorldLanguage("KK", "Қазақ тілі", "🇰🇿"),
            WorldLanguage("UZ", "Oʻzbekcha", "🇺🇿"),
            WorldLanguage("NE", "नेपाली", "🇳🇵"),
            WorldLanguage("TA", "தமிழ்", "🇱🇰"),
            WorldLanguage("QU", "Runa Simi", "🇵🇪"),
        )

    val codes: List<String> get() = all.map { it.code }

    fun of(code: String?): WorldLanguage? = all.firstOrNull { it.code == code }

    /** Krátky popis na čip: „🇸🇰 SK“ (neznámy kód ostane ako je). */
    fun chip(code: String): String = of(code)?.let { "${it.flag} ${it.code}" } ?: code

    /** Vyhľadanie podľa kódu alebo názvu (bez ohľadu na veľké písmená). */
    fun search(query: String): List<WorldLanguage> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return all
        return all.filter { it.code.lowercase().startsWith(q) || it.nativeName.lowercase().contains(q) }
    }
}
