package app.quantumsocial.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Naposledy použité emoji – len v pamäti, najnovšie prvé, bez opakovania. */
object RecentEmojis {
    const val MAX = 8

    private val _recent = MutableStateFlow<List<String>>(emptyList())
    val recent: StateFlow<List<String>> = _recent.asStateFlow()

    fun use(emoji: String) {
        _recent.value = (listOf(emoji) + _recent.value.filter { it != emoji }).take(MAX)
    }

    fun clear() {
        _recent.value = emptyList()
    }
}
