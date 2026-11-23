package app.quantumsocial.core

import app.quantumsocial.core.model.Badge
import app.quantumsocial.core.model.BadgeStats
import app.quantumsocial.core.model.DailyAura
import app.quantumsocial.core.model.Emotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik10Test {
    @Test
    fun auraLastsOnlyToday() {
        val a = DailyAura.of(Emotion.Love, "  dnes\nsom šťastný " + "x".repeat(80), 10)
        assertEquals(DailyAura.MAX_TEXT, a.text.length)
        assertTrue(a.isActive(10))
        assertFalse(a.isActive(11))
        assertEquals(a, DailyAura.decode(a.encode()))
        assertNull(DailyAura.decode("zle"))
    }

    @Test
    fun gratitudeStreakCountsBackFromTodayOrYesterday() {
        assertEquals(3, BadgeStats(gratitudeDays = setOf(8, 9, 10), today = 10).gratitudeStreak)
        assertEquals(2, BadgeStats(gratitudeDays = setOf(8, 9), today = 10).gratitudeStreak)
        assertEquals(0, BadgeStats(gratitudeDays = setOf(7), today = 10).gratitudeStreak)
    }

    @Test
    fun badgesFollowStats() {
        assertTrue(Badge.earnedBy(BadgeStats()).isEmpty())
        val s = BadgeStats(lifetimeDust = 150, gratitudeDays = setOf(8, 9, 10), seeds = 3, today = 10)
        assertEquals(
            listOf(Badge.FirstLight, Badge.Thankful, Badge.Gardener, Badge.Shining),
            Badge.earnedBy(s),
        )
    }
}
