package app.quantumsocial.core

import app.quantumsocial.core.data.ChallengeStore
import app.quantumsocial.core.data.GratitudeStore
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.DailyChallenge
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.LightSignature
import app.quantumsocial.core.model.MoodWaves
import app.quantumsocial.core.model.StarLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Testy bodov 13, 14, 16, 21 a 26. */
class BalikBodovTest {
    private val day = 20_000L
    private val dayMs = day * 86_400_000L + 12 * 3_600_000L

    @Before
    fun setUp() {
        StarDust.clear()
        GratitudeStore.restore(GratitudeJournal())
        ChallengeStore.restore(null)
    }

    // bod 16
    @Test
    fun levels_grow_with_dust() {
        assertEquals(StarLevel.Spark, StarLevel.of(0))
        assertEquals(StarLevel.Star, StarLevel.of(20))
        assertEquals(StarLevel.Pulsar, StarLevel.of(149))
        assertEquals(StarLevel.Supernova, StarLevel.of(10_000))
        assertEquals(0.5f, StarLevel.progress(10), 0.001f)
        assertEquals(1f, StarLevel.progress(500), 0.001f)
    }

    // bod 14
    @Test
    fun gratitude_once_a_day_and_fades_after_week() {
        val j = GratitudeJournal().write("  slnko   ráno ", day)!!
        assertEquals("slnko ráno", j.entries.single().text)
        assertNull(j.write("druhý zápis", day))
        assertNull(GratitudeJournal().write("   ", day))
        assertNull(GratitudeJournal().write("x".repeat(141), day))
        val later = j.write("čaj", day + 7)!!
        assertEquals(listOf("čaj"), later.entries.map { it.text })
    }

    @Test
    fun gratitude_roundtrip_and_store_gives_dust() {
        val j = GratitudeJournal().write("mama", day)!!.write("les", day + 1)!!
        assertEquals(j, GratitudeJournal.decode(j.encode()))
        assertEquals(GratitudeJournal(), GratitudeJournal.decode(null))
        assertTrue(GratitudeStore.write("priatelia", dayMs))
        assertFalse(GratitudeStore.write("ešte raz", dayMs))
        assertEquals(DustAction.Gratitude.reward, StarDust.state.value.balance)
    }

    // bod 13
    @Test
    fun challenge_is_same_for_day_and_done_once() {
        assertEquals(DailyChallenge.forDay(day), DailyChallenge.forDay(day))
        assertNotEquals(DailyChallenge.forDay(day), DailyChallenge.forDay(day + 1))
        assertTrue(DailyChallenge.forDay(-3).isNotEmpty())
        assertTrue(ChallengeStore.complete(dayMs))
        assertFalse(ChallengeStore.complete(dayMs))
        assertTrue(ChallengeStore.isDoneToday(dayMs))
        assertEquals(DustAction.Challenge.reward, StarDust.state.value.balance)
    }

    // bod 21
    @Test
    fun light_signature_is_stable_and_personal() {
        val a = LightSignature.of("hviezda-a")
        assertEquals(a, LightSignature.of("hviezda-a"))
        assertEquals(3, a.colors.size)
        assertTrue(a.rings in 2..4)
        assertTrue(a.rhythmMs.all { it in 300..1000 })
        val others = (1..20).map { LightSignature.of("hviezda-$it") }.toSet()
        assertTrue("podpisy sa majú líšiť", others.size > 15)
    }

    // bod 26
    @Test
    fun mood_waves_share_sums_to_one() {
        val list = listOf(Emotion.Joy, Emotion.Joy, Emotion.Calm, Emotion.Love)
        val shares = MoodWaves.shares(list)
        assertEquals(1f, shares.values.sum(), 0.001f)
        assertEquals(0.5f, shares.getValue(Emotion.Joy), 0.001f)
        assertEquals(Emotion.Joy, MoodWaves.dominant(list))
        assertEquals(1f, MoodWaves.shares(emptyList()).values.sum(), 0.001f)
        assertNull(MoodWaves.dominant(emptyList()))
    }
}
