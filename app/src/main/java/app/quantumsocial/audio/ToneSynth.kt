package app.quantumsocial.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import app.quantumsocial.core.model.StarChord
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Prehrá melódiu hviezdy (bod 28) – jemné sínusové tóny s plynulým nábehom a doznením. */
object ToneSynth {
    private const val RATE = 22_050
    private const val VOLUME = 0.25

    fun play(chord: StarChord) {
        thread(name = "star-chord") {
            val samples = render(chord)
            val track =
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(samples.size * 2)
                    .build()
            try {
                track.write(samples, 0, samples.size)
                track.play()
                Thread.sleep(samples.size * 1000L / RATE + 100)
            } finally {
                track.release()
            }
        }
    }

    private fun render(chord: StarChord): ShortArray {
        val out = ArrayList<Short>()
        chord.notesHz.zip(chord.durationsMs).forEach { (hz, ms) ->
            val n = RATE * ms / 1000
            val fade = min(n / 4, RATE / 50)
            for (i in 0 until n) {
                val env = min(1.0, min(i, n - i).toDouble() / fade)
                val v = sin(2 * PI * hz * i / RATE) * VOLUME * env
                out.add((v * Short.MAX_VALUE).toInt().toShort())
            }
        }
        return out.toShortArray()
    }
}
