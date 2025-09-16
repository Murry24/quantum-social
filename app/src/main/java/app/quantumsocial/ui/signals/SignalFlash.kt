package app.quantumsocial.ui.signals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.FlashSignal
import kotlinx.coroutines.delay

@Composable
fun SignalFlash(onSubmit: (FlashSignal) -> Unit) {
    var intensity by remember { mutableFloatStateOf(0.6f) } // 0f..1f
    var playTick by remember { mutableIntStateOf(0) } // spúšť náhľadu

    val alpha by animateFloatAsState(
        targetValue = if (playTick > 0) intensity else 0f,
        label = "flash",
    )

    LaunchedEffect(playTick) {
        if (playTick > 0) {
            // krátky záblesk – po ~150 ms vypni
            delay(150)
            playTick = 0
        }
    }

    Column(
        modifier =
            Modifier
                .padding(16.dp),
    ) {
        Text(
            text = "Intenzita záblesku: ${(intensity * 100).toInt()}%",
        )

        Slider(
            value = intensity,
            onValueChange = { value ->
                intensity = value
            },
            valueRange = 0.1f..1f,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    playTick++
                },
            ) {
                Text(
                    text = "Náhľad",
                )
            }

            Spacer(
                modifier =
                    Modifier
                        .weight(1f),
            )

            Button(
                onClick = {
                    onSubmit(
                        FlashSignal(
                            intensity = intensity,
                        ),
                    )
                },
            ) {
                Text(
                    text = "Odoslať záblesk",
                )
            }
        }

        // náhľad záblesku – pod ovládacími prvkami, nič neprekrýva
        Box(
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .alpha(alpha)
                    .background(Color.White),
        )
    }
}
