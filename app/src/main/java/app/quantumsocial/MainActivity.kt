package app.quantumsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.quantumsocial.core.ui.theme.AppTheme
import app.quantumsocial.nav.MainScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme { MainScaffold() }
        }
    }
}
