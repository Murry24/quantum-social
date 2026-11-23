package app.quantumsocial.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import app.quantumsocial.core.model.PerformanceMode
import app.quantumsocial.core.model.PerformancePolicy
import app.quantumsocial.core.model.WallpaperScene
import app.quantumsocial.core.ui.theme.GalaxyPalette
import app.quantumsocial.core.ui.theme.GalaxyTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Živá tapeta (bod 33): moja galaxia na ploche telefónu – obloha podľa „Farby galaxie“,
 * plávajúce hmloviny, trblietavé hviezdy a svetlušky. Kreslí sa len keď je tapeta viditeľná.
 */
class GalaxyWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = GalaxyEngine()

    private inner class GalaxyEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val scene = WallpaperScene()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val startedAt = SystemClock.elapsedRealtime()
        private var visible = false
        private var theme = GalaxyTheme.Dawn
        private var mode = PerformanceMode.Full
        private var width = 0
        private var height = 0
        private val drawFrame = Runnable { draw() }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                readSettings()
                draw()
            } else {
                handler.removeCallbacks(drawFrame)
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int,
        ) {
            this.width = width
            this.height = height
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            visible = false
            handler.removeCallbacks(drawFrame)
        }

        override fun onDestroy() {
            handler.removeCallbacks(drawFrame)
            super.onDestroy()
        }

        private fun readSettings() {
            val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            theme = GalaxyTheme.parse(prefs.getString(KEY_THEME, null))
            val power = getSystemService(Context.POWER_SERVICE) as PowerManager
            val choice = PerformancePolicy.parseChoice(prefs.getString(KEY_PERF, null))
            mode = PerformancePolicy.resolve(choice, power.isPowerSaveMode)
        }

        private fun draw() {
            handler.removeCallbacks(drawFrame)
            if (width == 0 || height == 0) return
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let { paint(it) }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
            if (visible) handler.postDelayed(drawFrame, WallpaperScene.frameIntervalMs(mode))
        }

        private fun paint(c: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            val seconds = (SystemClock.elapsedRealtime() - startedAt) / 1000f

            // obloha
            val stops = theme.skyStops.map { it.toInt() }.toIntArray()
            paint.shader = LinearGradient(0f, 0f, 0f, h, stops, null, Shader.TileMode.CLAMP)
            paint.alpha = 255
            c.drawRect(0f, 0f, w, h, paint)

            // hmloviny
            val r = max(w, h) * 0.55f
            GalaxyPalette.hazes.forEachIndexed { i, haze ->
                val (hx, hy) = scene.hazeCenter(i, seconds)
                val color = (haze.argb and 0x00FFFFFF).toInt() or ((haze.alpha * 255).toInt() shl 24)
                paint.shader = RadialGradient(hx * w, hy * h, r, color, 0x00FFFFFF, Shader.TileMode.CLAMP)
                c.drawCircle(hx * w, hy * h, r, paint)
            }
            paint.shader = null

            // hviezdy
            val density = resources.displayMetrics.density
            paint.color = 0xFFFFFFFF.toInt()
            for (s in scene.stars) {
                paint.alpha = (scene.starAlpha(s, seconds, mode.twinkle) * 255).toInt()
                c.drawCircle(s.x * w, s.y * h, s.radius * density, paint)
            }

            // svetlušky
            val colors = GalaxyPalette.fireflies
            for (i in 0 until mode.fireflyCount / 2) {
                val t = seconds * 0.08f + i * 0.9f
                val x = (0.5f + 0.42f * sin(t * 1.3f + i)) * w
                val y = (0.5f + 0.45f * cos(t + i * 0.7f)) * h
                paint.color = colors[i % colors.size].toInt()
                paint.alpha = (140 + 100 * sin(seconds * 1.5f + i)).toInt().coerceIn(40, 240)
                c.drawCircle(x, y, 2.5f * density, paint)
            }
        }
    }

    private companion object {
        const val PREFS = "quantum_prefs"
        const val KEY_THEME = "galaxy_theme"
        const val KEY_PERF = "performance_choice"
    }
}
