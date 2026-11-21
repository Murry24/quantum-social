package app.quantumsocial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MiniKnobs(
    minIntensity: Float,
    onIntensityChange: (Float) -> Unit,
    maxDistance: Float,
    onDistanceChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x66000000))
                .padding(8.dp),
    ) {
        Column {
            Text(text = "Min. intenzita: ${(minIntensity * 100).toInt()}%", color = Color.White)
            Slider(
                value = minIntensity,
                onValueChange = onIntensityChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(text = "Max. vzdialenosť: ${(maxDistance * 100).toInt()}%", color = Color.White)
            Slider(
                value = maxDistance,
                onValueChange = onDistanceChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
