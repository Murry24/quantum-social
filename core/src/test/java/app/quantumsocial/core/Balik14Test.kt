package app.quantumsocial.core

import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.SeedGarden
import app.quantumsocial.core.model.SeedHelpers
import app.quantumsocial.core.model.SeedOutcome
import app.quantumsocial.core.model.SeedSize
import app.quantumsocial.core.model.SeedStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik14Test {
    private fun bloomed(size: SeedSize = SeedSize.Small): SeedGarden {
        var g = SeedGarden().plant("a", "  nová   práca ", 10, size)!!
        repeat(size.days) { g = g.water("a", 10L + it, Emotion.Love, "krok\t$it")!! }
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
    fun sizeDecidesDaysAndStepsAreKept() {
        val tree = bloomed(SeedSize.Tree).seeds[0]
        assertEquals(SeedStage.Bloom, tree.stage)
        assertEquals(30, tree.steps.size)
        assertEquals("krok 0", tree.steps[0].text)
        assertEquals(listOf(Emotion.Love), tree.flowerEmotions)
        var g = SeedGarden().plant("b", "strom", 0, SeedSize.Tree)!!
        repeat(15) { g = g.water("b", it.toLong())!! }
        assertEquals(SeedStage.Bud, g.seeds[0].stage)
    }

    @Test
    fun answerAfterBloom() {
        val g = bloomed()
        assertTrue(g.seeds[0].awaitsAnswer)
        val more = g.notYet("a")!!
        assertEquals(SeedStage.Bud, more.seeds[0].stage)
        assertTrue(more.water("a", 50) != null)
        val done = g.finish("a", SeedOutcome.Done)!!
        assertEquals(1, done.flowers.size)
        assertNull(done.finish("a", SeedOutcome.Done))
        assertTrue(g.finish("a", SeedOutcome.Changed)!!.seeds.isEmpty())
        assertNull(SeedGarden().plant("x", "sen", 1)!!.finish("x", SeedOutcome.Done))
    }

    @Test
    fun releaseOnlyFulfilledOnce() {
        assertNull(bloomed().release("a"))
        val r = bloomed().finish("a", SeedOutcome.Done)!!.release("a")!!
        assertTrue(r.seeds[0].released)
        assertNull(r.release("a"))
    }

    @Test
    fun helpOnlyWhenSharedAndStable() {
        val g = SeedGarden().plant("a", "sen", 10)!!
        assertTrue(SeedHelpers.helpFor(g.seeds[0], 20).isEmpty())
        val shared = g.share("a", 10)!!
        assertNull(shared.share("a", 11))
        val help = SeedHelpers.helpFor(shared.seeds[0], 20)
        assertEquals(help, SeedHelpers.helpFor(shared.seeds[0], 20))
        assertTrue(help.all { it.day in 10..20 })
    }

    @Test
    fun encodeRoundTripAndOldFormat() {
        val g = bloomed().share("a", 11)!!.notYet("a")!!.water("a", 40, Emotion.Joy, "ďalší")!!
        assertEquals(g, SeedGarden.decode(g.encode()))
        val old = SeedGarden.decode("a\t10\t1\t10\tsen\t1")
        assertEquals("sen", old.seeds[0].text)
        assertTrue(old.seeds[0].released)
        assertTrue(old.remove("a").seeds.isEmpty())
    }
}
