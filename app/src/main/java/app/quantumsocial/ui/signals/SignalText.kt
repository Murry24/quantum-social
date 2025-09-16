package app.quantumsocial.ui.signals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.TextSignal

private const val MAX = 200

@Composable
fun SignalText(onSubmit: (TextSignal) -> Unit) {
    val (value, setValue) = remember { mutableStateOf(TextFieldValue("")) }
    val left = MAX - value.text.length
    val isValid = left >= 0 && value.text.isNotBlank()

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.text.length <= MAX) setValue(it)
            },
            label = { Text("Text (max $MAX znakov)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
        )
        Text(
            text = "$left zostáva",
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = { onSubmit(TextSignal(text = value.text.trim())) },
            enabled = isValid,
            modifier = Modifier.padding(top = 12.dp),
        ) { Text("Odoslať text") }
    }
}
