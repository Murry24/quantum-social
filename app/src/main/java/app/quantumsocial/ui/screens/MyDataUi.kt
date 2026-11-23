package app.quantumsocial.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.MainActivity
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.notify.GentleReminders
import org.json.JSONObject
import kotlin.system.exitProcess

/** Moje dáta: všetko o mne je len v telefóne – dá sa stiahnuť aj úplne zmazať. */
object MyData {
    private val prefsFiles = listOf("quantum_prefs", "quantum_vault")

    // tajné veci do súboru nedávame (kľúč obnovy je len pre teba, zapisuje sa pri zrodení)
    private val secretKeys = setOf("recovery_key", "recovery_sealed")

    fun exportJson(context: Context): String {
        val root = JSONObject()
        val settings = JSONObject()
        val p = context.applicationContext.getSharedPreferences("quantum_prefs", Context.MODE_PRIVATE)
        p.all.toSortedMap().forEach { (k, v) -> if (k !in secretKeys) settings.put(k, v.toString()) }
        root.put("app", "Quantum Social")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("data", settings)
        // spomienky sú v telefóne šifrované – do exportu idú čitateľne, sú tvoje
        root.put("memories", MemoryVault.load(context).encode())
        return root.toString(2)
    }

    fun write(
        context: Context,
        uri: Uri,
    ): Boolean =
        runCatching {
            context.contentResolver.openOutputStream(uri)!!.use { it.write(exportJson(context).toByteArray()) }
        }.isSuccess

    /** Zmaže nastavenia, šifrované veci, obrázky, ozveny a vlastný zvuk; zruší pripomienky a spustí appku nanovo. */
    fun deleteEverything(activity: Activity) {
        GentleReminders.setOn(activity, false)
        prefsFiles.forEach { activity.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
        activity.filesDir.listFiles()?.forEach { it.deleteRecursively() }
        activity.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        // čistý štart: v pamäti nesmie nič ostať
        val restart =
            Intent(activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(restart)
        activity.finishAffinity()
        exitProcess(0)
    }
}

/** Do Profilu: súkromie v kocke + stiahnuť / zmazať moje dáta. */
@Composable
fun MyDataCard() {
    val context = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val save =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                val ok = MyData.write(context, uri)
                result = if (ok) tr("Tvoje dáta sú uložené ✓") else tr("Uloženie sa nepodarilo.")
            }
        }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("🔒 Súkromie a moje dáta"), style = MaterialTheme.typography.titleMedium)
            listOf(
                "Si anonymný – žiadne meno, e-mail ani telefónne číslo.",
                "Všetko je zatiaľ uložené len v tvojom telefóne.",
                "Spomienky a kľúč obnovy sú zašifrované.",
                "Preklad správ beží priamo v telefóne.",
                "Kedykoľvek si môžeš stiahnuť alebo zmazať všetky svoje dáta.",
            ).forEach { Text(tr("· %s", tr(it)), style = MaterialTheme.typography.bodySmall) }
            OutlinedButton(onClick = { save.launch("quantum-social-moje-data.json") }) {
                Text(tr("📤 Stiahnuť moje dáta"))
            }
            OutlinedButton(onClick = { confirm = true }) { Text(tr("🗑 Zmazať všetko o mne")) }
            result?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(tr("Zmazať všetko o mne?")) },
            text = {
                Text(
                    tr(
                        "Zmizne tvoja hviezda, StarDust, priania, galaxie, semienka, spomienky aj nastavenia. " +
                            "Vrátiť sa to nedá.",
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.let(MyData::deleteEverything) }) {
                    Text(tr("Zmazať navždy"))
                }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(tr("Zrušiť")) } },
        )
    }
}
