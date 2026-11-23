package app.quantumsocial.lingua

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Lingua Bridges (bod 25): rozpoznanie jazyka a preklad priamo v telefóne (Google ML Kit).
 * Pri prvom preklade sa stiahne jazykový balík (~30 MB), potom funguje aj bez internetu.
 */
object LinguaTranslator {
    private suspend fun <T> Task<T>.awaitOrNull(): T? =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resume(null) }
        }

    /** @return ISO kód jazyka textu, alebo null ak sa nedá určiť */
    suspend fun detect(text: String): String? =
        LanguageIdentification.getClient().identifyLanguage(text).awaitOrNull()?.takeIf { it != "und" }

    suspend fun translate(
        text: String,
        fromIso: String,
        toIso: String,
    ): String? {
        val from = TranslateLanguage.fromLanguageTag(fromIso) ?: return null
        val to = TranslateLanguage.fromLanguageTag(toIso) ?: return null
        val options = TranslatorOptions.Builder().setSourceLanguage(from).setTargetLanguage(to).build()
        val translator = Translation.getClient(options)
        return try {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).awaitOrNull() ?: return null
            translator.translate(text).awaitOrNull()
        } finally {
            translator.close()
        }
    }
}
