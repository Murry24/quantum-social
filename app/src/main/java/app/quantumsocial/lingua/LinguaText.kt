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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.LinguaBridges
import app.quantumsocial.core.model.WorldLanguages
import app.quantumsocial.ui.onboarding.OnboardingPrefs
import kotlinx.coroutines.launch

/**
 * Bod 25: správa z cudziny prichádza rovno preložená do jazyka, ktorým žiarim (v telefóne, zadarmo).
 * Originál je o ťuk ďalej. Bez Wi-Fi a bez stiahnutého balíka ponúkne tlačidlo „Preložiť“.
 */
@Composable
fun LinguaText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val myCode = remember { OnboardingPrefs.language(context) }
    var fromIso by remember(text) { mutableStateOf<String?>(null) }
    var translated by remember(text) { mutableStateOf<String?>(null) }
    var showOriginal by remember(text) { mutableStateOf(false) }
    var busy by remember(text) { mutableStateOf(false) }
    var failed by remember(text) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(text) {
        val iso = LinguaTranslator.detect(text)
        fromIso = iso
        if (iso != null && LinguaBridges.shouldOffer(iso, myCode)) {
            busy = true
            translated = LinguaTranslator.translate(text, iso, LinguaBridges.toIso(myCode), wifiOnly = true)
            busy = false
        }
    }

    Column(modifier = modifier) {
        val done = translated
        Text(if (done != null && !showOriginal) done else text)
        val iso = fromIso
        if (iso == null || !LinguaBridges.shouldOffer(iso, myCode)) return@Column
        val small = MaterialTheme.typography.labelSmall
        when {
            busy -> Text(tr("🌐 prekladám…"), style = small)
            done != null ->
                TextButton(onClick = { showOriginal = !showOriginal }) {
                    val label = if (showOriginal) tr("🌐 Zobraziť preklad") else tr("🌐 Preložené · ukázať originál")
                    Text(label, style = small)
                }
            else ->
                TextButton(
                    onClick = {
                        busy = true
                        scope.launch {
                            translated = LinguaTranslator.translate(text, iso, LinguaBridges.toIso(myCode))
                            failed = translated == null
                            busy = false
                        }
                    },
                ) {
                    val label = if (failed) tr("skús to s internetom") else WorldLanguages.chip(myCode)
                    Text(tr("🌐 Preložiť do: %s", label), style = small)
                }
        }
    }
}
