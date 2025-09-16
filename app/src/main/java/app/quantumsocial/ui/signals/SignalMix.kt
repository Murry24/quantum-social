package app.quantumsocial.ui.signals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.Signal

@Composable
fun SignalMix(
    available: List<Signal>,
    onSubmit: (MixSignal) -> Unit,
) {
    val picked = remember { mutableStateListOf<String>() }
    Column(Modifier.padding(16.dp)) {
        Text("Vyber signály na mix:")
        available.take(10).forEach { s ->
            Row {
                Checkbox(
                    checked = picked.contains(s.id),
                    onCheckedChange = { checked ->
                        if (checked) picked.add(s.id) else picked.remove(s.id)
                    },
                )
                Text(text = s::class.simpleName ?: "Signal")
            }
        }
        Button(
            onClick = {
                val selected = available.filter { picked.contains(it.id) }
                if (selected.isNotEmpty()) onSubmit(MixSignal(parts = selected))
            },
            modifier = Modifier.padding(top = 12.dp),
        ) { Text("Odoslať Mix") }
    }
}
