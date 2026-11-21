package app.quantumsocial.core

import app.quantumsocial.core.data.InMemorySignalRepository
import app.quantumsocial.core.data.ReplyStore
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.Reply
import app.quantumsocial.core.model.ReplyThread
import app.quantumsocial.core.model.SignalLifecycle
import app.quantumsocial.core.model.TextSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReplyThreadTest {
    private val t0 = 1_000_000L

    @Before
    fun setUp() {
        InMemorySignalRepository.clear()
        ReplyStore.clear()
    }

    @Test
    fun atMostThreeReplies() {
        repeat(3) { assertTrue(ReplyStore.reply("s", Reply.emotion(Emotion.Joy, t0))) }
        assertFalse(ReplyStore.reply("s", Reply.emotion(Emotion.Calm, t0)))
        assertEquals(3, ReplyStore.threads.value["s"]!!.replies.size)
        assertFalse(ReplyThread("s", List(3) { Reply.emotion(Emotion.Joy) }).canReply)
    }

    @Test
    fun textMax120CharsAndNotEmpty() {
        assertNotNull(Reply.text("x".repeat(120)))
        assertNull(Reply.text("x".repeat(121)))
        assertNull(Reply.text("   "))
        assertEquals("ahoj", Reply.text("  ahoj ")!!.text)
    }

    @Test(expected = IllegalArgumentException::class)
    fun replyIsTextOrEmotionNotBoth() {
        Reply(text = "a", emotion = Emotion.Joy)
    }

    @Test
    fun threadFadesAndDisappearsWithParent() {
        val parent = TextSignal(text = "ahoj", createdAt = t0, ttlMillis = 10_000)
        // odpoveď ešte pred odoslaním – repo na pozadí čistí podľa reálneho času a mohlo by ho zmazať skôr
        ReplyStore.reply(parent.id, Reply.emotion(Emotion.Love, t0))
        InMemorySignalRepository.send(parent)

        assertEquals(1f, ReplyThread.brightness(parent, t0 + 1_000, null), 0.001f)
        assertEquals(0f, ReplyThread.brightness(parent, t0 + 10_000 + SignalLifecycle.FADE_OUT_MS, null), 0.001f)

        InMemorySignalRepository.sweep(t0 + 10_100)
        InMemorySignalRepository.sweep(t0 + 10_000 + SignalLifecycle.FADE_OUT_MS)
        assertNull(ReplyStore.threads.value[parent.id])
    }
}
