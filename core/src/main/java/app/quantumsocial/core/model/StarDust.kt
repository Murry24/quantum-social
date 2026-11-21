package app.quantumsocial.core.model

import java.time.Instant
import java.time.ZoneId

/**
 * StarDust (bod 20) – jediná mena v Quantum Social. Nedá sa kúpiť ani predať,
 * získava sa len za pozitívne činy a každý čin má denný strop (aby sa nedala „farmiť“).
 */
enum class DustAction(
    val reward: Int,
    val dailyCap: Int,
) {
    Pulse(reward = 1, dailyCap = 10),
    Reply(reward = 1, dailyCap = 10),
    Wish(reward = 2, dailyCap = 3),
    Zen(reward = 2, dailyCap = 2),
    Galaxy(reward = 5, dailyCap = 1),
    Gratitude(reward = 2, dailyCap = 1),
    Challenge(reward = 3, dailyCap = 1),
    Empathy(reward = 2, dailyCap = 5),
    Seed(reward = 1, dailyCap = 3),
}

data class StarDustState(
    val balance: Int = 0,
    // deň (epoch day), ku ktorému patria dnešné počty
    val day: Long = 0,
    val today: Map<DustAction, Int> = emptyMap(),
) {
    fun countToday(action: DustAction): Int = today[action] ?: 0

    /** @return nový stav a koľko prachu pribudlo (0, ak je dnešný strop vyčerpaný) */
    fun earn(
        action: DustAction,
        epochDay: Long,
    ): Pair<StarDustState, Int> {
        val fresh = if (epochDay != day) copy(day = epochDay, today = emptyMap()) else this
        val used = fresh.countToday(action)
        if (used >= action.dailyCap) return fresh to 0
        val next =
            fresh.copy(
                balance = fresh.balance + action.reward,
                today = fresh.today + (action to used + 1),
            )
        return next to action.reward
    }

    /** Krátky textový zápis na uloženie do nastavení. */
    fun encode(): String = "$balance|$day|" + today.entries.joinToString(",") { "${it.key.name}:${it.value}" }

    companion object {
        fun decode(stored: String?): StarDustState {
            val parts = stored?.split("|") ?: return StarDustState()
            if (parts.size != 3) return StarDustState()
            val balance = parts[0].toIntOrNull() ?: return StarDustState()
            val day = parts[1].toLongOrNull() ?: return StarDustState()
            val today =
                parts[2].split(",").filter { it.isNotBlank() }.mapNotNull { entry ->
                    val (name, count) = entry.split(":").takeIf { it.size == 2 } ?: return@mapNotNull null
                    val action = DustAction.values().firstOrNull { it.name == name } ?: return@mapNotNull null
                    count.toIntOrNull()?.let { action to it }
                }.toMap()
            return StarDustState(balance.coerceAtLeast(0), day, today)
        }

        fun epochDay(
            now: Long,
            zone: ZoneId = ZoneId.systemDefault(),
        ): Long = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().toEpochDay()
    }
}
