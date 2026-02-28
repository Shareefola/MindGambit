package com.mindgambit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mindgambit.app.presentation.navigation.NavGraph
import com.mindgambit.app.presentation.theme.MindGambitTheme
import dagger.hilt.android.AndroidEntryPoint

// ============================================================
// MindGambit — Main Activity
// Single-activity architecture. Compose NavHost renders all
// screens inside this one activity window.
// ============================================================
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash screen while app initialises
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Draw behind system bars (edge-to-edge)
        enableEdgeToEdge()

        setContent {
            MindGambitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph()
                }
            }
        }
    }
}
