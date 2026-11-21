package app.quantumsocial.ui.perf

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.quantumsocial.core.model.PerformanceChoice
import app.quantumsocial.core.model.PerformanceMode
import app.quantumsocial.core.model.PerformancePolicy
import app.quantumsocial.core.ui.theme.GalaxyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Aktuálny režim výkonu pre všetky obrazovky (bod 6). */
val LocalPerformanceMode = staticCompositionLocalOf { PerformanceMode.Full }

/** Farebná nálada oblohy (bod 32). */
val LocalGalaxyTheme = staticCompositionLocalOf { GalaxyTheme.Dawn }

/** Voľba farby galaxie z profilu – uložená v SharedPreferences. */
object GalaxyThemeSettings {
    private const val PREFS = "quantum_prefs"
    private const val KEY = "galaxy_theme"

    private val _theme = MutableStateFlow(GalaxyTheme.Dawn)
    val theme: StateFlow<GalaxyTheme> = _theme.asStateFlow()

    fun load(context: Context) {
        _theme.value = GalaxyTheme.parse(prefs(context).getString(KEY, null))
    }

    fun set(
        context: Context,
        theme: GalaxyTheme,
    ) {
        _theme.value = theme
        prefs(context).edit().putString(KEY, theme.name).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** Voľba šetriča z profilu – uložená v SharedPreferences. */
object PerformanceSettings {
    private const val PREFS = "quantum_prefs"
    private const val KEY_CHOICE = "performance_choice"

    private val _choice = MutableStateFlow(PerformanceChoice.Auto)
    val choice: StateFlow<PerformanceChoice> = _choice.asStateFlow()
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        loaded = true
        val stored = prefs(context).getString(KEY_CHOICE, null)
        _choice.value = PerformancePolicy.parseChoice(stored)
    }

    fun set(
        context: Context,
        choice: PerformanceChoice,
    ) {
        _choice.value = choice
        prefs(context).edit().putString(KEY_CHOICE, choice.name).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/** Sleduje šetrič batérie v Androide a poskytne výsledný režim cez [LocalPerformanceMode]. */
@Composable
fun PerformanceProvider(content: @Composable () -> Unit) {
    val context = LocalContext.current
    remember {
        PerformanceSettings.load(context)
        GalaxyThemeSettings.load(context)
    }
    val choice by PerformanceSettings.choice.collectAsState()
    val theme by GalaxyThemeSettings.theme.collectAsState()
    val systemPowerSave by rememberSystemPowerSave(context)
    val mode = PerformancePolicy.resolve(choice, systemPowerSave)
    CompositionLocalProvider(
        LocalPerformanceMode provides mode,
        LocalGalaxyTheme provides theme,
        content = content,
    )
}

@Composable
private fun rememberSystemPowerSave(context: Context): State<Boolean> {
    val power = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    val state = remember { mutableStateOf(power.isPowerSaveMode) }
    DisposableEffect(power) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    c: Context?,
                    intent: Intent?,
                ) {
                    state.value = power.isPowerSaveMode
                }
            }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return state
}

/**
 * Sekundy od otvorenia obrazovky, obnovované podľa režimu výkonu (Saver = pomalší tick).
 * Relatívny čas – Float z absolútnych milisekúnd by stratil presnosť.
 * Hodnotu čítaj až vo vnútri Canvasu, potom sa prekresľuje len kresba, nie celá obrazovka.
 */
@Composable
fun rememberGalaxySeconds(): State<Float> {
    val interval = LocalPerformanceMode.current.frameIntervalMs
    val startedAt = remember { System.nanoTime() }
    return produceState(initialValue = 0f, interval) {
        while (true) {
            value = (System.nanoTime() - startedAt) / 1_000_000_000f
            delay(interval)
        }
    }
}
