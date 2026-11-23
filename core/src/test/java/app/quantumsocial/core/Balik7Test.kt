package app.quantumsocial.core

import app.quantumsocial.core.model.LinguaBridges
import app.quantumsocial.core.model.Moderation
import app.quantumsocial.core.model.ReportReason
import app.quantumsocial.core.model.WorldLanguages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testy bodov 19 (lokálna časť) a 25. */
class Balik7Test {
    private val day = 20_000L

    // bod 19
    @Test
    fun report_blocks_and_has_daily_limits() {
        var m = Moderation().report("a", ReportReason.Spam, day)!!
        assertTrue(m.isBlocked("a"))
        assertNull(m.report("a", ReportReason.Hate, day))
        for (i in 1 until Moderation.MAX_REPORTS_PER_DAY) m = m.report("s$i", ReportReason.Other, day)!!
        assertNull(m.report("posledná", ReportReason.Other, day))
        assertTrue(m.report("posledná", ReportReason.Other, day + 1) != null)
    }

    @Test
    fun block_unblock_filter_and_roundtrip() {
        val m = Moderation().block("x").block("y").unblock("y")
        assertEquals(listOf("a", "b"), m.visible(listOf("a", "x", "b")) { it })
        assertFalse(m.isBlocked("y"))
        val withReport = m.report("z", ReportReason.SelfHarm, day)!!
        assertEquals(withReport, Moderation.decode(withReport.encode()))
        assertEquals(Moderation(), Moderation.decode(null))
    }

    // bod 25
    @Test
    fun lingua_codes_and_offer_rules() {
        assertEquals("cs", LinguaBridges.toIso("CZ"))
        assertEquals("sk", LinguaBridges.toIso("SK"))
        assertTrue(LinguaBridges.canTranslate("JA"))
        assertFalse(LinguaBridges.canTranslate("QU"))
        assertTrue(LinguaBridges.shouldOffer("en", "SK"))
        assertFalse(LinguaBridges.shouldOffer("sk", "SK"))
        assertFalse(LinguaBridges.shouldOffer(null, "SK"))
        assertFalse(LinguaBridges.shouldOffer("en", "QU"))
        // väčšina jazykov sveta z aplikácie sa dá preložiť
        assertTrue(WorldLanguages.codes.count(LinguaBridges::canTranslate) >= 45)
    }
}
