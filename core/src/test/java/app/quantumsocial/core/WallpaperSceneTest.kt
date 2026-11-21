package app.quantumsocial.core

import app.quantumsocial.core.model.PerformanceMode
import app.quantumsocial.core.model.WallpaperScene
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class WallpaperSceneTest {
    @Test
    fun same_seed_gives_same_sky() {
        assertEquals(WallpaperScene(seed = 7).stars, WallpaperScene(seed = 7).stars)
        assertEquals(WallpaperScene.STAR_COUNT, WallpaperScene().stars.size)
    }

    @Test
    fun stars_stay_on_screen_and_glow_in_range() {
        val scene = WallpaperScene()
        for (s in scene.stars) {
            assertTrue(s.x in 0f..1f && s.y in 0f..1f)
            for (t in listOf(0f, 1.3f, 42f, 1000f)) {
                val a = scene.starAlpha(s, t, twinkle = true)
                assertTrue("jas $a", a in 0.2f..1f)
            }
            assertEquals(0.75f, scene.starAlpha(s, 5f, twinkle = false), 0.0001f)
        }
    }

    @Test
    fun hazes_drift_only_a_little() {
        val scene = WallpaperScene()
        for (i in 0 until 5) {
            val (x0, y0) = scene.hazeCenter(i, 0f)
            for (t in listOf(10f, 100f, 1000f)) {
                val (x, y) = scene.hazeCenter(i, t)
                assertTrue(abs(x - x0) <= 0.13f && abs(y - y0) <= 0.13f)
            }
        }
    }

    @Test
    fun saver_redraws_less_often() {
        val saver = WallpaperScene.frameIntervalMs(PerformanceMode.Saver)
        val full = WallpaperScene.frameIntervalMs(PerformanceMode.Full)
        assertTrue(saver > full)
    }
}
