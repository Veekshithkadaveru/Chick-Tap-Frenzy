package app.krafted.chicktapfrenzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.krafted.chicktapfrenzy.ui.ChickTapApp
import app.krafted.chicktapfrenzy.ui.theme.ChickTapFrenzyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChickTapFrenzyTheme {
                ChickTapApp()
            }
        }
    }
}
