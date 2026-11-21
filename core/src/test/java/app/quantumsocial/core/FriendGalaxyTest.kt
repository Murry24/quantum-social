package app.quantumsocial.core

import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.model.BlackHoleInvite
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.EnterResult
import app.quantumsocial.core.model.FriendGalaxy
import app.quantumsocial.core.model.GalaxyRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FriendGalaxyTest {
    private val t0 = 1_000_000_000L
    private val hour = 60 * 60 * 1000L
    private val day = 24 * hour

    private fun storeWith(
        members: List<String>,
        maxUses: Int = 5,
    ): GalaxyStore =
        GalaxyStore().apply {
            put(
                FriendGalaxy("g", "Test", Emotion.Calm, members.first(), members.toSet(), t0, t0),
                BlackHoleInvite("ABC234", "g", t0, maxUses = maxUses),
            )
        }

    @Test
    fun validCodeLetsStarIn() {
        val s = storeWith(listOf("core"))
        assertEquals(EnterResult.Entered, s.enter("abc234", "me", t0 + hour))
        assertTrue("me" in s.galaxy("g")!!.members)
        assertEquals(EnterResult.AlreadyMember, s.enter("ABC234", "me", t0 + hour))
        assertEquals(EnterResult.Unknown, s.enter("ZZZZZZ", "you", t0 + hour))
    }

    @Test
    fun codeExpiresAfter24h() {
        val s = storeWith(listOf("core"))
        assertEquals(EnterResult.Expired, s.enter("ABC234", "me", t0 + 24 * hour))
    }

    @Test
    fun usedUpCodeExpires() {
        val s = storeWith(listOf("core"), maxUses = 2)
        assertEquals(EnterResult.Entered, s.enter("ABC234", "a", t0))
        assertEquals(EnterResult.Entered, s.enter("ABC234", "b", t0))
        assertEquals(EnterResult.Expired, s.enter("ABC234", "c", t0))
    }

    @Test
    fun fullGalaxyHas12Stars() {
        val s = storeWith((1..12).map { "s$it" })
        assertEquals(EnterResult.Full, s.enter("ABC234", "me", t0))
    }

    @Test
    fun closedBlackHoleBlocksEntryAndInvites() {
        val s = storeWith(listOf("core"))
        s.setOpen("g", "core", false)
        assertEquals(EnterResult.GalaxyClosed, s.enter("ABC234", "me", t0))
        assertNull(s.createInvite("g", "core", t0))
    }

    @Test
    fun onlyCoreCreatesInvite() {
        val s = storeWith(listOf("core", "other"))
        assertNull(s.createInvite("g", "other", t0))
        assertNotNull(s.createInvite("g", "core", t0))
    }

    @Test
    fun starCanBeInAtMost5GalaxiesAnd2Own() {
        val s = GalaxyStore()
        assertNotNull(s.create("Moja 1", Emotion.Joy, "me", t0))
        assertNotNull(s.create("Moja 2", Emotion.Joy, "me", t0))
        assertNull(s.create("Moja 3", Emotion.Joy, "me", t0))
        repeat(3) { i ->
            s.put(FriendGalaxy("x$i", "X", Emotion.Love, "me", setOf("me"), t0, t0))
        }
        val other = FriendGalaxy("g", "Test", Emotion.Calm, "core", setOf("core"), t0, t0)
        s.put(other, BlackHoleInvite("ABC234", "g", t0))
        assertEquals(EnterResult.TooManyGalaxies, s.enter("ABC234", "me", t0))
    }

    @Test
    fun coreLeavesOldestMemberBecomesCore() {
        val s = storeWith(listOf("core", "second", "third"))
        s.leave("g", "core")
        assertEquals("second", s.galaxy("g")!!.coreStarId)
    }

    @Test
    fun lastStarLeavingEndsGalaxy() {
        val s = storeWith(listOf("core"))
        s.leave("g", "core")
        assertNull(s.galaxy("g"))
    }

    @Test
    fun coreCanExpelButNotOthers() {
        val s = storeWith(listOf("core", "a", "b"))
        s.expel("g", "a", "b")
        assertTrue("b" in s.galaxy("g")!!.members)
        s.expel("g", "core", "b")
        assertTrue("b" !in s.galaxy("g")!!.members)
    }

    @Test
    fun galaxyFadesAfter7DaysAndSignalRelights() {
        val s = storeWith(listOf("core"))
        val g = s.galaxy("g")!!
        assertEquals(1f, g.brightness(t0 + 3 * day), 0.001f)
        assertEquals(0.5f, g.brightness(t0 + 5 * day), 0.001f)
        s.recordSignal("g", t0 + 6 * day)
        assertEquals(1f, s.galaxy("g")!!.brightness(t0 + 7 * day), 0.001f)
        s.sweep(t0 + 13 * day)
        assertNull(s.galaxy("g"))
    }

    @Test
    fun wormholesLeadToMyOtherGalaxiesNewestFirst() {
        val s = GalaxyStore()
        s.put(FriendGalaxy("a", "A", Emotion.Joy, "me", setOf("me"), t0, t0))
        s.put(FriendGalaxy("b", "B", Emotion.Joy, "x", setOf("x", "me"), t0, t0 + 2 * hour))
        s.put(FriendGalaxy("c", "C", Emotion.Joy, "x", setOf("x", "me"), t0, t0 + hour))
        s.put(FriendGalaxy("d", "D", Emotion.Joy, "x", setOf("x"), t0, t0 + 3 * hour))
        assertEquals(listOf("b", "c"), s.wormholesFrom("a", "me", t0 + 4 * hour).map { it.id })
    }

    @Test
    fun codeHasNoConfusableChars() {
        val rnd = Random(1)
        repeat(200) {
            val code = GalaxyRules.newCode(rnd)
            assertEquals(6, code.length)
            assertTrue(code, code.none { it in "0O1IL" })
        }
    }

    @Test
    fun nameIsTrimmedAndMax24() {
        assertNull(GalaxyRules.cleanName("   "))
        assertEquals(24, GalaxyRules.cleanName("x".repeat(40))!!.length)
    }

    @Test
    fun demoGalaxyAcceptsKvant7() {
        val s = GalaxyStore().apply { with(GalaxyStore) { seedDemo(t0) } }
        assertEquals(EnterResult.Entered, s.enter(GalaxyStore.DEMO_CODE, "me", t0))
        assertEquals(6, s.galaxy("demo-kamarati")!!.members.size)
    }
}
