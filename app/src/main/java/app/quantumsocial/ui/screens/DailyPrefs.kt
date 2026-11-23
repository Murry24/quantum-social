package app.quantumsocial.ui.screens

import android.content.Context
import app.quantumsocial.core.data.AuraStore
import app.quantumsocial.core.data.ChallengeStore
import app.quantumsocial.core.data.ConnectionStore
import app.quantumsocial.core.data.EchoStore
import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.data.GratitudeStore
import app.quantumsocial.core.data.ModerationStore
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.model.DailyAura
import app.quantumsocial.core.model.EchoDay
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.Moderation
import app.quantumsocial.core.model.SeedGarden
import app.quantumsocial.core.model.SoulLinks
import app.quantumsocial.core.model.StarDustState
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
