package app.quantumsocial.ui.signals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.WishBoard
import app.quantumsocial.core.model.WishTheme

/** Rad tém: [none] = „Všetko“ (filter) alebo „Bez témy“ (pri posielaní). */
@Composable
fun WishThemeRow(
    selected: WishTheme?,
    none: String,
    onSelect: (WishTheme?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text(none) }) }
        items(WishTheme.values().toList()) { t ->
            FilterChip(
                selected = selected == t,
                onClick = { onSelect(t) },
                label = { Text("${t.emoji} ${t.label}") },
            )
        }
    }
}

/** Pod prianím: téma, 🤞 držím palce, pri vlastnom 🌟 splnilo sa. */
@Composable
fun WishCardExtras(
    wishId: String,
    board: WishBoard,
    onCheer: () -> Unit,
    onFulfill: () -> Unit,
) {
    val theme = board.themes[wishId]
    val fulfilled = wishId in board.fulfilled
    if (fulfilled) Text("🌟 Splnilo sa!", color = Color(0xFFB8860B), fontWeight = FontWeight.Bold)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (theme != null) Text("${theme.emoji} ${theme.label}")
        val cheers = board.cheersFor(wishId)
        if (wishId in board.mine) {
            Text("🤞 $cheers")
            if (!fulfilled) TextButton(onClick = onFulfill) { Text("🌟 Splnilo sa") }
        } else {
            val mark = if (wishId in board.cheered) "✓" else ""
            TextButton(onClick = onCheer) { Text("🤞 Držím palce · $cheers $mark") }
        }
    }
}
