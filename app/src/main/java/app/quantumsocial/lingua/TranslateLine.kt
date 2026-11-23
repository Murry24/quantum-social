package app.quantumsocial.lingua

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import app.quantumsocial.core.model.LinguaBridges
import app.quantumsocial.core.model.WorldLanguages
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import kotlinx.coroutines.launch

/** Pod cudzojazyčným textom ponúkne „🌐 Preložiť“ do jazyka, ktorým žiarim. */
@Composable
fun TranslateLine(text: String) {
    val context = LocalContext.current
    val myCode = remember { OnboardingPrefs.language(context) }
    var fromIso by remember(text) { mutableStateOf<String?>(null) }
    var translated by remember(text) { mutableStateOf<String?>(null) }
    var busy by remember(text) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(text) { fromIso = LinguaTranslator.detect(text) }
    if (!LinguaBridges.shouldOffer(fromIso, myCode)) return

    Column {
        val done = translated
        when {
            done != null -> Text("🌐 $done", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
            busy -> Text("🌐 prekladám…", style = MaterialTheme.typography.bodySmall)
            else ->
                TextButton(
                    onClick = {
                        busy = true
                        scope.launch {
                            translated = LinguaTranslator.translate(text, fromIso!!, LinguaBridges.toIso(myCode))
                                ?: "preklad sa nepodaril – skús to s internetom"
                            busy = false
                        }
                    },
                ) { Text("🌐 Preložiť do: ${WorldLanguages.chip(myCode)}") }
        }
    }
}
