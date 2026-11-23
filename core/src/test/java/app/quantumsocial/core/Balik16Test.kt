package app.quantumsocial.core

import app.quantumsocial.core.model.DreamMirror
import app.quantumsocial.core.model.DreamSymbol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik16Test {
    @Test
    fun symbolsBuildBridgeInAnyLanguage() {
        val b = DreamMirror.bridge("I was in an exam", setOf(DreamSymbol.Exam))!!
        assertEquals(DreamMirror.otherDreams.last(), b.first)
        assertEquals(1f, b.second, 0.001f)
        assertNull(DreamMirror.bridge("xyz", emptySet()))
    }

    @Test
    fun galaxyTodayIsStableTopThree() {
        val a = DreamMirror.galaxyToday(100)
        assertEquals(3, a.size)
        assertEquals(a, DreamMirror.galaxyToday(100))
        assertTrue(a[0].second >= a[2].second)
    }
}
