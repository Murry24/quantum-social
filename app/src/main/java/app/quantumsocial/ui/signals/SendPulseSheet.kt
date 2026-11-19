package app.quantumsocial.ui.signals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.ui.screens.color
import app.quantumsocial.ui.screens.label

/**
 * Obsah spodného panelu na odoslanie pocitového pulzu (bod 8).
 * Bez písania – vyberieš emóciu (farebné svetlo) a pošleš.
 */
@Composable
fun SendPulseSheet(
    target: SignalTarget,
    onSend: (Emotion) -> Unit,
    onOpenProfile: ((String) -> Unit)? = null,
) {
    var selected by remember { mutableStateOf(Emotion.Joy) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                when (target) {
                    is SignalTarget.Star -> "✦ Pošli pocit tejto hviezde"
                    SignalTarget.Chaos -> "🌌 Pošli pocit do chaosu"
                },
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
        if (target == SignalTarget.Chaos) {
            Text(
                text = "Zachytí ho náhodná hviezda",
                fontSize = 13.sp,
                modifier = Modifier.alpha(0.7f),
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Emotion.values().forEach { e ->
                EmotionOrb(
                    emotion = e,
                    selected = e == selected,
                    onClick = { selected = e },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSend(selected) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Vypustiť pulz ✦")
        }

        if (target is SignalTarget.Star && onOpenProfile != null) {
            TextButton(onClick = { onOpenProfile(target.starId) }) {
                Text("Profil hviezdy")
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EmotionOrb(
    emotion: Emotion,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = emotion.color()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier =
                Modifier
                    .size(if (selected) 52.dp else 44.dp)
                    .background(
                        brush = Brush.radialGradient(listOf(Color.White, c, c.copy(alpha = 0.2f))),
                        shape = CircleShape,
                    )
                    .then(
                        if (selected) {
                            Modifier.border(2.dp, Color.White, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
        )
        Text(
            text = emotion.label(),
            fontSize = 12.sp,
            modifier =
                Modifier
                    .padding(top = 4.dp)
                    .alpha(if (selected) 1f else 0.6f),
        )
    }
}
