package app.quantumsocial.core.model

import app.quantumsocial.core.i18n.tr

/** ChallengeNet (bod 13): jedna jemná výzva dňa – rovnaká pre všetkých v ten istý deň. */
object DailyChallenge {
    val all: List<String> =
        listOf(
            "Pošli pulz Radosti hviezde, ktorú nepoznáš.",
            "Nájdi dnes niečo modré a usmej sa naň.",
            "Daj si minútu ticha v ZenNet.",
            "Napíš jednu vec, za ktorú si dnes vďačný.",
            "Odpovedz svetlom na cudzie prianie.",
            "Pozri sa 10 sekúnd na oblohu.",
            "Pošli pocit do chaosu – nech niekoho prekvapí.",
            "Pochváľ dnes niekoho naozaj, nie len tak.",
            "Vypni na 15 minút všetky notifikácie.",
            "Prejdi sa a všímaj si zvuky okolo.",
            "Napíš do WishNet jedno malé prianie.",
            "Daj si pohár vody pomaly, dúšok po dúšku.",
            "Spomeň si na niekoho, kto ti pomohol.",
            "Usmej sa na seba do zrkadla. Naozaj.",
        )

    fun forDay(epochDay: Long): String = tr(all[Math.floorMod(epochDay, all.size.toLong()).toInt()])
}
