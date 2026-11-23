package app.quantumsocial.core.model

/**
 * Uloženie prianí z WishNetu do telefónu, aby prežili reštart (žijú 24 h).
 * Ukladá sa text, emoji, záblesk, zvuk a obrázok, ak je to kópia v aplikácii (file:); mix nie.
 */
object WishNetVault {
    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")

    private fun unesc(s: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                out.append(
                    when (s[i + 1]) {
                        't' -> '\t'
                        'n' -> '\n'
                        else -> s[i + 1]
                    },
                )
                i += 2
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }

    fun encode(signals: List<Signal>): String =
        signals.filter { it.category == SignalCategory.WishNet }.mapNotNull { s ->
            val head = "${s.id}\t${s.createdAt}"
            when (s) {
                is TextSignal -> "T\t$head\t${esc(s.text)}"
                is EmojiSignal -> "E\t$head\t${esc(s.emoji)}"
                is FlashSignal -> "F\t$head\t${s.intensity}\t${s.durationMs}"
                is AudioSignal -> "A\t$head\t${esc(s.filePath)}\t${s.durationMs}"
                // odkaz z galérie (content:) by po reštarte neplatil
                is ImageSignal -> if (s.imageUri.startsWith("file:")) "I\t$head\t${esc(s.imageUri)}" else null
                else -> null
            }
        }.joinToString("\n")

    /** Vráti len priania, ktoré ešte nevypršali. Pokazené riadky preskočí. */
    fun decode(
        stored: String?,
        now: Long,
    ): List<Signal> =
        stored.orEmpty().lineSequence().mapNotNull { line ->
            val p = line.split('\t')
            runCatching {
                val id = p[1]
                val at = p[2].toLong()
                when (p[0]) {
                    "T" -> TextSignal(unesc(p[3]), id = id, createdAt = at).asWishNet()
                    "E" -> EmojiSignal(unesc(p[3]), id = id, createdAt = at).asWishNet()
                    "F" -> FlashSignal(p[3].toFloat(), p[4].toInt(), id = id, createdAt = at).asWishNet()
                    "A" -> AudioSignal(unesc(p[3]), p[4].toInt(), id = id, createdAt = at).asWishNet()
                    "I" -> ImageSignal(unesc(p[3]), id = id, createdAt = at).asWishNet()
                    else -> null
                }
            }.getOrNull()
        }.filter { !it.isExpired(now) }.toList()
}
