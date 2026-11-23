package app.quantumsocial.core

import app.quantumsocial.core.data.GalaxyStore
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.GalaxyCampfire
import app.quantumsocial.core.model.GalaxyRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Balik9Test {
    @Test
    fun onlyCoreCanEditGalaxy() {
        val store = GalaxyStore()
        val g = store.create("Kamoši", Emotion.Joy, "ja", now = 0)!!
        assertFalse(store.edit(g.id, "cudzi", "X", "", Emotion.Calm))
        assertFalse(store.edit(g.id, "ja", "   ", "", Emotion.Calm))
        assertTrue(store.edit(g.id, "ja", "Partia", "  spolu\nsvietime  ", Emotion.Calm))
        val e = store.galaxy(g.id)!!
        assertEquals("Partia", e.name)
        assertEquals("spolu svietime", e.motto)
        assertEquals(Emotion.Calm, e.emotion)
    }

    @Test
    fun mottoIsCut() {
        assertEquals(GalaxyRules.MAX_MOTTO, GalaxyRules.cleanMotto("a".repeat(200)).length)
    }

    @Test
    fun favoritesToggle() {
        val store = GalaxyStore()
        store.toggleFavorite("g")
        assertEquals(setOf("g"), store.favorites.value)
        store.toggleFavorite("g")
        assertTrue(store.favorites.value.isEmpty())
    }

    @Test
    fun campfireQuestionStableForDayAndChanges() {
        val a = GalaxyCampfire.questionFor("g1", 100)
        assertEquals(a, GalaxyCampfire.questionFor("g1", 100))
        val week = (100L..106L).map { GalaxyCampfire.questionFor("g1", it) }.toSet()
        assertEquals(7, week.size)
    }
}
