package app.quantumsocial.core.model

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

/** Kroky onboardingu „Zrodenie hviezdy“ (bod 18) v poradí, v akom idú. */
enum class OnboardingStep { Birth, Language, Emotion, Name, RecoveryKey, Notifications, Tutorial }

/**
 * Čistý stav sprievodcu. Kľúč (krok 5) sa nedá preskočiť a pokračuje sa až po potvrdení,
 * že si ho používateľ zapísal. Po poslednom kroku je [done] = true.
 */
data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Birth,
    val keyConfirmed: Boolean = false,
    val done: Boolean = false,
) {
    val canSkip: Boolean get() = !done && step != OnboardingStep.RecoveryKey
    val canContinue: Boolean get() = !done && (step != OnboardingStep.RecoveryKey || keyConfirmed)

    fun next(): OnboardingState {
        if (!canContinue) return this
        val steps = OnboardingStep.values()
        return if (step.ordinal == steps.lastIndex) copy(done = true) else copy(step = steps[step.ordinal + 1])
    }

    fun skip(): OnboardingState = if (canSkip) next() else this

    fun confirmKey(confirmed: Boolean): OnboardingState = copy(keyConfirmed = confirmed)
}

/** Obnovovací kľúč: 8 náhodných slov. Nikdy ho nelogovať ani neposielať. */
object RecoveryKey {
    const val WORD_COUNT = 8

    // shortcut: 64 slov × 8 = 48 bitov, pre server (bod 9) treba väčší zoznam (BIP39 má 2048)
    val words: List<String> =
        listOf(
            "iskra", "hviezda", "kométa", "dúha", "vlna", "perla", "ozvena", "aurora",
            "mesiac", "slnko", "oblak", "dážď", "vietor", "rosa", "jazero", "rieka",
            "les", "lúka", "kvet", "lipa", "breza", "mak", "levanduľa", "jahoda",
            "med", "čaj", "pierko", "krídlo", "vták", "motýľ", "včela", "líška",
            "sova", "jeleň", "delfín", "veľryba", "korál", "mušľa", "piesok", "kameň",
            "kryštál", "zlato", "striebro", "jantár", "smaragd", "zafír", "rubín", "opál",
            "pieseň", "tanec", "úsmev", "objatie", "sen", "nádej", "radosť", "pokoj",
            "svetlo", "tieň", "úsvit", "súmrak", "zima", "jar", "leto", "jeseň",
        )

    fun generate(random: Random = SecureRandom().asKotlinRandom()): List<String> =
        List(WORD_COUNT) { words[random.nextInt(words.size)] }
}
