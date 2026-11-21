package app.quantumsocial.core.data

import app.quantumsocial.core.model.Signal
import app.quantumsocial.core.model.SignalLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Lokálne (in-memory) úložisko signálov.
 *
 * Signál sa po vypršaní alebo prečítaní neodstráni hneď – najprv sa na
 * [SignalLifecycle.FADE_OUT_MS] zaradí do [fadingSince], aby UI stihlo
 * prehrať animáciu zhasnutia, a až potom zmizne zo [signals].
 */
object InMemorySignalRepository {
    private const val TICK_MS = 250L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _signals = MutableStateFlow<List<Signal>>(emptyList())
    val signals: StateFlow<List<Signal>> = _signals.asStateFlow()

    /** id signálu -> čas, odkedy zhasína. */
    private val _fadingSince = MutableStateFlow<Map<String, Long>>(emptyMap())
    val fadingSince: StateFlow<Map<String, Long>> = _fadingSince.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                sweep()
                delay(TICK_MS)
            }
        }
    }

    fun send(signal: Signal) {
        _signals.value = listOf(signal) + _signals.value
    }

    /** Prečítanie: signál s expireOnRead začne zhasínať. */
    fun markRead(
        id: String,
        now: Long = System.currentTimeMillis(),
    ) {
        val s = _signals.value.firstOrNull { it.id == id } ?: return
        if (s.expireOnRead && id !in _fadingSince.value) {
            _fadingSince.value = _fadingSince.value + (id to now)
        }
    }

    /**
     * Posunie životný cyklus: vypršané signály začnú zhasínať,
     * dozhasnuté sa odstránia. Volá sa automaticky; verejné kvôli testom.
     */
    fun sweep(now: Long = System.currentTimeMillis()) {
        val fading = _fadingSince.value.toMutableMap()
        for (s in _signals.value) {
            if (s.id !in fading && s.isExpired(now)) {
                fading[s.id] = s.expiresAt ?: now
            }
        }
        val gone = fading.filterValues { now - it >= SignalLifecycle.FADE_OUT_MS }.keys
        if (gone.isNotEmpty()) {
            _signals.value = _signals.value.filterNot { it.id in gone }
            // bod 7: odpovede zhasnú spolu s pôvodným signálom
            ReplyStore.drop(gone)
        }
        val next = fading - gone
        if (next != _fadingSince.value) _fadingSince.value = next
    }

    fun clear() {
        _signals.value = emptyList()
        _fadingSince.value = emptyMap()
    }

    fun shutdown() {
        scope.cancel()
    }
}
