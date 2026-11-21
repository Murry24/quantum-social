package app.quantumsocial.core

import app.quantumsocial.core.data.ReplyStore
import app.quantumsocial.core.data.SeedStore
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.CustomSound
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.SeedGarden
import app.quantumsocial.core.model.SeedStage
import app.quantumsocial.core.model.StarChord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Testy bodov 28, 29 a 30. */
class Balik3Test {
    private val day = 20_000L
    private val dayMs = day * 86_400_000L + 12 * 3_600_000L

    @Before
    fun setUp() {
        StarDust.clear()
        ReplyStore.clear()
        SeedStore.restore(SeedGarden())
    }

    // bod 28
    @Test
    fun star_chord_is_personal_and_changes_by_variant() {
        val a = StarChord.of("hviezda-a")
        assertEquals(a, StarChord.of("hviezda-a"))
        assertEquals(5, a.notesHz.size)
        assertTrue(a.notesHz.all { it in 260.0..881.0 })
        assertNotEquals(a, StarChord.of("hviezda-a", variant = 1))
    }

    @Test
    fun chord_changes_at_most_once_per_quarter() {
        val q = StarChord.quarter(2026, 11)
        assertEquals(StarChord.quarter(2026, 10), q)
        assertTrue(StarChord.quarter(2027, 1) > q)
        assertTrue(StarChord.canChange(null, q))
        assertFalse(StarChord.canChange(q, q))
        assertTrue(StarChord.canChange(q, q + 1))
    }

    @Test
    fun custom_sound_must_be_short_and_small() {
        assertNull(CustomSound.problem(8_000, 300_000))
        assertTrue(CustomSound.problem(null, 300_000) != null)
        assertTrue(CustomSound.problem(16_000, 300_000)!!.contains("15"))
        assertTrue(CustomSound.problem(5_000, 3_000_000)!!.contains("2 MB"))
    }

    // bod 29
    @Test
    fun first_reply_gets_empathy_bonus() {
        ReplyStore.reply("s1", Reply.emotion(Emotion.Love))
        ReplyStore.reply("s1", Reply.emotion(Emotion.Joy))
        val expected = 2 * DustAction.Reply.reward + DustAction.Empathy.reward
        assertEquals(expected, StarDust.state.value.balance)
    }

    // bod 30
    @Test
    fun seed_grows_with_daily_watering_and_blooms() {
        var g = SeedGarden().plant("x", "  nový   začiatok ", day)!!
        assertEquals("nový začiatok", g.seeds.single().text)
        assertNull(g.water("x", day)?.water("x", day))
        for (d in 0L until 5L) g = g.water("x", day + d)!!
        assertEquals(SeedStage.Bloom, g.seeds.single().stage)
        assertNull(g.water("x", day + 9))
        assertEquals(g, SeedGarden.decode(g.encode()))
    }

    @Test
    fun at_most_three_growing_seeds_and_watering_gives_dust() {
        var g = SeedGarden()
        for (i in 1..3) g = g.plant("s$i", "prianie $i", day)!!
        assertNull(g.plant("s4", "ďalšie", day))
        assertNull(SeedGarden().plant("e", "   ", day))
        assertTrue(SeedStore.plant("viac svetla", dayMs))
        val id = SeedStore.garden.value.seeds.single().id
        assertTrue(SeedStore.water(id, dayMs))
        assertFalse(SeedStore.water(id, dayMs))
        assertEquals(DustAction.Seed.reward, StarDust.state.value.balance)
    }
}
