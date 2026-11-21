package app.quantumsocial.core

import app.quantumsocial.core.data.WatchedStars
import app.quantumsocial.core.model.QuantName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StarProfileTest {
    @Before
    fun setUp() = WatchedStars.clear()

    @Test
    fun same_id_gives_always_same_name() {
        val id = "3f2a9c1e-7b4d-4e0a-9f6b-1c2d3e4f5a6b"
        assertEquals(QuantName.of(id), QuantName.of(id))
        assertEquals(QuantName.of("star-42"), QuantName.of("star-42"))
    }

    @Test
    fun name_has_adjective_noun_and_number() {
        val pattern = Regex("^\\p{Lu}\\p{Ll}+ \\p{Lu}\\p{Ll}+ \\d{1,2}$")
        repeat(200) { i ->
            val name = QuantName.of("star-$i")
            assertTrue("zlý tvar mena: $name", pattern.matches(name))
            val number = name.substringAfterLast(' ').toInt()
            assertTrue(number in 1..QuantName.MAX_NUMBER)
        }
    }

    @Test
    fun different_ids_give_varied_names() {
        val names = (0 until 200).map { QuantName.of("star-$it") }.toSet()
        assertTrue("málo rôznych mien: ${names.size}", names.size > 150)
    }

    @Test
    fun watch_toggle_on_and_off() {
        assertFalse(WatchedStars.isWatched("a"))
        assertTrue(WatchedStars.toggle("a"))
        assertTrue(WatchedStars.isWatched("a"))
        assertEquals(setOf("a"), WatchedStars.watched.value)

        assertFalse(WatchedStars.toggle("a"))
        assertFalse(WatchedStars.isWatched("a"))
        assertTrue(WatchedStars.watched.value.isEmpty())
    }

    @Test
    fun watching_one_star_does_not_touch_others() {
        WatchedStars.toggle("a")
        WatchedStars.toggle("b")
        WatchedStars.toggle("a")
        assertEquals(setOf("b"), WatchedStars.watched.value)
    }
}
