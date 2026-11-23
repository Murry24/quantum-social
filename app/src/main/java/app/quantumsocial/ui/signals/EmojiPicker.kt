package app.quantumsocial.ui.signals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.quantumsocial.core.data.RecentEmojis
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.EmojiCatalog

/** Veľký výber emoji v skupinách (WishNet → Emoji). Ťuk = emoji sa hneď použije. */
@Composable
fun EmojiGrid(
    modifier: Modifier = Modifier,
    onPick: (String) -> Unit,
) {
    val recent by RecentEmojis.recent.collectAsState()
    val groups =
        buildList {
            if (recent.isNotEmpty()) add(EmojiCatalog.Group(tr("Naposledy"), recent))
            addAll(EmojiCatalog.groups)
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier.fillMaxWidth().height(260.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        groups.forEach { g ->
            item(key = "title-${g.title}", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    tr(g.title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }
            items(g.emojis, key = { "${g.title}-$it" }) { e ->
                EmojiCell(emoji = e, selected = false) {
                    RecentEmojis.use(e)
                    onPick(e)
                }
            }
        }
    }
}

/** Kompaktný riadok emoji (doplnok k pulzu, odpoveď). Ťuk na vybrané emoji ho zruší. */
@Composable
fun EmojiStrip(
    selected: String?,
    modifier: Modifier = Modifier,
    onPick: (String?) -> Unit,
) {
    // poradie sa zafixuje pri otvorení – pri výbere sa riadok nepreskupí
    val emojis = remember { (RecentEmojis.recent.value + EmojiCatalog.all).distinct() }
    LazyRow(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 4.dp)) {
        items(emojis, key = { it }) { e ->
            EmojiCell(emoji = e, selected = e == selected) {
                if (e == selected) {
                    onPick(null)
                } else {
                    RecentEmojis.use(e)
                    onPick(e)
                }
            }
        }
    }
}

@Composable
private fun EmojiCell(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .padding(2.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable(onClick = onClick),
    ) {
        Text(emoji, fontSize = 22.sp)
    }
}
