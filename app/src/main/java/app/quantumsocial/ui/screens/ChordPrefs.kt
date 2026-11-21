package app.quantumsocial.ui.screens

import android.content.Context
import app.quantumsocial.core.model.StarChord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Variant melódie (bod 28) a štvrťrok poslednej zmeny – (variant, štvrťrok alebo null). */
object ChordPrefs {
    private const val PREFS = "quantum_prefs"
    private const val KEY_VARIANT = "chord_variant"
    private const val KEY_QUARTER = "chord_changed_quarter"

    private val _state = MutableStateFlow(0 to null as Int?)
    val state: StateFlow<Pair<Int, Int?>> = _state.asStateFlow()

    fun load(context: Context) {
        val p = prefs(context)
        _state.value = p.getInt(KEY_VARIANT, 0) to p.getInt(KEY_QUARTER, -1).takeIf { it >= 0 }
    }

    private fun nowQuarter(): Int = LocalDate.now().let { StarChord.quarter(it.year, it.monthValue) }

    fun canChange(): Boolean = StarChord.canChange(_state.value.second, nowQuarter())

    fun change(context: Context) {
        if (!canChange()) return
        val next = (_state.value.first + 1) to nowQuarter()
        _state.value = next
        prefs(context).edit().putInt(KEY_VARIANT, next.first).putInt(KEY_QUARTER, next.second).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
