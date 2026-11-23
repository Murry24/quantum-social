package app.quantumsocial.core.data

import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.StarDustState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Môj denník vďačnosti (bod 14). Ukladanie rieši app. */
object GratitudeStore {
    private val _journal = MutableStateFlow(GratitudeJournal())
    val journal: StateFlow<GratitudeJournal> = _journal.asStateFlow()

    /** @return true, ak sa zápis uložil (a pribudol StarDust) */
    fun write(
        text: String,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        val next = _journal.value.write(text, StarDustState.epochDay(now)) ?: return false
        _journal.value = next
        StarDust.earn(DustAction.Gratitude, now)
        return true
    }

    fun restore(journal: GratitudeJournal) {
        _journal.value = journal
    }
}

/** Splnené výzvy dňa (bod 13) – pamätá si len deň posledného splnenia. */
object ChallengeStore {
    private val _doneDay = MutableStateFlow<Long?>(null)
    val doneDay: StateFlow<Long?> = _doneDay.asStateFlow()

    fun isDoneToday(now: Long = System.currentTimeMillis()): Boolean = _doneDay.value == StarDustState.epochDay(now)

    fun complete(now: Long = System.currentTimeMillis()): Boolean {
        if (isDoneToday(now)) return false
        _doneDay.value = StarDustState.epochDay(now)
        StarDust.earn(DustAction.Challenge, now)
        return true
    }

    fun restore(day: Long?) {
        _doneDay.value = day
    }
}
