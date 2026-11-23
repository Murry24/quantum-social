package app.quantumsocial.core.model

enum class BreathStep(val label: String) { In("Nádych"), Hold("Zadrž"), Out("Výdych") }

/** Dýchací rytmus: kroky s dĺžkou v sekundách, opakujú sa dookola. */
enum class BreathPattern(val label: String, val steps: List<Pair<BreathStep, Int>>) {
    Calm("Pokojný 4–4", listOf(BreathStep.In to 4, BreathStep.Out to 4)),
    Relax("Upokojenie 4–7–8", listOf(BreathStep.In to 4, BreathStep.Hold to 7, BreathStep.Out to 8)),
    Box("Štvorec 4–4–4–4", listOf(BreathStep.In to 4, BreathStep.Hold to 4, BreathStep.Out to 4, BreathStep.Hold to 4)),
    ;

    /** Krok v danej sekunde a veľkosť kruhu 0..1 (nádych rastie, výdych klesá, zadržanie drží). */
    fun at(seconds: Float): Pair<BreathStep, Float> {
        val cycle = steps.sumOf { it.second }
        var t = ((seconds % cycle) + cycle) % cycle
        var size = 0f
        for ((step, len) in steps) {
            if (t < len) {
                val f = t / len
                return step to
                    when (step) {
                        BreathStep.In -> f
                        BreathStep.Out -> 1f - f
                        BreathStep.Hold -> size
                    }
            }
            size = if (step == BreathStep.In) 1f else if (step == BreathStep.Out) 0f else size
            t -= len
        }
        return steps.last().first to size
    }
}

/** Dni, keď som dokončil ticho – kvôli sérii. */
data class ZenDays(val days: Set<Long> = emptySet()) {
    fun add(day: Long) = copy(days = days + day)

    fun streak(today: Long): Int {
        var d = if (today in days) today else today - 1
        var n = 0
        while (d in days) {
            n++
            d--
        }
        return n
    }

    fun encode(): String = days.sorted().joinToString(",")

    companion object {
        val durationsMin = listOf(1, 3, 5)

        fun decode(stored: String?) = ZenDays(stored.orEmpty().split(',').mapNotNull { it.toLongOrNull() }.toSet())
    }
}
