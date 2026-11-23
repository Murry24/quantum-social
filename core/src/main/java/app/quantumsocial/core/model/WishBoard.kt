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

    /** Riadky: T id téma | C id (palec) | M id (moje) | D id (splnené). */
    fun encode(): String =
        (
            themes.map { "T\t${it.key}\t${it.value.name}" } + cheered.map { "C\t$it" } +
                mine.map { "M\t$it" } + fulfilled.map { "D\t$it" }
        ).joinToString("\n")

    /** Necháme len záznamy k prianiam, ktoré ešte žijú. */
    fun keepOnly(ids: Set<String>): WishBoard =
        WishBoard(themes.filterKeys { it in ids }, cheered intersect ids, mine intersect ids, fulfilled intersect ids)

    companion object {
        fun decode(stored: String?): WishBoard {
            var b = WishBoard()
            stored.orEmpty().lineSequence().forEach { line ->
                val p = line.split('\t')
                runCatching {
                    b =
                        when (p[0]) {
                            "T" -> b.copy(themes = b.themes + (p[1] to WishTheme.valueOf(p[2])))
                            "C" -> b.copy(cheered = b.cheered + p[1])
                            "M" -> b.copy(mine = b.mine + p[1])
                            "D" -> b.copy(fulfilled = b.fulfilled + p[1])
                            else -> b
                        }
                }
            }
            return b
        }

        fun demoCheers(wishId: String): Int = abs(wishId.hashCode() % 13)
    }
}
