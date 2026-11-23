package app.quantumsocial.ui.screens

import androidx.compose.ui.graphics.Color
import app.quantumsocial.core.i18n.tr
import app.quantumsocial.core.model.Emotion

fun Emotion.label(): String =
    when (this) {
        Emotion.Joy -> tr("Radosť")
        Emotion.Calm -> tr("Pokoj")
        Emotion.Energy -> tr("Energia")
        Emotion.Love -> tr("Láska")
        Emotion.Mystery -> tr("Tajomno")
    }

fun Emotion.color(): Color =
    when (this) {
        Emotion.Joy -> Color(0xFFFFD54F)
        Emotion.Calm -> Color(0xFF64B5F6)
        Emotion.Energy -> Color(0xFFEF5350)
        Emotion.Love -> Color(0xFFBA68C8)
        Emotion.Mystery -> Color(0xFFA1887F)
    }
