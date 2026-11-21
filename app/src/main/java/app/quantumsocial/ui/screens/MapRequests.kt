package app.quantumsocial.ui.screens

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Čo má mapa otvoriť po ťuku na ✦ v spodnej lište (lišta je mimo mapy). */
enum class MapRequest { Chaos, Galaxies }

object MapRequests {
    private val _pending = MutableStateFlow<MapRequest?>(null)
    val pending: StateFlow<MapRequest?> = _pending.asStateFlow()

    fun request(r: MapRequest) {
        _pending.value = r
    }

    fun consume() {
        _pending.value = null
    }
}
