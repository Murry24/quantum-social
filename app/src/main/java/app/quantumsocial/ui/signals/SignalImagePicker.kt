package app.quantumsocial.ui.signals

import android.content.Context
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.ImageSignal
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Kópie obrázkov prianí v aplikácii – odkaz z galérie by po reštarte prestal platiť. */
object WishImages {
    private const val MAX_BYTES = 8L * 1024 * 1024

    fun dir(context: Context): File = File(context.filesDir, "wish_images").apply { mkdirs() }

    /** @return file:// odkaz na kópiu, alebo null ak sa nedá prečítať / je väčší ako 8 MB */
    fun copy(
        context: Context,
        uri: Uri,
    ): String? =
        runCatching {
            val file = File(dir(context), "${UUID.randomUUID()}.img")
            context.contentResolver.openInputStream(uri)!!.use { input ->
                file.outputStream().use { out ->
                    val copied = input.copyTo(out)
                    require(copied <= MAX_BYTES)
                }
            }
            Uri.fromFile(file).toString()
        }.getOrNull()

    /** Zmaže kópie, ktoré už žiadne prianie nepoužíva. */
    fun cleanup(
        context: Context,
        keep: Set<String>,
    ) {
        dir(context).listFiles()?.filter { Uri.fromFile(it).toString() !in keep }?.forEach { it.delete() }
    }
}

@Composable
fun SignalImagePicker(onSubmit: (ImageSignal) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var failed by remember { mutableStateOf(false) }
    val (uri, setUri) = remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { setUri(it) },
        )
    Column(Modifier.padding(16.dp)) {
        Button(onClick = {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text(tr("Vybrať obrázok")) }

        uri?.let {
            AsyncImage(
                model = it,
                contentDescription = tr("náhľad"),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
            )
            Button(
                onClick = {
                    scope.launch {
                        val copy = withContext(Dispatchers.IO) { WishImages.copy(context, it) }
                        failed = copy == null
                        if (copy != null) onSubmit(ImageSignal(imageUri = copy))
                    }
                },
                modifier = Modifier.padding(top = 12.dp),
            ) { Text(tr("Odoslať obrázok")) }
            if (failed) Text(tr("Obrázok sa nedá použiť (najviac 8 MB)."))
        }
    }
}
