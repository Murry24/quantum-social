package app.quantumsocial.ui.screens

import android.content.Context
import app.quantumsocial.core.data.ChallengeStore
import app.quantumsocial.core.data.GratitudeStore
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.SeedGarden
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Uloženie vďačnosti (bod 14) a splnenej výzvy dňa (bod 13). */
object DailyPrefs {
    private const val PREFS = "quantum_prefs"
    private const val KEY_GRATITUDE = "gratitude"
    private const val KEY_CHALLENGE = "challenge_done_day"
    private const val KEY_SEEDS = "future_seeds"

    suspend fun keepSaved(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        GratitudeStore.restore(GratitudeJournal.decode(prefs.getString(KEY_GRATITUDE, null)))
        ChallengeStore.restore(prefs.getLong(KEY_CHALLENGE, -1L).takeIf { it >= 0 })
        SeedStore.restore(SeedGarden.decode(prefs.getString(KEY_SEEDS, null)))
        coroutineScope {
            launch { GratitudeStore.journal.collect { prefs.edit().putString(KEY_GRATITUDE, it.encode()).apply() } }
            launch { SeedStore.garden.collect { prefs.edit().putString(KEY_SEEDS, it.encode()).apply() } }
            launch {
                ChallengeStore.doneDay.collect { d ->
                    prefs.edit().apply { if (d == null) remove(KEY_CHALLENGE) else putLong(KEY_CHALLENGE, d) }.apply()
                }
            }
        }
    }
}
