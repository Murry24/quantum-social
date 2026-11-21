package app.quantumsocial.core

import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.SignalCategory
import app.quantumsocial.core.model.SignalLifecycle
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.core.model.asWishNet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WishNetTtlTest {
    @Test
    fun wishNetSignalLives24hAndSurvivesReading() {
        val emoji = EmojiSignal(emoji = "✨", id = "e", createdAt = 0L).asWishNet()
        assertEquals(SignalLifecycle.WISHNET_TTL_MS, emoji.ttlMillis)
        assertEquals(SignalCategory.WishNet, emoji.category)
        assertFalse(emoji.expireOnRead)
        assertEquals("e", emoji.id)
        assertFalse(emoji.isExpired(now = 60_000L))
    }

    @Test
    fun contentStaysTheSame() {
        val t = TextSignal(text = "ahoj").asWishNet() as TextSignal
        assertEquals("ahoj", t.text)
    }
}
