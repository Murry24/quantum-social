package app.quantumsocial.core.data

import app.quantumsocial.core.model.BlackHoleInvite
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.EnterResult
import app.quantumsocial.core.model.FriendGalaxy
import app.quantumsocial.core.model.GalaxyRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

/**
 * Galaxie priateľov (bod 31) – zatiaľ len v pamäti, ako [InMemorySignalRepository].
 * Zhasnuté galaxie (7 dní bez aktivity) sa pri každej zmene odstránia.
 */
class GalaxyStore {
    private val _galaxies = MutableStateFlow<List<FriendGalaxy>>(emptyList())
    val galaxies: StateFlow<List<FriendGalaxy>> = _galaxies.asStateFlow()

    private val invites = mutableMapOf<String, BlackHoleInvite>()

    // zvýši sa pri novej pozvánke (tá nemení zoznam galaxií), aby sa uložila
    private val _invitesRevision = MutableStateFlow(0)
    val invitesRevision: StateFlow<Int> = _invitesRevision.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    /** ⭐ obľúbená galaxia je v zozname prvá. */
    fun toggleFavorite(galaxyId: String) {
        val f = _favorites.value
        _favorites.value = if (galaxyId in f) f - galaxyId else f + galaxyId
    }

    /** Jadro môže zmeniť meno, motto a farbu. @return false pri prázdnom mene alebo ak nie je jadro */
    fun edit(
        galaxyId: String,
        byStarId: String,
        name: String,
        motto: String,
        emotion: Emotion,
    ): Boolean {
        val g = galaxy(galaxyId) ?: return false
        val clean = GalaxyRules.cleanName(name) ?: return false
        if (g.coreStarId != byStarId) return false
        update(g.copy(name = clean, motto = GalaxyRules.cleanMotto(motto), emotion = emotion))
        return true
    }

    fun galaxy(id: String): FriendGalaxy? = _galaxies.value.firstOrNull { it.id == id }

    /** Kam vedie čierna diera s týmto kódom (aby UI vedelo, kam po vstupe letieť). */
    fun galaxyIdForCode(code: String): String? = invites[code.trim().uppercase()]?.galaxyId

    /** Galaxie, v ktorých hviezda svieti (zhasnuté nie). */
    fun galaxiesOf(
        starId: String,
        now: Long = System.currentTimeMillis(),
    ): List<FriendGalaxy> = _galaxies.value.filter { starId in it.members && !it.isDark(now) }

    /** Červie diery: ostatné moje galaxie, najživšie prvé. */
    fun wormholesFrom(
        galaxyId: String,
        starId: String,
        now: Long = System.currentTimeMillis(),
    ): List<FriendGalaxy> =
        galaxiesOf(starId, now)
            .filter { it.id != galaxyId }
            .sortedByDescending { it.lastActivityAt }

    /** @return nová galaxia, alebo null (prázdne meno, limit 2 vlastných alebo 5 galaxií) */
    fun create(
        name: String,
        emotion: Emotion,
        coreStarId: String,
        now: Long = System.currentTimeMillis(),
    ): FriendGalaxy? {
        sweep(now)
        val clean = GalaxyRules.cleanName(name) ?: return null
        val mine = galaxiesOf(coreStarId, now)
        if (mine.size >= GalaxyRules.MAX_GALAXIES_PER_STAR) return null
        if (mine.count { it.coreStarId == coreStarId } >= GalaxyRules.MAX_OWNED) return null
        val g =
            FriendGalaxy(
                id = UUID.randomUUID().toString(),
                name = clean,
                emotion = emotion,
                coreStarId = coreStarId,
                members = setOf(coreStarId),
                createdAt = now,
                lastActivityAt = now,
            )
        _galaxies.value = _galaxies.value + g
        return g
    }

    /** Čiernu dieru vytvorí len jadro a len keď je galaxia otvorená. */
    fun createInvite(
        galaxyId: String,
        byStarId: String,
        now: Long = System.currentTimeMillis(),
        random: Random = Random.Default,
    ): BlackHoleInvite? {
        val g = galaxy(galaxyId) ?: return null
        if (g.coreStarId != byStarId || !g.open) return null
        var code = GalaxyRules.newCode(random)
        while (code in invites) code = GalaxyRules.newCode(random)
        return BlackHoleInvite(code = code, galaxyId = galaxyId, createdAt = now).also {
            invites[code] = it
            _invitesRevision.value++
        }
    }

    fun enter(
        code: String,
        starId: String,
        now: Long = System.currentTimeMillis(),
    ): EnterResult {
        sweep(now)
        val invite = invites[code.trim().uppercase()] ?: return EnterResult.Unknown
        val g = galaxy(invite.galaxyId) ?: return EnterResult.Unknown
        if (starId in g.members) return EnterResult.AlreadyMember
        if (!invite.isValid(now)) return EnterResult.Expired
        if (!g.open) return EnterResult.GalaxyClosed
        if (g.members.size >= GalaxyRules.MAX_MEMBERS) return EnterResult.Full
        if (galaxiesOf(starId, now).size >= GalaxyRules.MAX_GALAXIES_PER_STAR) return EnterResult.TooManyGalaxies
        invites[invite.code] = invite.copy(usedCount = invite.usedCount + 1)
        update(g.copy(members = g.members + starId))
        return EnterResult.Entered
    }

