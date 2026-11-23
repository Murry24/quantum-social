package app.quantumsocial.core

import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.ImageSignal
import app.quantumsocial.core.model.SignalLifecycle
import app.quantumsocial.core.model.TextSignal
import app.quantumsocial.core.model.WishBoard
import app.quantumsocial.core.model.WishNetVault
import app.quantumsocial.core.model.WishTheme
import app.quantumsocial.core.model.asWishNet
import org.junit.Assert.assertEquals
import org.junit.Test

class Balik12Test {
    @Test
    fun wishesSurviveAndExpire() {
        val now = 1_000_000L
        val text = TextSignal("ahoj\tsvet\npriania \\ ✨", id = "t", createdAt = now).asWishNet()
        val old = EmojiSignal("🌟", id = "old", createdAt = now - SignalLifecycle.WISHNET_TTL_MS - 1).asWishNet()
        val ephemeral = TextSignal("len 30 s", id = "e", createdAt = now)
        val back = WishNetVault.decode(WishNetVault.encode(listOf(text, old, ephemeral)) + "\nZLE", now)
        assertEquals(listOf(text), back)
    }

    @Test
    fun onlyCopiedImagesAreKept() {
        val now = 1_000_000L
        val copy = ImageSignal("file:///data/wish_images/a.img", id = "i", createdAt = now).asWishNet()
        val gallery = ImageSignal("content://media/1", id = "g", createdAt = now).asWishNet()
        assertEquals(listOf(copy), WishNetVault.decode(WishNetVault.encode(listOf(copy, gallery)), now))
    }

    @Test
    fun boardRoundTripAndPrune() {
        val b = WishBoard().post("a", WishTheme.Dreams).post("b", null).toggleCheer("x").fulfill("a")
        val back = WishBoard.decode(b.encode())
        assertEquals(b, back)
        assertEquals(setOf("a"), back.keepOnly(setOf("a")).mine)
    }
}
