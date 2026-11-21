package app.quantumsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.PerformanceChoice
import app.quantumsocial.ui.perf.PerformanceSettings

@Composable
fun ProfileScreen(onRebirth: () -> Unit) {
    val context = LocalContext.current
    val choice by PerformanceSettings.choice.collectAsState()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Môj profil", style = MaterialTheme.typography.titleLarge)

        // bod 6: ručné prepnutie šetriča výkonu (Auto = podľa šetriča batérie v Androide)
        Text("Animácie galaxie", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PerformanceChoice.values().forEach { option ->
                FilterChip(
                    selected = option == choice,
                    onClick = { PerformanceSettings.set(context, option) },
                    label = { Text(option.label()) },
                )
            }
        }

        // bod 18: na testovanie – zmaže len príznak, obnovovací kľúč ostáva
        OutlinedButton(onClick = onRebirth) { Text("Znova prejsť zrodením") }
    }
}

private fun PerformanceChoice.label() =
    when (this) {
        PerformanceChoice.Auto -> "Auto"
        PerformanceChoice.Full -> "Plné"
        PerformanceChoice.Saver -> "Šetrič"
    }
