package app.quantumsocial.core.model

import app.quantumsocial.core.i18n.tr

/** Jemné notifikácie (bod 15): poetické pripomienky namiesto červených odznakov a čísel. */
object GentleNotes {
    // hodina, kedy príde jemná pripomienka (večer, nie ráno)
    const val HOUR = 18

    val all: List<Pair<String, String>> =
        listOf(
            "✨ Iskra čaká" to "Dnešná výzva dňa ti posiela tiché svetlo.",
            "🌙 Galaxia sa stmieva" to "Máš chvíľu na minútu ticha?",
            "🌟 Zlatá iskra" to "Za čo si dnes vďačný?",
            "🌱 Tvoje semienko" to "Možno je smädné – zalej ho kvapkou svetla.",
            "💫 Pulz v diaľke" to "Niekde je hviezda, ktorej by tvoj pocit urobil radosť.",
            "🌊 Vlny nálad" to "Pozri, ako sa dnes cíti galaxia.",
            "🎧 Ozvena dňa" to "Aký zvuk mal tvoj dnešok?",
        )

    fun forDay(epochDay: Long): Pair<String, String> =
        all[Math.floorMod(epochDay, all.size.toLong()).toInt()].let { (title, body) -> tr(title) to tr(body) }
}
