package app.quantumsocial.core

import app.quantumsocial.core.i18n.I18n
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class I18nTest {
    @After
    fun reset() = I18n.use("sk", emptyMap())

    @Test
    fun translatesAndFillsPlaceholdersInOrder() {
        I18n.use("en", I18n.parse("# x\nAhoj %s, máš %s\tHi %s, you have %s\nSpäť\tBack\n"))
        assertEquals("Hi Ann, you have 3", I18n.tr("Ahoj %s, máš %s", "Ann", 3))
        assertEquals("Back", I18n.tr("Späť"))
        assertEquals("Nepreložené", I18n.tr("Nepreložené"))
        // samotné percento sa nesmie pokaziť
        assertEquals("50%", I18n.tr("%s%", 50))
    }

    @Test
    fun picksChosenThenPhoneThenEnglish() {
        assertEquals("sk", I18n.pick("sk", "de"))
        assertEquals("en", I18n.pick(null, "EN"))
        assertEquals("en", I18n.pick(null, "xx"))
        assertEquals("sk", I18n.pick(null, "sk"))
    }

    /** Každý slovník v aplikácii musí mať v preklade rovnako veľa %s ako v slovenčine. */
    @Test
    fun dictionariesAreValid() {
        val dir = File("../app/src/main/assets/i18n")
        assertTrue("chýba priečinok ${dir.absolutePath}", dir.isDirectory)
        I18n.available.filter { it != "sk" }.forEach { code ->
            val f = File(dir, "$code.tsv")
            assertTrue("chýba slovník $code", f.isFile)
            val dict = I18n.parse(f.readText())
            assertTrue("prázdny slovník $code", dict.size > 100)
            dict.forEach { (sk, t) ->
                assertEquals("$code: „$sk“", I18n.placeholders(sk), I18n.placeholders(t))
            }
        }
    }
}
