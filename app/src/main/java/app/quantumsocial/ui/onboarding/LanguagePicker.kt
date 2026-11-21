package app.quantumsocial.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.WorldLanguages

/** Výber jazyka z celého sveta – s vyhľadávaním podľa názvu alebo skratky. */
@Composable
fun LanguagePicker(
    selected: String,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        label = { Text("Hľadať jazyk…") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(260.dp).padding(top = 8.dp),
    ) {
        items(WorldLanguages.search(query), key = { it.code }) { l ->
            FilterChip(
                selected = l.code == selected,
                onClick = { onSelect(l.code) },
                label = { Text("${l.flag} ${l.nativeName}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.padding(4.dp),
            )
        }
    }
}
