package app.quantumsocial.core.model

/**
 * Bezpečnosť medzi hviezdami (bod 19, lokálna časť): nahlásiť a zablokovať hviezdu.
 * Zablokovaná hviezda zmizne z mapy, WishNet, svetelného mosta aj súhvezdí – len pre mňa.
 * Nahlásenia sa zatiaľ len zbierajú; server ich neskôr pošle na moderáciu.
 */
enum class ReportReason(
    val label: String,
) {
    Spam("Spam alebo reklama"),
    Hate("Nenávisť alebo urážky"),
    Harassment("Obťažovanie"),
    Sexual("Nevhodný obsah"),
    SelfHarm("Niekto môže byť v ohrození"),
    Other("Niečo iné"),
}

data class Report(
    val starId: String,
    val reason: ReportReason,
    val day: Long,
)

data class Moderation(
    val blocked: Set<String> = emptySet(),
    val reports: List<Report> = emptyList(),
) {
    fun isBlocked(starId: String): Boolean = starId in blocked

    fun block(starId: String): Moderation = copy(blocked = blocked + starId)

    fun unblock(starId: String): Moderation = copy(blocked = blocked - starId)

    /** Nahlásenie: najviac [MAX_REPORTS_PER_DAY] denne, každú hviezdu raz za deň; nahlásená sa aj zablokuje. */
    fun report(
        starId: String,
        reason: ReportReason,
        today: Long,
    ): Moderation? {
        val todays = reports.filter { it.day == today }
        if (todays.size >= MAX_REPORTS_PER_DAY || todays.any { it.starId == starId }) return null
        return copy(blocked = blocked + starId, reports = reports + Report(starId, reason, today))
    }

    /** Odfiltruje zablokované hviezdy zo zoznamu čohokoľvek. */
    fun <T> visible(
        items: List<T>,
        starIdOf: (T) -> String,
    ): List<T> = items.filter { starIdOf(it) !in blocked }

    fun encode(): String =
        (blocked.map { "B\t$it" } + reports.map { "R\t${it.starId}\t${it.reason.name}\t${it.day}" }).joinToString("\n")

    companion object {
        const val MAX_REPORTS_PER_DAY = 10

        fun decode(stored: String?): Moderation {
            val lines = stored.orEmpty().lines().map { it.split("\t") }
            val blocked = lines.filter { it.size == 2 && it[0] == "B" }.map { it[1] }.toSet()
            val reports =
                lines.filter { it.size == 4 && it[0] == "R" }.mapNotNull { p ->
                    val reason = ReportReason.values().firstOrNull { it.name == p[2] } ?: return@mapNotNull null
                    Report(p[1], reason, p[3].toLongOrNull() ?: return@mapNotNull null)
                }
            return Moderation(blocked, reports)
        }
    }
}
