package app.quantumsocial.ui.onboarding

import android.content.Context
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.RecoveryKey

/** Uložené voľby zo „Zrodenia hviezdy“ (bod 18) – zatiaľ len lokálne v SharedPreferences. */
object OnboardingPrefs {
    private const val PREFS = "quantum_prefs"
    private const val KEY_DONE = "onboarding_done"
    private const val KEY_RECOVERY = "recovery_key"

    fun isDone(context: Context) = prefs(context).getBoolean(KEY_DONE, false)

    /** Existujúci kľúč, alebo nový (vygeneruje a uloží). Nikdy ho nelogovať ani neposielať. */
    fun recoveryKey(context: Context): List<String> {
        val p = prefs(context)
        p.getString(KEY_RECOVERY, null)?.let { return it.split(" ") }
        val key = RecoveryKey.generate()
        // TODO bod 9: Android Keystore – SharedPreferences nie sú bezpečné úložisko kľúča
        p.edit().putString(KEY_RECOVERY, key.joinToString(" ")).apply()
        return key
    }

    fun finish(
        context: Context,
        language: String,
        emotion: Emotion,
        starId: String,
        notifications: Boolean,
    ) = prefs(context).edit()
        .putString("language", language)
        .putString("first_emotion", emotion.name)
        .putString("my_star_id", starId)
        .putBoolean("notifications", notifications)
        .putBoolean(KEY_DONE, true)
        .apply()

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
