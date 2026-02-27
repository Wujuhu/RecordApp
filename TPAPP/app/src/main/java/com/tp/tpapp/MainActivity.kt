package com.tp.tpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tp.tpapp.data.SettingsRepository
import com.tp.tpapp.ui.navigation.Routes
import com.tp.tpapp.ui.navigation.TPNavigation
import com.tp.tpapp.ui.theme.TPAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = SettingsRepository.getInstance(this)

        setContent {
            val themeMode by settingsRepository.themeModeFlow.collectAsState(initial = -1)
            val startupTab by settingsRepository.startupTabFlow.collectAsState(
                initial = SettingsRepository.STARTUP_TAB_RECORD
            )
            val darkTheme = when(themeMode) {
                0 -> false
                1 -> true
                else -> isSystemInDarkTheme()
            }
            val startRoute = if (startupTab == SettingsRepository.STARTUP_TAB_PASSWORD) {
                Routes.APP_LIST
            } else {
                Routes.RECORD_LIST
            }

            TPAPPTheme(darkTheme = darkTheme) {
                TPNavigation(startDestination = startRoute)
            }
        }
    }
}
