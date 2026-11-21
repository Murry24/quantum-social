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

    fun restore(state: StarDustState) {
        _state.value = state
    }

    fun clear() {
        _state.value = StarDustState()
    }
}
