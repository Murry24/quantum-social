package app.quantumsocial.core

import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Memory
import app.quantumsocial.core.model.MemoryNebula
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.model.StarLevel
import app.quantumsocial.core.model.WorldLanguages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Testy bodov 24, 27 a jazykov sveta. */
class Balik5Test {
    private val day = 20_000L
    private val dayMs = day * 86_400_000L + 12 * 3_600_000L

    @Before
    fun setUp() {
        StarDust.clear()
        PulseOutbox.clear()
    }

    // jazyky
    @Test
    fun world_languages_are_many_and_unique() {
        assertTrue(WorldLanguages.all.size >= 50)
        assertEquals(WorldLanguages.all.size, WorldLanguages.codes.toSet().size)
        assertEquals("🇸🇰 SK", WorldLanguages.chip("SK"))
        assertEquals("XX", WorldLanguages.chip("XX"))
        assertTrue(WorldLanguages.search("japon").isEmpty())
        assertEquals("JA", WorldLanguages.search("日本").single().code)
        assertTrue(WorldLanguages.search("sl").any { it.code == "SL" })
        assertEquals(WorldLanguages.all, WorldLanguages.search(" "))
    }

    // bod 24
    @Test
    fun memory_nebula_add_filter_remove_roundtrip() {
        var n = MemoryNebula()
        n = n.add(Memory("a", day, Emotion.Love, "  svadba\tsestry  "))!!
        n = n.add(Memory("b", day - 365, Emotion.Joy, "prvý sneh"))!!
        assertNull(n.add(Memory("c", day, Emotion.Joy, "   ")))
        assertEquals(1, n.byEmotion(Emotion.Love).size)
        assertEquals(listOf("b"), n.onThisDay(day).map { it.id })
        assertEquals(n.memories.map { it.id }, MemoryNebula.decode(n.encode()).memories.map { it.id })
        assertEquals(listOf("a"), n.remove("b").memories.map { it.id })
    }

    // bod 27
    @Test
    fun urgent_pulse_costs_dust_once_a_day_and_keeps_level() {
        StarDust.restore(StarDustState(balance = 25))
        val sent = PulseOutbox.send(SignalTarget.Star("a"), Emotion.Joy, emptyList(), now = dayMs, urgent = true)
        assertNotNull(sent)
        assertTrue(sent!!.signal.urgent)
        val s = StarDust.state.value
        assertEquals(25 - StarDustState.ALERT_COST + 1, s.balance)
        assertEquals(StarLevel.Star, StarLevel.of(s.lifetime))
        assertNull(PulseOutbox.send(SignalTarget.Star("a"), Emotion.Joy, emptyList(), now = dayMs, urgent = true))
        assertFalse(s.canAlert(day))
        assertEquals(s, StarDustState.decode(s.encode()))
    }

    @Test
    fun urgent_pulse_needs_enough_dust_and_old_format_still_reads() {
        StarDust.restore(StarDustState(balance = 5))
        assertNull(PulseOutbox.send(SignalTarget.Star("a"), Emotion.Joy, emptyList(), now = dayMs, urgent = true))
        assertEquals(StarDustState(balance = 7, day = 3), StarDustState.decode("7|3|"))
    }
}
