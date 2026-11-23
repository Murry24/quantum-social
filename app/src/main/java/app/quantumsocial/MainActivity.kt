package app.quantumsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.quantumsocial.core.i18n.I18n
import app.quantumsocial.core.ui.theme.AppTheme
import app.quantumsocial.nav.MainScaffold
import app.quantumsocial.ui.perf.PerformanceProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // smer písma podľa jazyka aplikácie (arabčina sprava doľava), nie podľa telefónu
        val direction = if (I18n.lang in I18n.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                AppTheme { PerformanceProvider { MainScaffold() } }
            }
        }
    }
}
