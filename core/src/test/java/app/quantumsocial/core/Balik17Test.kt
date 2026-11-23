package app.quantumsocial.core

import app.quantumsocial.core.model.DreamCircles
import app.quantumsocial.core.model.DreamCirclesState
import app.quantumsocial.core.model.DreamNoteKind
import app.quantumsocial.core.model.DreamSymbol
import app.quantumsocial.core.model.Emotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik17Test {
    @Test
    fun circleLivesThreeDaysInWindow() {
        val s = DreamCirclesState().join(DreamSymbol.Flying, 10)
        val c = s.circles.single()
        assertEquals(9L, c.window)
        assertTrue(c.isAlive(11))
        assertFalse(c.isAlive(12))
        assertEquals(s, s.join(DreamSymbol.Flying, 11))
        assertTrue(s.alive(12).circles.isEmpty())
    }

    @Test
    fun notesAreAnonymousStableAndMine() {
        val s = DreamCirclesState().join(DreamSymbol.Water, 9)
        val key = s.circles.single().key
        assertNull(s.post(key, "x", DreamNoteKind.Meaning, null, "   ", 9))
        val p = s.post(key, "m1", DreamNoteKind.Path, Emotion.Calm, "  idem\tďalej ", 10)!!
        val notes = DreamCircles.notes(p.circles.single(), 10)
        assertEquals(notes, DreamCircles.notes(p.circles.single(), 10))
        assertEquals(1, notes.count { it.author == 0 })
        assertEquals("idem ďalej", notes.first { it.author == 0 }.text)
        val members = DreamCircles.others(DreamSymbol.Water, 9)
        assertTrue(notes.filter { it.author != 0 }.all { it.author in 1..members })
        assertTrue(DreamCircles.sharedThread(notes) != null)
    }

    @Test
    fun resonanceAndRoundTrip() {
        var s = DreamCirclesState().join(DreamSymbol.Key, 9)
        val c = s.circles.single()
        val note = DreamCircles.othersNotes(c, 9).first()
        val before = DreamCircles.resonance(c, note)
        s = s.toggleResonance(c.key, note.id).post(c.key, "m", DreamNoteKind.Feeling, null, "ľahkosť", 9)!!
        assertEquals(before + 1, DreamCircles.resonance(s.circles.single(), note))
        assertEquals(s, DreamCirclesState.decode(s.encode()))
    }
}
