package app.quantumsocial.core.i18n

/**
 * Preklad celej aplikácie (štýl gettext): kľúčom je slovenský text, slovník pre jazyk je
 * súbor `assets/i18n/<kód>.tsv` s riadkami `slovensky<TAB>preklad`. Chýbajúci preklad = slovenčina.
 * Premenné v texte sú `%s` a dosadzujú sa v poradí, preto ich v preklade musí byť rovnako veľa.
 */
object I18n {
    /** Jazyky, pre ktoré existuje slovník (+ slovenčina, ktorá ho nepotrebuje). */
    val available = listOf("sk", "en", "de", "es", "fr", "cs", "pl")

    @Volatile
    private var dict: Map<String, String> = emptyMap()

    @Volatile
    var lang: String = "sk"
        private set

    fun use(
        code: String,
        dictionary: Map<String, String>,
    ) {
        lang = code
        dict = dictionary
    }

    fun tr(
        sk: String,
        vararg args: Any?,
    ): String {
        val text = dict[sk] ?: sk
        if (args.isEmpty()) return text
        // vlastné dosadenie: každé %s v poradí; samotné % (napr. „50 %“) ostane
        val parts = text.split("%s")
        return buildString {
            parts.forEachIndexed { i, part ->
                append(part)
                if (i < parts.lastIndex) append(args.getOrNull(i) ?: "")
            }
        }
    }

    /** Jazyk telefónu, ak preň máme slovník; inak angličtina (pre celý svet). */
    fun pick(
        chosen: String?,
        phone: String,
    ): String = chosen?.takeIf { it in available } ?: phone.lowercase().takeIf { it in available } ?: "en"

    fun parse(tsv: String): Map<String, String> =
        tsv.lineSequence()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line ->
                val i = line.indexOf('\t')
                if (i <= 0) null else line.substring(0, i) to line.substring(i + 1).replace("\\n", "\n")
            }
            .toMap()

    fun placeholders(s: String): Int = s.split("%s").size - 1
}

fun tr(
    sk: String,
    vararg args: Any?,
): String = I18n.tr(sk, *args)
