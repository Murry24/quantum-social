package app.quantumsocial.core.data

import app.quantumsocial.core.model.DailyAura
import app.quantumsocial.core.model.DustAction
import app.quantumsocial.core.model.Echo
import app.quantumsocial.core.model.EchoDay
import app.quantumsocial.core.model.Emotion
import app.quantumsocial.core.model.GratitudeJournal
import app.quantumsocial.core.model.Moderation
import app.quantumsocial.core.model.ReportReason
import app.quantumsocial.core.model.SeedGarden
import app.quantumsocial.core.model.SoulLinks
import app.quantumsocial.core.model.StarDustState
import app.quantumsocial.core.model.WishBoard
import app.quantumsocial.core.model.WishTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

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

/** Moja záhrada semienok budúcnosti (bod 30). Ukladanie rieši app. */
object SeedStore {
    private val _garden = MutableStateFlow(SeedGarden())
    val garden: StateFlow<SeedGarden> = _garden.asStateFlow()

    fun plant(
        text: String,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        val id = UUID.randomUUID().toString()
        val next = _garden.value.plant(id, text, StarDustState.epochDay(now)) ?: return false
        _garden.value = next
        return true
    }

    /** Zaliatie – raz denne na semienko, každé prinesie trochu StarDust. */
    fun water(
        id: String,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        val next = _garden.value.water(id, StarDustState.epochDay(now)) ?: return false
        _garden.value = next
        StarDust.earn(DustAction.Seed, now)
        return true
    }

    fun restore(garden: SeedGarden) {
        _garden.value = garden
    }
}

/** Moje dnešné ozveny (bod 11). Súbory a ukladanie rieši app. */
object EchoStore {
    private val _day = MutableStateFlow(EchoDay())
    val day: StateFlow<EchoDay> = _day.asStateFlow()

    fun add(echo: Echo): Boolean {
        val next = _day.value.add(echo) ?: return false
        _day.value = next
        StarDust.earn(DustAction.Echo)
        return true
    }

    fun restore(day: EchoDay) {
        _day.value = day
    }
}

/** Moje spojenia (body 22, 23): SoulLinks a súhvezdia, do ktorých som vstúpil. Ukladanie rieši app. */
object ConnectionStore {
    private val _links = MutableStateFlow(SoulLinks())
    val links: StateFlow<SoulLinks> = _links.asStateFlow()

    private val _joined = MutableStateFlow<Set<String>>(emptySet())
    val joined: StateFlow<Set<String>> = _joined.asStateFlow()

    fun requestLink(
        starId: String,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        val next = _links.value.request(starId, StarDustState.epochDay(now)) ?: return false
        _links.value = next
        return true
    }

    fun removeLink(starId: String) {
        _links.value = _links.value.remove(starId)
    }

    /** Ukážkové hviezdy odpovedajú na druhý deň. */
    fun refresh(now: Long = System.currentTimeMillis()) {
        _links.value = _links.value.demoAcceptances(StarDustState.epochDay(now))
    }

    fun toggleConstellation(id: String) {
        _joined.value = if (id in _joined.value) _joined.value - id else _joined.value + id
    }

    fun restore(
        links: SoulLinks,
        joined: Set<String>,
    ) {
        _links.value = links
        _joined.value = joined
    }
}

/** Bod 19 (lokálne): koho som zablokoval a nahlásil. Ukladanie rieši app. */
object ModerationStore {
    private val _state = MutableStateFlow(Moderation())
    val state: StateFlow<Moderation> = _state.asStateFlow()

    fun block(starId: String) {
        _state.value = _state.value.block(starId)
    }

    fun unblock(starId: String) {
        _state.value = _state.value.unblock(starId)
    }

    /** @return false, ak dnes už nahlásiť nemožno */
    fun report(
        starId: String,
        reason: ReportReason,
        now: Long = System.currentTimeMillis(),
    ): Boolean {
        val next = _state.value.report(starId, reason, StarDustState.epochDay(now)) ?: return false
        _state.value = next
        return true
    }

    fun restore(moderation: Moderation) {
        _state.value = moderation
    }
}

/** WishNet+ (témy, palce, splnené priania). */
object WishBoardStore {
    private val _board = MutableStateFlow(WishBoard())
    val board: StateFlow<WishBoard> = _board.asStateFlow()

    fun post(
        wishId: String,
        theme: WishTheme?,
    ) {
        _board.value = _board.value.post(wishId, theme)
    }

    fun toggleCheer(wishId: String) {
        _board.value = _board.value.toggleCheer(wishId)
    }

    fun fulfill(wishId: String) {
        _board.value = _board.value.fulfill(wishId)
    }

    fun restore(board: WishBoard) {
        _board.value = board
    }
}

/** Moja dnešná aura (profil). Ukladanie rieši app. */
object AuraStore {
    private val _aura = MutableStateFlow<DailyAura?>(null)
    val aura: StateFlow<DailyAura?> = _aura.asStateFlow()

    fun set(
        emotion: Emotion,
        text: String,
        now: Long = System.currentTimeMillis(),
    ) {
        _aura.value = DailyAura.of(emotion, text, StarDustState.epochDay(now))
    }

    fun clear() {
        _aura.value = null
    }

    fun restore(aura: DailyAura?) {
        _aura.value = aura
    }
}
