package app.quantumsocial.core

import app.quantumsocial.core.model.FutureSeed
import app.quantumsocial.core.model.SeedGarden
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik14Test {
    private fun bloomed(): SeedGarden {
        var g = SeedGarden().plant("a", "  nová   práca ", 10)!!
        repeat(FutureSeed.WATERINGS_TO_BLOOM) { g = g.water("a", 10L + it)!! }
        return g
    }

    @Test
    fun thirstyAfterTwoDaysWithoutWater() {
        val g = SeedGarden().plant("a", "sen", 10)!!
        assertFalse(g.seeds[0].isThirsty(11))
        assertTrue(g.seeds[0].isThirsty(12))
        assertFalse(g.water("a", 12)!!.seeds[0].isThirsty(13))
        assertFalse(bloomed().seeds[0].isThirsty(100))
    }

    @Test
    fun releaseOnlyBloomedOnce() {
        assertNull(SeedGarden().plant("a", "sen", 10)!!.release("a"))
        val r = bloomed().release("a")!!
        assertTrue(r.seeds[0].released)
        assertNull(r.release("a"))
        assertEquals(r, SeedGarden.decode(r.encode()))
    }

    @Test
    fun oldFormatAndRemove() {
        val old = SeedGarden.decode("a\t10\t1\t10\tsen")
        assertFalse(old.seeds[0].released)
        assertTrue(old.remove("a").seeds.isEmpty())
    }
}
