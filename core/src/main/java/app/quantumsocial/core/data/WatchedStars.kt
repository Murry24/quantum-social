package app.quantumsocial.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * „Sleduj žiarenie“ (bod 5) – nie je to follow, len jemné označenie hviezd,
 * ktorých svetlo chceš vidieť. Zatiaľ len v pamäti (po reštarte aplikácie zmizne).
 */
object WatchedStars {
    private val _watched = MutableStateFlow<Set<String>>(emptySet())
    val watched: StateFlow<Set<String>> = _watched.asStateFlow()

    fun isWatched(starId: String): Boolean = starId in _watched.value

    /** @return true, ak hviezda je po prepnutí sledovaná */
    fun toggle(starId: String): Boolean {
        val nowWatched = !isWatched(starId)
        _watched.value = if (nowWatched) _watched.value + starId else _watched.value - starId
        return nowWatched
    }

    fun clear() {
        _watched.value = emptySet()
    }
}
