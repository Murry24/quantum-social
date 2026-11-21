package app.quantumsocial.core

import app.quantumsocial.core.model.DreamMirror
import app.quantumsocial.core.model.GalacticMoments
import app.quantumsocial.core.model.StarLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testy bodov 12 a 17. */
class Balik2Test {
    @Test
    fun level_up_only_when_crossing_threshold() {
        assertEquals(StarLevel.Star, GalacticMoments.levelUp(19, 20))
        assertNull(GalacticMoments.levelUp(20, 25))
        assertNull(GalacticMoments.levelUp(25, 10))
        assertEquals(StarLevel.Pulsar, GalacticMoments.levelUp(10, 70))
    }

    @Test
    fun emotion_of_day_is_stable() {
        assertEquals(GalacticMoments.emotionOfDay(20_000), GalacticMoments.emotionOfDay(20_000))
        val week = (0L until 7L).map { GalacticMoments.emotionOfDay(20_000 + it) }.toSet()
        assertTrue(week.size > 1)
    }

    @Test
    fun dream_words_ignore_diacritics_and_short_words() {
        assertEquals(setOf("lieta", "mesto"), DreamMirror.words("Lietal by som nad mestom"))
    }

    @Test
    fun dream_bridge_finds_similar_flying_dream() {
        val (match, score) = DreamMirror.bridge("Snívalo sa mi, že som lietal nad lesom s vtákmi")!!
        assertTrue(match.contains("vtákmi"))
        assertTrue(score in 0f..1f)
        assertNull(DreamMirror.bridge("ok"))
    }
}
