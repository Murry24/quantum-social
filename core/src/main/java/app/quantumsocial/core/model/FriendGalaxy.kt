package app.quantumsocial.core.model

import kotlin.random.Random

/**
 * Galaxia priateľov (bod 31) – malá vlastná galaxia, do ktorej sa vstupuje čiernou dierou.
 * [members] sú v poradí vstupu (prvý = najstarší), jadro [coreStarId] je zakladateľ.
 */
data class FriendGalaxy(
    val id: String,
    val name: String,
    // farba / nálada galaxie
    val emotion: Emotion,
    val coreStarId: String,
    val members: Set<String>,
    val createdAt: Long,
    val lastActivityAt: Long,
    // čierna diera otvorená = dajú sa vytvárať pozvánky a vstupovať
    val open: Boolean = true,
    // krátke motto pod menom, píše ho jadro
    val motto: String = "",
) {
    fun brightness(now: Long): Float = GalaxyRules.brightness(lastActivityAt, now)

    fun isDark(now: Long): Boolean = brightness(now) <= 0f
}

/** Čierna diera = pozvánka do galaxie. Platí [GalaxyRules.INVITE_TTL_MS] alebo do vyčerpania vstupov. */
data class BlackHoleInvite(
    val code: String,
    val galaxyId: String,
    val createdAt: Long,
    val maxUses: Int = 5,
    val usedCount: Int = 0,
) {
    fun isValid(now: Long): Boolean = now - createdAt < GalaxyRules.INVITE_TTL_MS && usedCount < maxUses
}

enum class EnterResult { Entered, Expired, Full, AlreadyMember, GalaxyClosed, TooManyGalaxies, Unknown }

object GalaxyRules {
    const val MAX_MEMBERS = 12
    const val MAX_GALAXIES_PER_STAR = 5
    const val MAX_OWNED = 2
    const val MAX_NAME = 24
    const val MAX_MOTTO = 60
    const val INVITE_TTL_MS = 24 * 60 * 60 * 1000L

    private const val DAY_MS = 24 * 60 * 60 * 1000L

    /** Plný jas, ak bola aktivita za posledné 3 dni. */
    const val BRIGHT_MS = 3 * DAY_MS

    /** Po 7 dňoch bez aktivity galaxia zhasne. */
    const val DARK_MS = 7 * DAY_MS

    // bez zameniteľných znakov 0/O, 1/I/L
    const val CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    const val CODE_LENGTH = 6

    /** Rovnaký štýl ako [SignalLifecycle.brightness]: najprv plné svetlo, potom lineárne blednutie. */
    fun brightness(
        lastActivityAt: Long,
        now: Long,
    ): Float {
        val idle = now - lastActivityAt
        if (idle <= BRIGHT_MS) return 1f
        return (1f - (idle - BRIGHT_MS).toFloat() / (DARK_MS - BRIGHT_MS)).coerceIn(0f, 1f)
    }

    fun newCode(random: Random = Random.Default): String =
        String(CharArray(CODE_LENGTH) { CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)] })

    /** Orezané meno, alebo null ak je prázdne. Dlhšie ako [MAX_NAME] sa skráti. */
    fun cleanName(name: String): String? = name.trim().take(MAX_NAME).trim().ifEmpty { null }

    fun cleanMotto(motto: String): String = motto.replace('\n', ' ').trim().take(MAX_MOTTO).trim()
}

/** Táborák galaxie: každý deň iná spoločná otázka, rovnaká pre všetkých členov. */
object GalaxyCampfire {
    val questions =
        listOf(
            "Čo ťa dnes potešilo?",
            "Za čo si dnes vďačný?",
            "Aká pesnička ti dnes hrá v hlave?",
            "Čo by si dnes rád zažil?",
            "Ktorá chvíľa dňa bola najkrajšia?",
            "Čo ťa dnes prekvapilo?",
            "Čím by si dnes niekomu spravil radosť?",
            "Aké miesto by si chcel navštíviť?",
            "Čo ťa naposledy rozosmialo?",
            "Na čo sa tešíš tento týždeň?",
            "Akú farbu má dnes tvoja nálada?",
            "Čo si sa nedávno naučil?",
            "Komu by si dnes poslal svetlo?",
            "Aký malý sen máš?",
        )

    fun questionFor(
        galaxyId: String,
        day: Long,
    ): String = questions[Math.floorMod(galaxyId.hashCode() + day.toInt(), questions.size)]
}
