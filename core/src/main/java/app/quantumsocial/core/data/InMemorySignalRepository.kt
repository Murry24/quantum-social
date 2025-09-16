package app.quantumsocial.core.data

import app.quantumsocial.core.model.Signal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemorySignalRepository {
    private val _signals = MutableStateFlow<List<Signal>>(emptyList())
    val signals: StateFlow<List<Signal>> = _signals.asStateFlow()

    fun send(signal: Signal) {
        // Najnovšie hore
        _signals.value = listOf(signal) + _signals.value
    }
}
