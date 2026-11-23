package app.quantumsocial.wallpaper

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Otvorí systémový náhľad živej tapety (bod 33); používateľ ju potvrdí sám. */
object WallpaperLauncher {
    fun open(context: Context) {
        val component = ComponentName(context, GalaxyWallpaperService::class.java)
        val preview =
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
        try {
            context.startActivity(preview)
        } catch (e: ActivityNotFoundException) {
            // niektoré telefóny nemajú priamy náhľad – aspoň zoznam živých tapiet
            context.startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
        }
    }
}
