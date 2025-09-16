package app.quantumsocial.ui.signals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.FlashSignal

@Composable
fun SignalFlash(onSubmit: (FlashSignal) -> Unit) {
    var intensity by remember { mutableFloatStateOf(0.6f) } // 0..1
    var play by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (play) intensity else 0f,
        label = "flash",
    )

    Box(Modifier.padding(16.dp)) {
        Text("Intenzita záblesku: ${(intensity * 100).toInt()}%")
        Slider(
            value = intensity,
            onValueChange = { intensity = it },
            valueRange = 0.1f..1f,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Button(onClick = { play = true }) { Text("Prehrať náhľad") }
        Button(
            onClick = { onSubmit(FlashSignal(intensity = intensity)) },
            modifier = Modifier.padding(top = 12.dp),
        ) { Text("Odoslať záblesk") }

        // náhľad (biely overlay)
        Box(
            modifier =
                Modifier
                    .padding(top = 60.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .alpha(alpha)
                    .background(Color.White),
        )
        if (play) {
            // rýchly reset
            play = false
        }
    }
}
