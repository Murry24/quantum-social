package app.quantumsocial.core

import app.quantumsocial.core.model.Constellation
import app.quantumsocial.core.model.DemoStars
import app.quantumsocial.core.model.LinkStatus
import app.quantumsocial.core.model.Serendipity
import app.quantumsocial.core.model.SoulLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testy bodov 10, 22 a 23. */
class Balik6Test {
    private val day = 20_000L

    // bod 10
    @Test
    fun serendipity_is_daily_stable_never_me() {
        val a = Serendipity.matchFor("ja", day)
        assertEquals(a, Serendipity.matchFor("ja", day))
        assertNotEquals("ja", a)
        val week = (0L until 7L).mapNotNull { Serendipity.matchFor("ja", day + it) }.toSet()
        assertTrue(week.size > 1)
        assertNull(Serendipity.matchFor("ja", day, listOf("ja")))
    }

    // bod 22
    @Test
    fun constellations_of_day_live_three_days_and_max_ten() {
        val list = Constellation.ofDay(day)
        assertEquals(3, list.size)
        assertEquals(list, Constellation.ofDay(day))
        val c = list.first()
        assertTrue(c.isAlive(day + 2))
        assertTrue(!c.isAlive(day + 3))
        assertEquals(3L, c.daysLeft(day))
        var full = c
        var i = 0
        while (!full.isFull) full = full.join("x${i++}")!!
        assertEquals(Constellation.MAX_MEMBERS, full.members.size)
        assertNull(full.join("ďalší"))
        assertNull(c.join(c.members.first()))
        assertEquals(c.members.size - 1, c.leave(c.members.first()).members.size)
    }

    // bod 23
    @Test
    fun soul_links_need_consent_and_max_five() {
        var s = SoulLinks()
        for (i in 1..5) s = s.request(DemoStars.ids[i], day)!!
        assertNull(s.request("šiesta", day))
        assertNull(s.request(DemoStars.ids[1], day))
        assertTrue(s.links.all { it.status == LinkStatus.Pending })
        assertTrue(s.demoAcceptances(day).links.all { it.status == LinkStatus.Pending })
        s = s.demoAcceptances(day + 1)
        assertTrue(s.links.all { it.status == LinkStatus.Active })
        assertNotNull(s.request("nová", day + 1)?.let { null } ?: s.remove(DemoStars.ids[1]).request("nová", day + 1))
        assertEquals(s, SoulLinks.decode(s.encode()))
    }
}
