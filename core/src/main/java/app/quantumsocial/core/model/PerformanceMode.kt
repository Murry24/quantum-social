package app.quantumsocial.core.model

/**
 * Šetrič výkonu (bod 6) – menej animácií, aby sa telefón (a notebook s emulátorom) neprehrieval.
 */
enum class PerformanceMode(
    val fireflyCount: Int,
    val parallax: Boolean,
    // ako často sa prekreslí animácia (Full ≈ 30 fps, Saver ≈ 15 fps)
    val frameIntervalMs: Long,
    // počet bublín v ZenNet
    val zenBubbles: Int,
    // trblietanie hviezd na mape
    val twinkle: Boolean,
) {
    Full(fireflyCount = 30, parallax = true, frameIntervalMs = 33, zenBubbles = 28, twinkle = true),
    Saver(fireflyCount = 8, parallax = false, frameIntervalMs = 66, zenBubbles = 10, twinkle = false),
}

/** Voľba používateľa v profile. Auto = riadi sa šetričom batérie v Androide. */
enum class PerformanceChoice { Auto, Full, Saver }

object PerformancePolicy {
    fun resolve(
        choice: PerformanceChoice,
        systemPowerSave: Boolean,
    ): PerformanceMode =
        when (choice) {
            PerformanceChoice.Full -> PerformanceMode.Full
            PerformanceChoice.Saver -> PerformanceMode.Saver
            PerformanceChoice.Auto -> if (systemPowerSave) PerformanceMode.Saver else PerformanceMode.Full
        }

    /** Bezpečné načítanie uloženej voľby (neznáma hodnota = Auto). */
    fun parseChoice(stored: String?): PerformanceChoice =
        PerformanceChoice.values().firstOrNull { it.name == stored } ?: PerformanceChoice.Auto
}
