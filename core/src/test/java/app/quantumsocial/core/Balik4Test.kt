package app.quantumsocial.core

import app.quantumsocial.core.data.EchoStore
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Echo
import app.quantumsocial.core.model.EchoDay
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.GentleNotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Testy bodov 11 a 15. */
class Balik4Test {
    private val day = 20_000L

    private fun echo(
        id: String,
        ms: Long = 8_000,
        d: Long = day,
    ) = Echo(id, "/x/$id.m4a", ms, Emotion.Calm, d)

    @Before
    fun setUp() {
        StarDust.clear()
        EchoStore.restore(EchoDay())
    }

    // bod 11
    @Test
    fun echo_length_rules() {
        assertTrue(EchoDay.problem(3_000) != null)
        assertNull(EchoDay.problem(5_000))
        assertNull(EchoDay.problem(15_200))
        assertTrue(EchoDay.problem(16_000) != null)
    }

    @Test
    fun at_most_three_echoes_a_day_and_old_ones_expire() {
        var d = EchoDay()
        for (i in 1..3) d = d.add(echo("e$i"))!!
        assertNull(d.add(echo("e4")))
        assertNull(EchoDay().add(echo("short", ms = 2_000)))
        val tomorrow = d.add(echo("t1", d = day + 1))!!
        assertEquals(listOf("t1"), tomorrow.echoes.map { it.id })
        assertEquals(3, d.expired(day + 1).size)
        assertEquals(d, EchoDay.decode(d.encode()))
    }

    @Test
    fun echo_store_gives_dust() {
        assertTrue(EchoStore.add(echo("a")))
        assertFalse(EchoStore.add(echo("b", ms = 1_000)))
        assertEquals(DustAction.Echo.reward, StarDust.state.value.balance)
    }

    // bod 15
    @Test
    fun gentle_note_changes_daily_and_has_no_numbers() {
        assertEquals(GentleNotes.forDay(day), GentleNotes.forDay(day))
        assertTrue(GentleNotes.forDay(day) != GentleNotes.forDay(day + 1))
        assertTrue(GentleNotes.all.none { (t, b) -> (t + b).any { it.isDigit() } })
    }
}