    /** Biela diera – potichu odísť. Odíde jadro → jadrom je najstarší člen; nikto neostal → galaxia zanikne. */
    fun leave(
        galaxyId: String,
        starId: String,
    ) {
        val g = galaxy(galaxyId) ?: return
        val rest = g.members - starId
        if (rest.isEmpty()) {
            remove(galaxyId)
            return
        }
        update(g.copy(members = rest, coreStarId = if (g.coreStarId == starId) rest.first() else g.coreStarId))
    }

    /** Jadro zatvorí / otvorí čiernu dieru. */
    fun setOpen(
        galaxyId: String,
        byStarId: String,
        open: Boolean,
    ) {
        val g = galaxy(galaxyId) ?: return
        if (g.coreStarId == byStarId) update(g.copy(open = open))
    }

    /** Jadro „vypľuje“ hviezdu z galaxie. */
    fun expel(
        galaxyId: String,
        byStarId: String,
        starId: String,
    ) {
        val g = galaxy(galaxyId) ?: return
        if (g.coreStarId == byStarId && starId != byStarId) update(g.copy(members = g.members - starId))
    }

    /** Signál v galaxii ju znova rozsvieti. */
    fun recordSignal(
        galaxyId: String,
        now: Long = System.currentTimeMillis(),
    ) {
        galaxy(galaxyId)?.let { update(it.copy(lastActivityAt = now)) }
    }

    /** Odstráni zhasnuté galaxie a ich pozvánky. */
    fun sweep(now: Long = System.currentTimeMillis()) {
        _galaxies.value.filter { it.isDark(now) }.forEach { remove(it.id) }
    }

    /** Pre testy a demo: vloží galaxiu a pozvánku s daným kódom. */
    fun put(
        galaxy: FriendGalaxy,
        invite: BlackHoleInvite? = null,
    ) {
        _galaxies.value = _galaxies.value.filter { it.id != galaxy.id } + galaxy
        invite?.let { invites[it.code] = it }
    }

    /** Uloženie do telefónu: riadky G (galaxia), I (pozvánka), F (obľúbená), oddelené tabulátorom. */
    fun encode(): String {
        fun clean(s: String) = s.replace('\t', ' ').replace('\n', ' ')
        val g =
            _galaxies.value.map {
                listOf(
                    "G", it.id, clean(it.name), it.emotion.name, it.coreStarId, it.members.joinToString(","),
                    it.createdAt, it.lastActivityAt, it.open, clean(it.motto),
                ).joinToString("\t")
            }
        val i =
            invites.values.map {
                listOf("I", it.code, it.galaxyId, it.createdAt, it.maxUses, it.usedCount).joinToString("\t")
            }
        val f = _favorites.value.map { "F\t$it" }
        return (g + i + f).joinToString("\n")
    }

    /** Načíta uložené galaxie (pokazené riadky preskočí). Ukážková galaxia ostane, ak ju uložené nenahradia. */
    fun restore(stored: String?) {
        if (stored.isNullOrBlank()) return
        val favorites = mutableSetOf<String>()
        stored.lineSequence().forEach { line ->
            val p = line.split('\t')
            runCatching {
                when (p[0]) {
                    "G" ->
                        put(
                            FriendGalaxy(
                                id = p[1],
                                name = p[2],
                                emotion = Emotion.valueOf(p[3]),
                                coreStarId = p[4],
                                members = p[5].split(',').filter { it.isNotEmpty() }.toSet(),
                                createdAt = p[6].toLong(),
                                lastActivityAt = p[7].toLong(),
                                open = p[8].toBoolean(),
                                motto = p.getOrElse(9) { "" },
                            ),
                        )
                    "I" -> invites[p[1]] = BlackHoleInvite(p[1], p[2], p[3].toLong(), p[4].toInt(), p[5].toInt())
                    "F" -> favorites += p[1]
                }
            }
        }
        _favorites.value = favorites
        sweep()
    }

    private fun update(g: FriendGalaxy) {
        _galaxies.value = _galaxies.value.map { if (it.id == g.id) g else it }
    }

    private fun remove(galaxyId: String) {
        _galaxies.value = _galaxies.value.filter { it.id != galaxyId }
        invites.values.removeAll { it.galaxyId == galaxyId }
    }

    companion object {
        const val DEMO_CODE = "KVANT7"

        /** Zdieľaný obchod pre aplikáciu s ukážkovou galaxiou „Kamaráti“ (5 vymyslených hviezd). */
        val shared: GalaxyStore by lazy { GalaxyStore().apply { seedDemo(System.currentTimeMillis()) } }

        fun GalaxyStore.seedDemo(now: Long) {
            val id = "demo-kamarati"
            put(
                FriendGalaxy(
                    id = id,
                    name = "Kamaráti",
                    emotion = Emotion.Joy,
                    coreStarId = "kamarat-1",
                    members = (1..5).map { "kamarat-$it" }.toSet(),
                    createdAt = now,
                    lastActivityAt = now,
                ),
                BlackHoleInvite(code = DEMO_CODE, galaxyId = id, createdAt = now),
            )
        }
    }
}
