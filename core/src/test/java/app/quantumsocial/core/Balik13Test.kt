package app.quantumsocial.core

import app.quantumsocial.core.model.BreathPattern
import app.quantumsocial.core.model.BreathStep
import app.quantumsocial.core.model.ZenDays
import org.junit.Assert.assertEquals
import org.junit.Test

class Balik13Test {
    @Test
    fun breathFollowsPattern() {
        val p = BreathPattern.Relax
        assertEquals(BreathStep.In, p.at(1f).first)
        assertEquals(0.5f, p.at(2f).second, 0.001f)
        assertEquals(BreathStep.Hold to 1f, p.at(6f))
        assertEquals(BreathStep.Out, p.at(12f).first)
        // po 19 s sa cyklus opakuje
        assertEquals(p.at(1f), p.at(20f))
    }

    @Test
    fun zenStreak() {
        val d = ZenDays().add(8).add(9).add(10)
        assertEquals(3, d.streak(10))
        assertEquals(3, d.streak(11))
        assertEquals(0, d.streak(13))
        assertEquals(d, ZenDays.decode(d.encode()))
    }
}
