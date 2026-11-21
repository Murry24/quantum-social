package app.quantumsocial.core.data

import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.StarDustState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Môj StarDust (bod 20). Ukladanie na disk rieši app (`StarDustPrefs`). */
object StarDust {
    private val _state = MutableStateFlow(StarDustState())
    val state: StateFlow<StarDustState> = _state.asStateFlow()

    /** @return koľko prachu pribudlo */
    fun earn(
        action: DustAction,
        now: Long = System.currentTimeMillis(),
    ): Int {
        val (next, gained) = _state.value.earn(action, StarDustState.epochDay(now))
        _state.value = next
        return gained
    }

    /** Zaplatí naliehavý pulz (bod 27). @return false, ak nie je dosť prachu alebo už dnes bol */
    fun useAlert(now: Long = System.currentTimeMillis()): Boolean {
        val next = _state.value.useAlert(StarDustState.epochDay(now)) ?: return false
        _state.value = next
        return true
    }

    fun restore(state: StarDustState) {
        _state.value = state
    }

    fun clear() {
        _state.value = StarDustState()
    }
}
