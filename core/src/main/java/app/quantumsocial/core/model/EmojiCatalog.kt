package app.quantumsocial.core.model

/** Emoji na posielanie (WishNet, doplnok k pulzu, odpovede) – v skupinách, aby sa dali rýchlo nájsť. */
object EmojiCatalog {
    data class Group(
        val title: String,
        val emojis: List<String>,
    )

    val groups: List<Group> =
        listOf(
            Group("Pocity", listOf("😊", "🥹", "😌", "🥰", "😢", "😴", "🤗", "😮")),
            Group("Srdcia", listOf("💛", "💙", "💜", "🤍", "🩷", "❤️‍🔥", "💗", "💞")),
            Group("Vesmír", listOf("✨", "🌟", "💫", "🌙", "☀️", "🌈", "🪐", "☄️")),
            Group("Príroda", listOf("🌸", "🌿", "🍃", "🌊", "🔥", "❄️", "🌻", "🦋")),
            Group("Gestá", listOf("🙏", "👋", "🤝", "✌️", "🫶", "👏", "💪", "🙌")),
        )

    val all: List<String> get() = groups.flatMap { it.emojis }

    /** Len emoji z katalógu – nie ľubovoľný text (pulz je bez slov). */
    fun isAllowed(emoji: String): Boolean = emoji in all
}

/** Svetelný efekt, ktorý môže pulz niesť so sebou. */
enum class PulseEffect { Sparks, Rainbow, Wave, Flash }
