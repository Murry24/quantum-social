package app.quantumsocial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.ui.screens.color // from EmotionUi.kt

@Composable
fun LegendEmotions(
    selected: List<Emotion>,
    onToggle: (Emotion, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x66000000))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(Emotion.values()) { e ->
            val isSel = selected.contains(e)
            FilterChip(
                selected = isSel,
                onClick = { onToggle(e, !isSel) },
                label = { Text(e.name) },
                leadingIcon = {
                    androidx.compose.foundation.layout.Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .background(e.color(), CircleShape),
                    )
                },
                modifier = Modifier.padding(horizontal = 2.dp),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = e.color().copy(alpha = 0.25f),
                    ),
            )
        }
    }
}
