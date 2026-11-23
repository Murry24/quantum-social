package app.quantumsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.data.ModerationStore
import app.quantumsocial.core.model.QuantName
import app.quantumsocial.core.model.ReportReason

/** Bod 19: nahlásiť alebo zablokovať hviezdu (zatiaľ len v telefóne). */
@Composable
fun ModerationRow(
    starId: String,
    onBlocked: () -> Unit,
) {
    var reporting by remember { mutableStateOf(false) }
    var reported by remember { mutableStateOf<ReportReason?>(null) }
    var tooMany by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = { reporting = true }) { Text("⚑ Nahlásiť") }
        TextButton(
            onClick = {
                ModerationStore.block(starId)
                onBlocked()
            },
        ) { Text("🚫 Zablokovať") }
    }

    if (reporting) {
        AlertDialog(
            onDismissRequest = { reporting = false },
            title = { Text("Prečo hlásiš túto hviezdu?") },
            text = {
                Column {
                    ReportReason.values().forEach { reason ->
                        TextButton(
                            onClick = {
                                reporting = false
                                if (ModerationStore.report(starId, reason)) reported = reason else tooMany = true
                            },
                        ) { Text(reason.label) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { reporting = false }) { Text("Zrušiť") } },
        )
    }

    reported?.let { reason ->
        AlertDialog(
            onDismissRequest = {
                reported = null
                onBlocked()
            },
            title = { Text("Ďakujeme ✦") },
            text = {
                Text(
                    if (reason == ReportReason.SelfHarm) {
                        "Ak je niekto v ohrození, volaj 112. Hviezda je skrytá a viac ju neuvidíš."
                    } else {
                        "Hviezda je skrytá a viac ju neuvidíš. Odblokovať ju môžeš v Profile."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reported = null
                        onBlocked()
                    },
                ) { Text("OK") }
            },
        )
    }

    if (tooMany) {
        AlertDialog(
            onDismissRequest = { tooMany = false },
            text = { Text("Túto hviezdu si dnes už nahlásil, alebo si dnes nahlásil priveľa hviezd.") },
            confirmButton = { TextButton(onClick = { tooMany = false }) { Text("OK") } },
        )
    }
}

/** Do Profilu: zoznam zablokovaných hviezd s možnosťou odblokovať. */
@Composable
fun BlockedStarsList() {
    val moderation by ModerationStore.state.collectAsState()
    if (moderation.blocked.isEmpty()) return
    Text("Zablokované hviezdy", style = MaterialTheme.typography.titleMedium)
    moderation.blocked.sorted().forEach { id ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(QuantName.of(id), modifier = Modifier.padding(top = 12.dp))
            OutlinedButton(onClick = { ModerationStore.unblock(id) }) { Text("Odblokovať") }
        }
    }
}
