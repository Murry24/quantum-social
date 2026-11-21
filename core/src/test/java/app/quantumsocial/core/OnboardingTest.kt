package app.quantumsocial.core

import app.quantumsocial.core.model.OnboardingState
import app.quantumsocial.core.model.OnboardingStep
import app.quantumsocial.core.model.RecoveryKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingTest {
    @Test
    fun stepsGoInOrderAndFinish() {
        var s = OnboardingState()
        val seen = mutableListOf(s.step)
        while (!s.done) {
            if (s.step == OnboardingStep.RecoveryKey) s = s.confirmKey(true)
            s = s.next()
            if (!s.done) seen += s.step
        }
        assertEquals(OnboardingStep.values().toList(), seen)
    }

    @Test
    fun recoveryKeyCannotBeSkippedNorPassedWithoutConfirmation() {
        val atKey = OnboardingState(step = OnboardingStep.RecoveryKey)
        assertFalse(atKey.canSkip)
        assertEquals(atKey, atKey.skip())
        assertEquals(atKey, atKey.next())
        assertEquals(OnboardingStep.Notifications, atKey.confirmKey(true).next().step)
    }

    @Test
    fun otherStepsCanBeSkipped() {
        val s = OnboardingState(step = OnboardingStep.Language)
        assertTrue(s.canSkip)
        assertEquals(OnboardingStep.Emotion, s.skip().step)
        assertTrue(OnboardingState(step = OnboardingStep.Tutorial).skip().done)
    }

    @Test
    fun recoveryKeyHasEightWordsFromList() {
        repeat(20) {
            val key = RecoveryKey.generate()
            assertEquals(8, key.size)
            assertTrue(key.all { it in RecoveryKey.words })
            assertEquals("slová sa neopakujú", 8, key.toSet().size)
        }
    }
}
