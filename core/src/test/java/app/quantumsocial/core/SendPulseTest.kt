package app.quantumsocial.core

import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.model.ChaosPicker
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.model.ZenSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class SendPulseTest {
    @Before
    fun setUp() = PulseOutbox.clear()

    @Test
    fun pulse_to_star_goes_to_that_star() {
        val sent = PulseOutbox.send(SignalTarget.Star("a"), Emotion.Love, visibleStarIds = emptyList())
        assertNotNull(sent)
        assertEquals("a", sent!!.resolvedStarId)
        assertEquals(Emotion.Love, sent.signal.emotion)
        assertEquals(1, PulseOutbox.sent.value.size)
    }

    @Test
    fun pulse_to_chaos_picks_one_of_visible_stars() {
        val visible = listOf("a", "b", "c")
        repeat(20) { i ->
            val sent = PulseOutbox.send(SignalTarget.Chaos, Emotion.Joy, visible, random = Random(i))
            assertTrue(sent!!.resolvedStarId in visible)
        }
    }

    @Test
    fun pulse_to_chaos_without_stars_is_not_sent() {
        assertNull(PulseOutbox.send(SignalTarget.Chaos, Emotion.Calm, visibleStarIds = emptyList()))
        assertTrue(PulseOutbox.sent.value.isEmpty())
    }

    @Test
    fun chaos_picker_respects_exclude() {
        repeat(20) { i ->
            assertEquals("b", ChaosPicker.pick(listOf("a", "b"), exclude = "a", random = Random(i)))
        }
        assertNull(ChaosPicker.pick(listOf("a"), exclude = "a"))
    }

    @Test
    fun pulse_signal_is_ephemeral() {
        val sent = PulseOutbox.send(SignalTarget.Star("a"), Emotion.Energy, emptyList(), now = 1_000L)!!
        assertTrue(sent.signal.expireOnRead)
        assertEquals(31_000L, sent.signal.expiresAt)
    }

    @Test
    fun zen_session_counts_down_one_minute() {
        val zen = ZenSession(startedAt = 10_000L)
        assertEquals(60_000L, zen.remainingMs(10_000L))
        assertEquals(0.5f, zen.progress(40_000L), 0.001f)
        assertFalse(zen.isFinished(69_999L))
        assertTrue(zen.isFinished(70_000L))
        assertEquals(0L, zen.remainingMs(99_000L))
        assertEquals(60_000L, zen.remainingMs(5_000L))
    }
}
