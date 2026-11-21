package app.quantumsocial.core.model

/**
 * Memory Nebula (bod 24): súkromný archív výnimočných chvíľ. Na rozdiel od signálov nezhasína –
 * je len tvoj, zašifrovaný v telefóne (Android Keystore) a nikam sa neposiela.
 */
data class Memory(
    val id: String,
    val day: Long,
    val emotion: Emotion,
    val text: String,
)

data class MemoryNebula(
    val memories: List<Memory> = emptyList(),
) {
    fun add(memory: Memory): MemoryNebula? {
        val clean = memory.text.trim()
        if (clean.isEmpty() || clean.length > MAX_TEXT || memories.size >= MAX_MEMORIES) return null
        return copy(memories = listOf(memory.copy(text = clean)) + memories)
    }

    fun remove(id: String): MemoryNebula = copy(memories = memories.filter { it.id != id })

    fun byEmotion(emotion: Emotion?): List<Memory> =
        if (emotion == null) memories else memories.filter { it.emotion == emotion }

    /** Spomienka „v tento deň“ pred rokom a viac (pre jemné pripomenutie). */
    fun onThisDay(
        today: Long,
        yearDays: Long = 365,
    ): List<Memory> = memories.filter { today - it.day >= yearDays && (today - it.day) % yearDays == 0L }

    /** Zápis pred zašifrovaním – každý riadok jedna spomienka, text na konci (môže mať tabulátor nahradený). */
    fun encode(): String =
        memories.joinToString("\n") { m ->
            listOf(m.id, m.day, m.emotion.name, m.text.replace("\n", " ").replace("\t", " ")).joinToString("\t")
        }

    companion object {
        const val MAX_TEXT = 500
        const val MAX_MEMORIES = 200

        fun decode(stored: String?): MemoryNebula =
            MemoryNebula(
                stored.orEmpty().lines().mapNotNull { line ->
                    val p = line.split("\t", limit = 4)
                    if (p.size != 4) return@mapNotNull null
                    Memory(
                        id = p[0],
                        day = p[1].toLongOrNull() ?: return@mapNotNull null,
                        emotion = Emotion.values().firstOrNull { it.name == p[2] } ?: return@mapNotNull null,
                        text = p[3],
                    )
                },
            )
    }
}
