package app.quantumsocial.core

import app.quantumsocial.core.model.WishBoard
import app.quantumsocial.core.model.WishSort
import app.quantumsocial.core.model.WishTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class Balik8Test {
    private data class W(val id: String, val at: Long)

    @Test
    fun ownWishCannotBeCheeredButCanBeFulfilled() {
        val b = WishBoard().post("mine", WishTheme.Love).toggleCheer("mine").fulfill("mine")
        assertEquals(0, b.cheersFor("mine"))
        assertEquals(setOf("mine"), b.fulfilled)
    }

    @Test
    fun cheerTogglesOnceForOthers() {
        val base = WishBoard.demoCheers("other")
        val b = WishBoard().toggleCheer("other")
        assertEquals(base + 1, b.cheersFor("other"))
        assertEquals(base, b.toggleCheer("other").cheersFor("other"))
        assertEquals(emptySet<String>(), WishBoard().fulfill("other").fulfilled)
    }

    @Test
    fun arrangeFiltersByThemeAndSorts() {
        val items = listOf(W("a", 1), W("b", 3), W("c", 2))
        val b = WishBoard().post("a", WishTheme.Dreams).post("c", WishTheme.Dreams)
        val dreams = b.arrange(items, { it.id }, { it.at }, WishTheme.Dreams, WishSort.Newest)
        assertEquals(listOf("c", "a"), dreams.map { it.id })
        val all = b.arrange(items, { it.id }, { it.at }, null, WishSort.Newest)
        assertEquals(listOf("b", "c", "a"), all.map { it.id })
    }
}
