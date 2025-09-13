package app.quantumsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.quantumsocial.ui.GalaxyScreen
import app.quantumsocial.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { GalaxyScreen() } }
    }
}
