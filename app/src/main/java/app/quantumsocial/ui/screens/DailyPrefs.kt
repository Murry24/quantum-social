package app.quantumsocial.ui.screens

import android.content.Context
import app.quantumsocial.core.data.AuraStore
import app.quantumsocial.core.data.ChallengeStore
import app.quantumsocial.core.data.ConnectionStore
import app.quantumsocial.core.data.EchoStore
import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.data.GratitudeStore
import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.ModerationStore
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.data.WishBoardStore
import app.quantumsocial.core.data.ZenStore
import app.quantumsocial.core.model.DailyAura
import app.quantumsocial.core.model.EchoDay
import app.quantumsocial.core.model.FutureSeed
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.Moderation
import app.quantumsocial.core.model.SeedGarden
import app.quantumsocial.core.model.SoulLinks
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.model.WishBoard
import app.quantumsocial.core.model.WishNetVault
import app.quantumsocial.core.model.ZenDays
import app.quantumsocial.ui.signals.WishImages
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

/** Uloženie vďačnosti (bod 14) a splnenej výzvy dňa (bod 13). */
object DailyPrefs {
    private const val PREFS = "quantum_prefs"
    private const val KEY_GRATITUDE = "gratitude"
    private const val KEY_CHALLENGE = "challenge_done_day"
    private const val KEY_SEEDS = "future_seeds"
    private const val KEY_ECHOES = "echoes"
    private const val KEY_LINKS = "soul_links"
    private const val KEY_JOINED = "constellations_joined"
    private const val KEY_MODERATION = "moderation"
    private const val KEY_AURA = "daily_aura"
    private const val KEY_GALAXIES = "friend_galaxies"
    private const val KEY_WISHES = "wishnet_wishes"
    private const val KEY_ZEN = "zen_days"
    private const val KEY_WISH_BOARD = "wishnet_board"

    /** Pre večernú pripomienku: prvé smädné semienko (číta priamo z telefónu, appka nemusí bežať). */
    fun thirstySeed(context: Context): FutureSeed? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = StarDustState.epochDay(System.currentTimeMillis())
        return SeedGarden.decode(prefs.getString(KEY_SEEDS, null)).growing.firstOrNull { it.isThirsty(today) }
    }

    suspend fun keepSaved(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        GratitudeStore.restore(GratitudeJournal.decode(prefs.getString(KEY_GRATITUDE, null)))
        ChallengeStore.restore(prefs.getLong(KEY_CHALLENGE, -1L).takeIf { it >= 0 })
        SeedStore.restore(SeedGarden.decode(prefs.getString(KEY_SEEDS, null)))
        // včerajšie ozveny zhasli – zmažeme aj ich súbory
        val echoes = EchoDay.decode(prefs.getString(KEY_ECHOES, null))
        val today = StarDustState.epochDay(System.currentTimeMillis())
        echoes.expired(today).forEach { File(it.filePath).delete() }
        EchoStore.restore(EchoDay(echoes.today(today)))
        ConnectionStore.restore(
            SoulLinks.decode(prefs.getString(KEY_LINKS, null)),
            prefs.getStringSet(KEY_JOINED, emptySet()).orEmpty().toSet(),
        )
        ConnectionStore.refresh()
        // priania z WishNetu žijú 24 h aj cez reštart
        val wishes = WishNetVault.decode(prefs.getString(KEY_WISHES, null), System.currentTimeMillis())
        InMemorySignalRepository.restore(wishes)
        WishImages.cleanup(context, wishes.filterIsInstance<ImageSignal>().map { it.imageUri }.toSet())
        val board = WishBoard.decode(prefs.getString(KEY_WISH_BOARD, null))
        WishBoardStore.restore(board.keepOnly(wishes.map { it.id }.toSet()))
        ZenStore.restore(ZenDays.decode(prefs.getString(KEY_ZEN, null)))
        val galaxies = GalaxyStore.shared
        galaxies.restore(prefs.getString(KEY_GALAXIES, null))
        AuraStore.restore(DailyAura.decode(prefs.getString(KEY_AURA, null)))
        ModerationStore.restore(Moderation.decode(prefs.getString(KEY_MODERATION, null)))
        coroutineScope {
            launch { GratitudeStore.journal.collect { prefs.edit().putString(KEY_GRATITUDE, it.encode()).apply() } }
            launch { SeedStore.garden.collect { prefs.edit().putString(KEY_SEEDS, it.encode()).apply() } }
            launch { EchoStore.day.collect { prefs.edit().putString(KEY_ECHOES, it.encode()).apply() } }
            launch { ConnectionStore.links.collect { prefs.edit().putString(KEY_LINKS, it.encode()).apply() } }
            launch {
                combine(galaxies.galaxies, galaxies.favorites, galaxies.invitesRevision) { _, _, _ ->
                    galaxies.encode()
                }.collect { prefs.edit().putString(KEY_GALAXIES, it).apply() }
            }
            launch {
                InMemorySignalRepository.signals.collect {
                    prefs.edit().putString(KEY_WISHES, WishNetVault.encode(it)).apply()
                }
            }
            launch { ZenStore.days.collect { prefs.edit().putString(KEY_ZEN, it.encode()).apply() } }
            launch { WishBoardStore.board.collect { prefs.edit().putString(KEY_WISH_BOARD, it.encode()).apply() } }
            launch {
                AuraStore.aura.collect { a ->
                    prefs.edit().apply { if (a == null) remove(KEY_AURA) else putString(KEY_AURA, a.encode()) }.apply()
                }
            }
            launch { ModerationStore.state.collect { prefs.edit().putString(KEY_MODERATION, it.encode()).apply() } }
            launch { ConnectionStore.joined.collect { prefs.edit().putStringSet(KEY_JOINED, it).apply() } }
            launch {
                ChallengeStore.doneDay.collect { d ->
                    prefs.edit().apply { if (d == null) remove(KEY_CHALLENGE) else putLong(KEY_CHALLENGE, d) }.apply()
                }
            }
        }
    }
}
