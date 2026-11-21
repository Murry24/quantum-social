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
        return BlackHoleInvite(code = code, galaxyId = galaxyId, createdAt = now).also { invites[code] = it }
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
