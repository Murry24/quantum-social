package app.quantumsocial.ui.signals

import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.quantumsocial.core.model.AudioSignal
import kotlinx.coroutines.delay
import java.io.File
import kotlin.time.Duration.Companion.seconds

private const val MAX_MS = 15_000

@Composable
fun SignalAudioRecorder(onSubmit: (AudioSignal) -> Unit) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0) }
    var lastFilePath by remember { mutableStateOf<String?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (granted) isRecording = true
            },
        )

    // Recorder držíme ako lokálnu premennú
    val recorder =
        remember {
            if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()
        }

    fun startRecording(): String {
        val out = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")
        recorder.reset()
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.setAudioEncodingBitRate(128_000)
        recorder.setAudioSamplingRate(44_100)
        recorder.setOutputFile(out.absolutePath)
        recorder.prepare()
        recorder.start()
        return out.absolutePath
    }

    fun stopRecording() {
        try {
            recorder.stop()
        } catch (_: Exception) {
            // ignore
        }
        recorder.reset()
    }

    // Časovač a auto-stop po 15s
    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedMs = 0
            while (isRecording && elapsedMs < MAX_MS) {
                delay(100)
                elapsedMs += 100
            }
            if (isRecording) {
                isRecording = false
                stopRecording()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                recorder.release()
            } catch (_: Exception) {
            }
        }
    }

    Column(Modifier.padding(16.dp)) {
        if (!isRecording) {
            Button(onClick = {
                // žiadosť o povolenie
                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                // start sa nastaví v onResult keď je granted (vyššie)
                lastFilePath = startRecording()
                isRecording = true
            }) { Text("🎙️ Nahrať (max 15 s)") }
        } else {
            Button(onClick = {
                isRecording = false
                stopRecording()
            }) { Text("⏹️ Stop (${elapsedMs / 1000}s)") }
        }

        if (!isRecording && lastFilePath != null && elapsedMs > 0) {
            val seconds = (elapsedMs / 1000).toInt()
            Button(
                onClick = {
                    onSubmit(AudioSignal(filePath = lastFilePath!!, durationMs = elapsedMs.toInt()))
                    // clear
                    lastFilePath = null
                    elapsedMs = 0
                },
                modifier = Modifier.padding(top = 12.dp),
            ) { Text("Odoslať audio ($seconds s)") }
        }
    }
}
