package app.quantumsocial.core.data

import app.quantumsocial.core.model.ChaosPicker
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.PulseSignal
import app.quantumsocial.core.model.SignalTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Odoslané pocitové pulzy (bod 8). Zatiaľ lokálne – neskôr (bod 9) ich odošle server.
 *
 * Signál „do chaosu“ dostane pri odoslaní konkrétnu náhodnú hviezdu, aby UI vedelo,
 * kam má pulz letieť.
 */
object PulseOutbox {
    private const val MAX_HISTORY = 50

    data class Sent(
        val signal: PulseSignal,
        // hviezda, ku ktorej pulz letí (pri chaose náhodne vybraná)
        val resolvedStarId: String,
    )

    private val _sent = MutableStateFlow<List<Sent>>(emptyList())
    val sent: StateFlow<List<Sent>> = _sent.asStateFlow()

    /**
     * @param visibleStarIds hviezdy, z ktorých sa vyberá pri [SignalTarget.Chaos]
     * @return odoslaný pulz, alebo null, ak pri chaose nebola žiadna hviezda
     */
    fun send(
        target: SignalTarget,
        emotion: Emotion,
        visibleStarIds: List<String>,
        now: Long = System.currentTimeMillis(),
        random: Random = Random.Default,
    ): Sent? {
        val starId =
            when (target) {
                is SignalTarget.Star -> target.starId
                SignalTarget.Chaos -> ChaosPicker.pick(visibleStarIds, random = random) ?: return null
            }
        val sent =
            Sent(
                signal = PulseSignal(emotion = emotion, target = target, createdAt = now),
                resolvedStarId = starId,
            )
        _sent.value = (listOf(sent) + _sent.value).take(MAX_HISTORY)
        return sent
    }

    fun clear() {
        _sent.value = emptyList()
    }
}
