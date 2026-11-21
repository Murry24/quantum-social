package app.quantumsocial.core

import app.quantumsocial.core.data.PulseOutbox
import app.quantumsocial.core.data.ReplyStore
import app.quantumsocial.core.data.StarDust
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.SignalTarget
import app.quantumsocial.core.model.StarDustState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.ZoneOffset

class StarDustTest {
    private val day = 20_000L

    @Before
    fun setUp() {
        StarDust.clear()
        PulseOutbox.clear()
        ReplyStore.clear()
    }

    @Test
    fun earning_adds_reward_until_daily_cap() {
        var s = StarDustState()
        repeat(DustAction.Wish.dailyCap + 2) { s = s.earn(DustAction.Wish, day).first }
        assertEquals(DustAction.Wish.reward * DustAction.Wish.dailyCap, s.balance)
        assertEquals(0, s.earn(DustAction.Wish, day).second)
    }

    @Test
    fun new_day_resets_caps_but_keeps_balance() {
        var s = StarDustState()
        repeat(DustAction.Galaxy.dailyCap) { s = s.earn(DustAction.Galaxy, day).first }
        val (next, gained) = s.earn(DustAction.Galaxy, day + 1)
        assertEquals(DustAction.Galaxy.reward, gained)
        assertEquals(2 * DustAction.Galaxy.reward, next.balance)
        assertEquals(1, next.countToday(DustAction.Galaxy))
    }

    @Test
    fun encode_decode_roundtrip_and_garbage_is_empty() {
        val s = StarDustState().earn(DustAction.Pulse, day).first.earn(DustAction.Zen, day).first
        assertEquals(s, StarDustState.decode(s.encode()))
        assertEquals(StarDustState(), StarDustState.decode(null))
        assertEquals(StarDustState(), StarDustState.decode("nezmysel"))
        assertEquals(StarDustState(), StarDustState.decode("x|y|z"))
    }

    @Test
    fun epoch_day_uses_zone() {
        assertEquals(0L, StarDustState.epochDay(0L, ZoneOffset.UTC))
        assertEquals(1L, StarDustState.epochDay(86_400_000L, ZoneOffset.UTC))
    }

    @Test
    fun sending_pulse_and_reply_earns_dust() {
        PulseOutbox.send(SignalTarget.Star("a"), Emotion.Joy, visibleStarIds = emptyList())
        ReplyStore.reply("s1", Reply.emotion(Emotion.Calm))
        // prvá odpoveď prianiu má aj bonus za empatiu (bod 29)
        val expected = DustAction.Pulse.reward + DustAction.Reply.reward + DustAction.Empathy.reward
        assertEquals(expected, StarDust.state.value.balance)
    }
}
