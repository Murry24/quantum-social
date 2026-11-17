package app.quantumsocial.ui.util

import app.quantumsocial.core.model.Emotion

/**
 * Minimal reprezentácia hviezdy pre hit-test.
 */
data class HitStarPoint(
    val id: String,
    val emotion: Emotion,
    val intensity: Float,
    val distance: Float,
    val isWishNet: Boolean,
    val angleRad: Float,
)
