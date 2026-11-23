package app.quantumsocial.lingua

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.i18n.I18n
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.WorldLanguages
import java.util.Locale

/** Jazyk aplikácie: buď vybraný v Profile, alebo jazyk telefónu (bez slovníka → angličtina). */
object AppLanguage {
    private const val PREFS = "quantum_prefs"
    private const val KEY = "ui_lang"

    /** null = podľa telefónu */
    fun chosen(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)

    fun load(context: Context) {
        val code = I18n.pick(chosen(context), Locale.getDefault().language)
        val dict =
            if (code == "sk") {
                emptyMap()
            } else {
                runCatching {
                    context.assets.open("i18n/$code.tsv").bufferedReader().use { I18n.parse(it.readText()) }
                }.getOrDefault(emptyMap())
            }
        I18n.use(code, dict)
    }

    fun choose(
        activity: Activity,
        code: String?,
    ) {
        activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, code).apply()
        load(activity)
        activity.recreate()
    }
}

/** Slovník sa načíta skôr než čokoľvek iné (aj pre pripomienky a tapetu). */
class QuantumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLanguage.load(this)
    }
}

/** Do Profilu: jazyk aplikácie (podľa telefónu alebo ručne). Zmena znova otvorí obrazovku. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppLanguagePicker() {
    val activity = LocalContext.current as? Activity ?: return
    val chosen = remember { AppLanguage.chosen(activity) }
    Text(tr("Jazyk aplikácie"), style = MaterialTheme.typography.titleMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(
            selected = chosen == null,
            onClick = { AppLanguage.choose(activity, null) },
            label = { Text(tr("Podľa telefónu")) },
        )
        I18n.available.forEach { code ->
            // ISO kód → kód zo zoznamu svetových jazykov (cs → CZ)
            val lang = WorldLanguages.of(if (code == "cs") "CZ" else code.uppercase())
            FilterChip(
                selected = chosen == code,
                onClick = { AppLanguage.choose(activity, code) },
                label = { Text(lang?.let { "${it.flag} ${it.nativeName}" } ?: code) },
            )
        }
    }
}
