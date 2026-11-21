package app.quantumsocial.core

import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.model.EmojiSignal
import app.quantumsocial.core.model.LifePhase
import app.quantumsocial.core.model.MixSignal
import app.quantumsocial.core.model.SignalLifecycle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SignalLifecycleTest {
    // reálny čas – repozitár má na pozadí vlastný ticker, ktorý beží s reálnymi hodinami
    private val t0 = System.currentTimeMillis()

    private fun emoji(ttl: Long = 30_000L) = EmojiSignal(emoji = "✨", createdAt = t0, ttlMillis = ttl)

    @Before
    fun setUp() = InMemorySignalRepository.clear()

    @Test
    fun phases_follow_remaining_time() {
        val s = emoji(ttl = 30_000L)
        assertEquals(LifePhase.Alive, SignalLifecycle.phase(s, t0 + 10_000, null))
        assertEquals(LifePhase.Expiring, SignalLifecycle.phase(s, t0 + 26_000, null))
        assertEquals(LifePhase.FadingOut, SignalLifecycle.phase(s, t0 + 30_000, null))
        assertEquals(LifePhase.FadingOut, SignalLifecycle.phase(s, t0 + 1_000, fadingSince = t0))
    }

    @Test
    fun brightness_dims_while_expiring_and_reaches_zero_after_fade() {
        val s = emoji(ttl = 30_000L)
        assertEquals(1f, SignalLifecycle.brightness(s, t0 + 10_000, null), 0.001f)
        val nearEnd = SignalLifecycle.brightness(s, t0 + 29_000, null)
        assertTrue(nearEnd in 0.35f..0.6f)
        assertEquals(0f, SignalLifecycle.brightness(s, t0 + 30_000 + SignalLifecycle.FADE_OUT_MS, null), 0.001f)
    }

    @Test
    fun signal_without_ttl_never_dims() {
        val s = MixSignal(parts = emptyList(), createdAt = t0, ttlMillis = null)
        assertEquals(LifePhase.Alive, SignalLifecycle.phase(s, t0 + 999_999_999, null))
        assertEquals(1f, SignalLifecycle.brightness(s, t0 + 999_999_999, null), 0.001f)
    }

    @Test
    fun expired_signal_fades_first_then_disappears() {
        val s = emoji(ttl = 10_000L)
        InMemorySignalRepository.send(s)

        InMemorySignalRepository.sweep(t0 + 10_100)
        assertTrue(InMemorySignalRepository.signals.value.any { it.id == s.id })
        assertEquals(t0 + 10_000, InMemorySignalRepository.fadingSince.value[s.id])

        InMemorySignalRepository.sweep(t0 + 10_000 + SignalLifecycle.FADE_OUT_MS)
        assertFalse(InMemorySignalRepository.signals.value.any { it.id == s.id })
        assertNull(InMemorySignalRepository.fadingSince.value[s.id])
    }

    @Test
    fun read_signal_fades_out_instead_of_vanishing_immediately() {
        val s = emoji(ttl = 60_000L)
        InMemorySignalRepository.send(s)

        InMemorySignalRepository.markRead(s.id, now = t0 + 1_000)
        assertTrue(InMemorySignalRepository.signals.value.any { it.id == s.id })
        val fadingSince = InMemorySignalRepository.fadingSince.value[s.id]
        assertEquals(LifePhase.FadingOut, SignalLifecycle.phase(s, t0 + 1_100, fadingSince))

        InMemorySignalRepository.sweep(t0 + 1_000 + SignalLifecycle.FADE_OUT_MS)
        assertFalse(InMemorySignalRepository.signals.value.any { it.id == s.id })
    }

    @Test
    fun wishnet_signal_is_not_removed_on_read() {
        val s = MixSignal(parts = emptyList(), createdAt = t0)
        InMemorySignalRepository.send(s)
        InMemorySignalRepository.markRead(s.id, now = t0 + 1_000)
        InMemorySignalRepository.sweep(t0 + 10_000)
        assertTrue(InMemorySignalRepository.signals.value.any { it.id == s.id })
    }
}
