package app.quantumsocial.core.model

/** Mood Waves (bod 26): aký pocit práve prevláda v galaxii – podiel každej emócie 0..1. */
object MoodWaves {
    fun shares(emotions: List<Emotion>): Map<Emotion, Float> {
        if (emotions.isEmpty()) return Emotion.values().associateWith { 1f / Emotion.values().size }
        val counts = emotions.groupingBy { it }.eachCount()
        return Emotion.values().associateWith { (counts[it] ?: 0).toFloat() / emotions.size }
    }

    fun dominant(emotions: List<Emotion>): Emotion? =
        emotions.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
}
