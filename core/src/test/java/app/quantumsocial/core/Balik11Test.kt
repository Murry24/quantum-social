package app.quantumsocial.core

import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.EnterResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik11Test {
    @Test
    fun galaxiesSurviveRestart() {
        val now = System.currentTimeMillis()
        val a = GalaxyStore()
        val g = a.create("Partia\tna\nchate", Emotion.Love, "ja", now)!!
        a.edit(g.id, "ja", "Partia", "spolu svietime", Emotion.Calm)
        val inv = a.createInvite(g.id, "ja", now)!!
        a.toggleFavorite(g.id)

        val b = GalaxyStore()
        b.restore(a.encode() + "\nZLE\triadok")
        val r = b.galaxy(g.id)!!
        assertEquals("Partia", r.name)
        assertEquals("spolu svietime", r.motto)
        assertEquals(Emotion.Calm, r.emotion)
        assertEquals(setOf(g.id), b.favorites.value)
        assertEquals(EnterResult.Entered, b.enter(inv.code, "kamos", now))
        assertTrue("kamos" in b.galaxy(g.id)!!.members)
    }

    @Test
    fun emptyRestoreKeepsDemo() {
        val s = GalaxyStore().apply { with(GalaxyStore) { seedDemo(System.currentTimeMillis()) } }
        s.restore(null)
        assertEquals(1, s.galaxies.value.size)
    }
}
