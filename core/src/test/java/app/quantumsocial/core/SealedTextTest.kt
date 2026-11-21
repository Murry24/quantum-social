package app.quantumsocial.core

import app.quantumsocial.core.security.SealedText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SealedTextTest {
    private val sample = SealedText(iv = ByteArray(12) { it.toByte() }, cipher = byteArrayOf(9, 8, 7, 6, 5))

    @Test
    fun encode_then_decode_gives_same_bytes() {
        assertEquals(sample, SealedText.decode(sample.encode()))
    }

    @Test
    fun encoded_form_has_version_and_no_plain_words() {
        val stored = sample.encode()
        assertTrue(stored.startsWith("v1:"))
        assertEquals(3, stored.split(":").size)
    }

    @Test
    fun broken_or_unknown_input_is_rejected() {
        assertNull(SealedText.decode(""))
        assertNull(SealedText.decode("iskra hviezda kométa"))
        assertNull(SealedText.decode("v2:AAAA:BBBB"))
        assertNull(SealedText.decode("v1:%%%:BBBB"))
        assertNull(SealedText.decode("v1::BBBB"))
    }
}
