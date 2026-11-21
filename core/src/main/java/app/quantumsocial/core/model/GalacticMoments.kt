package app.quantumsocial.core.model

/** Galactic Moments (bod 17): spoločné chvíle galaxie – nový level hviezdy a emócia dňa. */
object GalacticMoments {
    /** @return nový level, ak práve teraz hviezda preskočila na vyšší; inak null */
    fun levelUp(
        dustBefore: Int,
        dustAfter: Int,
    ): StarLevel? {
        val before = StarLevel.of(dustBefore)
        val after = StarLevel.of(dustAfter)
        return after.takeIf { it.ordinal > before.ordinal }
    }

    /** Emócia dňa – rovnaká pre celú galaxiu v ten istý deň. */
    fun emotionOfDay(epochDay: Long): Emotion {
        val all = Emotion.values()
        return all[Math.floorMod(epochDay * 7 + 3, all.size.toLong()).toInt()]
    }
}
