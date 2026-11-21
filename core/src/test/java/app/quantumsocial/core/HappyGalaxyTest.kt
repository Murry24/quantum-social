package app.quantumsocial.core

import app.quantumsocial.core.model.PerformanceChoice
import app.quantumsocial.core.model.PerformanceMode
import app.quantumsocial.core.model.PerformancePolicy
import app.quantumsocial.core.ui.theme.ColorContrast
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.core.ui.theme.GalaxyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HappyGalaxyTest {
    @Test
    fun white_text_is_readable_on_every_sky_color() {
        val skies = GalaxyPalette.skyStops + GalaxyPalette.ZEN_TOP + GalaxyPalette.ZEN_BOTTOM
        for (sky in skies) {
            val r = ColorContrast.ratio(GalaxyPalette.ON_SKY, sky)
            assertTrue("kontrast ${"%.2f".format(r)} na ${sky.toString(16)}", r >= 4.5)
        }
    }

    @Test
    fun star_cores_stay_visible_inside_nebulas() {
        for (sky in GalaxyPalette.skyStops) {
            for (haze in GalaxyPalette.hazes) {
                val mixed = ColorContrast.blend(haze.argb, haze.alpha, sky)
                val r = ColorContrast.ratio(GalaxyPalette.ON_SKY, mixed)
                assertTrue("kontrast ${"%.2f".format(r)} v hmlovine", r >= 3.0)
            }
        }
    }

    @Test
    fun sky_is_lighter_than_old_dark_space() {
        val oldDark = 0xFF1A1457
        for (sky in GalaxyPalette.skyStops) {
            assertTrue(ColorContrast.ratio(0xFF000000, sky) > ColorContrast.ratio(0xFF000000, oldDark))
        }
    }

    @Test
    fun every_galaxy_theme_keeps_text_and_stars_readable() {
        for (theme in GalaxyTheme.values()) {
            for (sky in theme.skyStops) {
                val r = ColorContrast.ratio(GalaxyPalette.ON_SKY, sky)
                assertTrue("${theme.name}: kontrast ${"%.2f".format(r)}", r >= 4.5)
                for (haze in GalaxyPalette.hazes) {
                    val mixed = ColorContrast.blend(haze.argb, haze.alpha, sky)
                    assertTrue("${theme.name}: hmlovina", ColorContrast.ratio(GalaxyPalette.ON_SKY, mixed) >= 3.0)
                }
            }
        }
    }

    @Test
    fun stored_theme_parsing() {
        assertEquals(GalaxyTheme.Ocean, GalaxyTheme.parse("Ocean"))
        assertEquals(GalaxyTheme.Dawn, GalaxyTheme.parse(null))
        assertEquals(GalaxyTheme.Dawn, GalaxyTheme.parse("nezmysel"))
    }

    @Test
    fun contrast_known_values() {
        assertEquals(21.0, ColorContrast.ratio(0xFFFFFFFF, 0xFF000000), 0.01)
        assertEquals(1.0, ColorContrast.ratio(0xFF777777, 0xFF777777), 0.001)
        assertEquals(0xFF808080, ColorContrast.blend(0xFFFFFFFF, 0.5f, 0xFF000000) and 0xFFFEFEFE)
    }

    @Test
    fun auto_follows_android_battery_saver() {
        assertEquals(PerformanceMode.Full, PerformancePolicy.resolve(PerformanceChoice.Auto, systemPowerSave = false))
        assertEquals(PerformanceMode.Saver, PerformancePolicy.resolve(PerformanceChoice.Auto, systemPowerSave = true))
    }

    @Test
    fun manual_choice_wins_over_system() {
        assertEquals(PerformanceMode.Full, PerformancePolicy.resolve(PerformanceChoice.Full, systemPowerSave = true))
        assertEquals(PerformanceMode.Saver, PerformancePolicy.resolve(PerformanceChoice.Saver, systemPowerSave = false))
    }

    @Test
    fun saver_really_saves() {
        val full = PerformanceMode.Full
        val saver = PerformanceMode.Saver
        assertTrue(saver.fireflyCount < full.fireflyCount)
        assertTrue(saver.fireflyCount <= 8)
        assertTrue(full.fireflyCount in 20..40)
        assertTrue(saver.frameIntervalMs >= 60)
        assertTrue(!saver.parallax && !saver.twinkle)
        assertTrue(saver.zenBubbles < full.zenBubbles)
    }

    @Test
    fun stored_choice_parsing() {
        assertEquals(PerformanceChoice.Saver, PerformancePolicy.parseChoice("Saver"))
        assertEquals(PerformanceChoice.Auto, PerformancePolicy.parseChoice(null))
        assertEquals(PerformanceChoice.Auto, PerformancePolicy.parseChoice("nezmysel"))
    }
}
