package app.quantumsocial.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val DRAG_PX = 6f

/**
 * Skrytá horná lišta mapy: zatvorená = len malé uško hore v strede.
 * Ťuk alebo potiahnutie dole ju otvorí; potiahnutie hore, šípka ⌃ alebo ťuk mimo ju zavrie.
 * [active] = niektorý filter je zapnutý → na ušku svieti bodka.
 */
@Composable
fun TopDrawer(
    open: Boolean,
    active: Boolean,
    onOpenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val drag =
        rememberDraggableState { delta ->
            if (delta > DRAG_PX) onOpenChange(true)
            if (delta < -DRAG_PX) onOpenChange(false)
        }

    Box(modifier = modifier.fillMaxSize()) {
        if (open) {
            // ťuk mimo panela ho zavrie
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onOpenChange(false)
                    },
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(Color(0xCC24306B))
                        .draggable(drag, Orientation.Vertical)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                content()
                Text(
                    text = "⌃",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier =
                        Modifier
                            .clickable { onOpenChange(false) }
                            .padding(horizontal = 24.dp, vertical = 2.dp)
                            .semantics { contentDescription = "Zavrieť filtre" },
                )
            }
        }

        if (!open) {
            // uško: veľká dotyková plocha, malý viditeľný tvar
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .size(width = 96.dp, height = 40.dp)
                        .draggable(drag, Orientation.Vertical)
                        .clickable { onOpenChange(true) }
                        .semantics { contentDescription = "Otvoriť filtre" },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(width = 36.dp, height = 5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.7f)),
                    )
                    if (active) {
                        Box(
                            Modifier
                                .padding(start = 6.dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE08A)),
                        )
                    }
                }
            }
        }
    }
}
