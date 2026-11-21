package app.quantumsocial.core

import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.data.RecentEmojis
import app.quantumsocial.core.model.EmojiCatalog
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.PulseEffect
import app.quantumsocial.core.model.PulseSignal
import app.quantumsocial.core.model.SignalTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EmojiTest {
    @Before
    fun setUp() {
        RecentEmojis.clear()
        PulseOutbox.clear()
    }

    @Test
    fun catalog_has_five_groups_of_eight_without_duplicates() {
        assertEquals(5, EmojiCatalog.groups.size)
        assertTrue(EmojiCatalog.groups.all { it.emojis.size == 8 })
        assertEquals(40, EmojiCatalog.all.toSet().size)
    }

    @Test
    fun recent_is_newest_first_without_repeats_and_capped() {
        val all = EmojiCatalog.all
        all.take(10).forEach(RecentEmojis::use)
        RecentEmojis.use(all[9])
        val recent = RecentEmojis.recent.value
        assertEquals(RecentEmojis.MAX, recent.size)
        assertEquals(all[9], recent.first())
        assertEquals(recent.size, recent.toSet().size)
    }

    @Test
    fun pulse_carries_emoji_and_effect() {
        val sent =
            PulseOutbox.send(
                SignalTarget.Star("a"),
                Emotion.Joy,
                visibleStarIds = emptyList(),
                emoji = "🌈",
                effect = PulseEffect.Rainbow,
            )!!
        assertEquals("🌈", sent.signal.emoji)
        assertEquals(PulseEffect.Rainbow, sent.signal.effect)
    }

    @Test
    fun pulse_without_extras_still_works() {
        val sent = PulseOutbox.send(SignalTarget.Star("a"), Emotion.Calm, visibleStarIds = emptyList())!!
        assertNull(sent.signal.emoji)
        assertNull(sent.signal.effect)
    }

    @Test(expected = IllegalArgumentException::class)
    fun pulse_rejects_text_instead_of_emoji() {
        PulseSignal(emotion = Emotion.Joy, target = SignalTarget.Chaos, emoji = "ahoj")
    }
}
