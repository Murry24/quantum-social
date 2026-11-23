package app.quantumsocial.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.quantumsocial.audio.CustomSoundStore
import app.quantumsocial.core.data.EchoStore
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Echo
import app.quantumsocial.core.model.EchoDay
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.ui.signals.EmotionOrb
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID

/** Nahrávanie ozveny: povolenie mikrofónu sa pýta pred prvým nahrávaním, nie po ňom. */
private class EchoRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var file: File? = null
        private set

    fun start() {
        val dir = File(context.filesDir, "echoes").apply { mkdirs() }
        val out = File(dir, "echo_${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(96_000)
        r.setAudioSamplingRate(44_100)
        r.setOutputFile(out.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        file = out
    }

    fun stop() {
        try {
            recorder?.stop()
        } catch (e: RuntimeException) {
            // príliš krátka nahrávka – súbor je neplatný
            file?.delete()
            file = null
        }
        recorder?.release()
        recorder = null
    }
}

private fun playFile(path: String) {
    val player = MediaPlayer()
    try {
        player.setDataSource(path)
        player.setOnCompletionListener { it.release() }
        player.prepare()
        player.start()
    } catch (e: Exception) {
        player.release()
    }
}

/** EchoNet (bod 11): zvuk dňa – 5 až 15 sekúnd s náladou, žije do konca dňa. */
@Composable
fun EchoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val echoes by EchoStore.day.collectAsState()
    val today = StarDustState.epochDay(System.currentTimeMillis())
    val recorder = remember { EchoRecorder(context) }
    var recording by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var recorded by remember { mutableStateOf<Pair<File, Long>?>(null) }
    var mood by remember { mutableStateOf(Emotion.Calm) }
    var message by remember { mutableStateOf<String?>(null) }
    val onSky = Color(GalaxyPalette.ON_SKY)

    fun begin() {
        recorded = null
        message = null
        recorder.start()
        startedAt = SystemClock.elapsedRealtime()
        recording = true
    }

    fun finish() {
        recording = false
        recorder.stop()
        val ms = SystemClock.elapsedRealtime() - startedAt
        val f = recorder.file
        val problem = EchoDay.problem(ms)
        if (f == null || problem != null) {
            f?.delete()
            message = problem ?: tr("Nahrávka sa nepodarila.")
        } else {
            recorded = f to ms
        }
    }

    val askMic =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) begin() else message = tr("Bez mikrofónu sa ozvena nahrať nedá.")
        }

    LaunchedEffect(recording) {
        while (recording) {
            elapsed = SystemClock.elapsedRealtime() - startedAt
            if (elapsed >= EchoDay.MAX_MS) finish()
            delay(200)
        }
    }
    DisposableEffect(Unit) { onDispose { if (recording) recorder.stop() } }

    Column(
        modifier = Modifier.fillMaxSize().background(GalaxyPalette.zenBrush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text(tr("‹ Späť"), color = onSky) }
        Text(tr("🎧 EchoNet – ozvena dňa"), style = MaterialTheme.typography.titleLarge, color = onSky)
        Text(tr("Nahraj 5 až 15 sekúnd zvuku svojho dňa. Ozveny zhasnú o polnoci."), color = onSky)

        when {
            recording ->
                Button(onClick = { finish() }) { Text(tr("⏹ Stop (%s s)", elapsed / 1000)) }
            !echoes.canAdd(today) ->
                Text(tr("Dnes už máš %s ozveny. Zajtra môžeš nahrať ďalšie.", EchoDay.MAX_PER_DAY), color = onSky)
            else ->
                Button(
                    onClick = {
                        val granted =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                                PackageManager.PERMISSION_GRANTED
                        if (granted) begin() else askMic.launch(Manifest.permission.RECORD_AUDIO)
                    },
                ) { Text(tr("🎙 Nahrať ozvenu")) }
        }
        message?.let { Text(it, color = onSky) }

        recorded?.let { (file, ms) ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr("Aká je nálada tejto ozveny? (%s s)", ms / 1000))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Emotion.values().forEach { e -> EmotionOrb(emotion = e, selected = e == mood) { mood = e } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { playFile(file.absolutePath) }) { Text(tr("▶ Vypočuť")) }
                        Button(
                            onClick = {
                                val echo = Echo(UUID.randomUUID().toString(), file.absolutePath, ms, mood, today)
                                if (EchoStore.add(echo)) {
                                    recorded = null
                                    message = tr("Ozvena letí galaxiou ✨")
                                }
                            },
                        ) { Text(tr("Pustiť do éteru")) }
                    }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(echoes.today(today), key = { it.id }) { e ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                        Text(
                            tr("🎧 %s · %s s", tr(e.mood.label()), e.durationMs / 1000),
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { playFile(e.filePath) }) { Text("▶") }
                        TextButton(
                            onClick = {
                                CustomSoundStore.useFile(context, File(e.filePath))
                                message = tr("Ozvena je teraz zvukom tvojej hviezdy 🎵")
                            },
                        ) { Text(tr("Môj zvuk")) }
                    }
                }
            }
        }
    }
}
