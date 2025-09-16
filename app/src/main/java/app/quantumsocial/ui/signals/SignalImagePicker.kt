package app.quantumsocial.ui.signals

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.ImageSignal
import coil.compose.AsyncImage

@Composable
fun SignalImagePicker(onSubmit: (ImageSignal) -> Unit) {
    val (uri, setUri) = remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { setUri(it) },
        )
    Column(Modifier.padding(16.dp)) {
        Button(onClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("Vybrať obrázok") }

        uri?.let {
            AsyncImage(
                model = it,
                contentDescription = "náhľad",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
            )
            Button(
                onClick = { onSubmit(ImageSignal(imageUri = it.toString())) },
                modifier = Modifier.padding(top = 12.dp),
            ) { Text("Odoslať obrázok") }
        }
    }
}
