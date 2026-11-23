package app.quantumsocial.core.model

import kotlin.math.abs

/** Téma priania vo WishNete. */
enum class WishTheme(val emoji: String, val label: String) {
    Health("💚", "Zdravie"),
    Love("💗", "Láska"),
    Family("🏡", "Rodina"),
    Work("💼", "Práca"),
    Travel("✈️", "Cesty"),
    Dreams("🌙", "Sny"),
    Peace("🕊️", "Pokoj"),
}

enum class WishSort(val label: String) { Newest("Najnovšie"), MostCheered("Najviac palcov") }

/**
 * WishNet+ : témy prianí, „držím palce“ a „splnilo sa“.
 * Žije len spolu so signálmi (24 h v pamäti), preto sa neukladá.
 */
data class WishBoard(
    val themes: Map<String, WishTheme> = emptyMap(),
    val cheered: Set<String> = emptySet(),
    val mine: Set<String> = emptySet(),
    val fulfilled: Set<String> = emptySet(),
) {
    fun post(
        wishId: String,
        theme: WishTheme?,
    ): WishBoard = copy(themes = if (theme == null) themes else themes + (wishId to theme), mine = mine + wishId)

    /** Vlastnému prianiu palce nedržím; každému inému raz (druhý ťuk vráti späť). */
    fun toggleCheer(wishId: String): WishBoard =
        when {
            wishId in mine -> this
            wishId in cheered -> copy(cheered = cheered - wishId)
            else -> copy(cheered = cheered + wishId)
        }

    /** Ukážkové hviezdy „držia palce“ pevným počtom podľa id, kým nie je server. */
    fun cheersFor(wishId: String): Int {
        val demo = if (wishId in mine) 0 else demoCheers(wishId)
        return demo + if (wishId in cheered) 1 else 0
    }

    fun fulfill(wishId: String): WishBoard = if (wishId in mine) copy(fulfilled = fulfilled + wishId) else this

    fun <T> arrange(
        items: List<T>,
        idOf: (T) -> String,
        createdAtOf: (T) -> Long,
        theme: WishTheme?,
        sort: WishSort,
    ): List<T> {
        val shown = if (theme == null) items else items.filter { themes[idOf(it)] == theme }
        return when (sort) {
            WishSort.Newest -> shown.sortedByDescending(createdAtOf)
            WishSort.MostCheered -> shown.sortedByDescending { cheersFor(idOf(it)) }
        }
    }

    companion object {
        fun demoCheers(wishId: String): Int = abs(wishId.hashCode() % 13)
    }
}
