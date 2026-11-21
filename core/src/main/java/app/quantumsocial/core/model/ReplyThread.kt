package app.quantumsocial.core.model

/** Odpoveď svetlom (bod 7): buď krátky text, alebo jedna emócia. */
data class Reply(
    val text: String? = null,
    val emotion: Emotion? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    init {
        require((text == null) != (emotion == null)) { "odpoveď je text alebo emócia" }
    }

    companion object {
        /** @return odpoveď, alebo null ak je text prázdny alebo dlhší ako [ReplyThread.MAX_TEXT] */
        fun text(
            text: String,
            now: Long = System.currentTimeMillis(),
        ): Reply? {
            val clean = text.trim()
            return if (clean.isEmpty() || clean.length > ReplyThread.MAX_TEXT) {
                null
            } else {
                Reply(
                    text = clean,
                    createdAt = now,
                )
            }
        }

        fun emotion(
            emotion: Emotion,
            now: Long = System.currentTimeMillis(),
        ): Reply = Reply(emotion = emotion, createdAt = now)
    }
}

/** Krátke vlákno odpovedí na signál – max 3 odpovede, zhasne spolu s pôvodným signálom. */
data class ReplyThread(
    val signalId: String,
    val replies: List<Reply> = emptyList(),
) {
    val canReply: Boolean get() = replies.size < MAX_REPLIES

    fun add(reply: Reply): ReplyThread = if (canReply) copy(replies = replies + reply) else this

    companion object {
        const val MAX_REPLIES = 3
        const val MAX_TEXT = 120

        /** Vlákno svieti presne tak ako pôvodný signál. */
        fun brightness(
            parent: Signal,
            now: Long,
            fadingSince: Long?,
        ): Float = SignalLifecycle.brightness(parent, now, fadingSince)
    }
}
