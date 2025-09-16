package app.quantumsocial.core.data

import app.quantumsocial.core.model.Signal
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

object InMemorySignalRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _signals = MutableStateFlow<List<Signal>>(emptyList())
    val signals: StateFlow<List<Signal>> = _signals.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val filtered = _signals.value.filterNot { it.isExpired(now) }
                if (filtered.size != _signals.value.size) _signals.value = filtered
                delay(1_000)
            }
        }
    }

    fun send(signal: Signal) {
        _signals.value = listOf(signal) + _signals.value
    }

    fun markRead(id: String) {
        val s = _signals.value.firstOrNull { it.id == id } ?: return
        if (s.expireOnRead) _signals.value = _signals.value.filterNot { it.id == id }
    }

    fun clear() {
        _signals.value = emptyList()
    }

    fun shutdown() {
        scope.cancel()
    }
}
