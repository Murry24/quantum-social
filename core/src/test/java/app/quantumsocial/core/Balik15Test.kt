package app.quantumsocial.core

import app.quantumsocial.core.model.GratitudeJournal
import org.junit.Assert.assertEquals
import org.junit.Test

class Balik15Test {
    @Test
    fun weekShowsSparkDays() {
        val j = GratitudeJournal().write("a", 4)!!.write("b", 8)!!.write("c", 10)!!
        assertEquals(listOf(true, false, false, false, true, false, true), j.week(10))
        assertEquals(List(7) { false }, GratitudeJournal().week(10))
    }
}
