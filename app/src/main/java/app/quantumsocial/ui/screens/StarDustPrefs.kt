package app.quantumsocial.ui.screens

import android.content.Context
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.StarDustState

/** Uloženie StarDust (bod 20) – načíta stav pri štarte a potom ukladá každú zmenu. */
object StarDustPrefs {
    private const val PREFS = "quantum_prefs"
    private const val KEY = "stardust"

    suspend fun keepSaved(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        StarDust.restore(StarDustState.decode(prefs.getString(KEY, null)))
        StarDust.state.collect { prefs.edit().putString(KEY, it.encode()).apply() }
    }
}
