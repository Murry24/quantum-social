package app.quantumsocial.ui.onboarding

import android.content.Context
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.RecoveryKey
import app.quantumsocial.core.security.SealedText
import app.quantumsocial.security.KeyVault
import java.util.UUID

/** Uložené voľby zo „Zrodenia hviezdy“ (bod 18) – zatiaľ len lokálne v SharedPreferences. */
object OnboardingPrefs {
    private const val PREFS = "quantum_prefs"
    private const val KEY_DONE = "onboarding_done"
    private const val KEY_RECOVERY = "recovery_key"
    private const val KEY_STAR = "my_star_id"
    private const val VAULT = "quantum_vault"
    private const val KEY_SEALED = "recovery_sealed"

    fun isDone(context: Context) = prefs(context).getBoolean(KEY_DONE, false)

    /** Sprievodca prvými krokmi (po zrodení) sa ukáže raz; v Profile sa dá pustiť znova. */
    fun tourDone(context: Context) = prefs(context).getBoolean("tour_done", false)

    fun setTourDone(
        context: Context,
        done: Boolean,
    ) = prefs(context).edit().putBoolean("tour_done", done).apply()

    /** Jazyk, ktorým hviezda žiari (z onboardingu). */
    fun language(context: Context): String = prefs(context).getString("language", null) ?: "SK"

    /** Id mojej hviezdy (z onboardingu); ak ešte nie je uložené, vytvorí sa nové. */
    fun myStarId(context: Context): String {
        val p = prefs(context)
        return p.getString(KEY_STAR, null) ?: UUID.randomUUID().toString().also {
            p.edit().putString(KEY_STAR, it).apply()
        }
    }

    /**
     * Existujúci kľúč, alebo nový (vygeneruje a uloží). Nikdy ho nelogovať ani neposielať.
     * Bod 9: na disku je len zašifrovaný (Android Keystore) v samostatnom súbore, ktorý sa nezálohuje.
     */
    fun recoveryKey(context: Context): List<String> {
        val vault = vault(context)
        vault.getString(KEY_SEALED, null)
            ?.let(SealedText::decode)
            ?.let(KeyVault::open)
            ?.let { return it.split(" ") }

        // starší nešifrovaný kľúč (pred bodom 9) prenesieme do trezoru a pôvodný zmažeme
        val legacy = prefs(context).getString(KEY_RECOVERY, null)?.split(" ")
        // shortcut: ak sa trezor nedá otvoriť (iné zariadenie / zmazaný Keystore), vznikne nový kľúč;
        // so serverom (bod 19) treba namiesto toho obnovu cez kľúč
        val key = legacy ?: RecoveryKey.generate()
        vault.edit().putString(KEY_SEALED, KeyVault.seal(key.joinToString(" ")).encode()).apply()
        if (legacy != null) prefs(context).edit().remove(KEY_RECOVERY).apply()
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
        .putString(KEY_STAR, starId)
        .putBoolean("notifications", notifications)
        .putBoolean(KEY_DONE, true)
        .apply()

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun vault(context: Context) = context.applicationContext.getSharedPreferences(VAULT, Context.MODE_PRIVATE)
}
