package app.quantumsocial.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import app.quantumsocial.core.model.CustomSound
import java.io.File

/**
 * Vlastný zvuk mojej hviezdy (bod 28). Vybraný súbor sa skopíruje do aplikácie,
 * takže funguje aj keď ho používateľ neskôr v telefóne presunie alebo zmaže.
 */
object CustomSoundStore {
    private const val FILE = "my_star_sound"

    private fun file(context: Context) = File(context.filesDir, FILE)

    fun has(context: Context): Boolean = file(context).exists()

    /** @return null pri úspechu, inak dôvod, prečo sa zvuk nedá použiť */
    fun import(
        context: Context,
        uri: Uri,
    ): String? {
        val resolver = context.contentResolver
        val bytes = resolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
        val duration =
            try {
                MediaMetadataRetriever().run {
                    try {
                        setDataSource(context, uri)
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                    } finally {
                        release()
                    }
                }
            } catch (e: RuntimeException) {
                null
            }
        CustomSound.problem(duration, if (bytes < 0) CustomSound.MAX_BYTES + 1 else bytes)?.let { return it }
        resolver.openInputStream(uri)?.use { input -> file(context).outputStream().use { input.copyTo(it) } }
            ?: return "Súbor sa nedá otvoriť."
        return null
    }

    fun play(context: Context) {
        val player = MediaPlayer.create(context, Uri.fromFile(file(context))) ?: return
        player.setOnCompletionListener { it.release() }
        player.start()
    }

    fun remove(context: Context) {
        file(context).delete()
    }
}
