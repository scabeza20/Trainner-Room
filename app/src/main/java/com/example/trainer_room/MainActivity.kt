package com.example.trainer_room

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trainer_room.audio.AlarmSoundPlayer
import com.example.trainer_room.data.TrainerPreferences
import com.example.trainer_room.ui.RoutineViewModel
import com.example.trainer_room.ui.screens.RoutineScreen
import com.example.trainer_room.ui.screens.SettingsScreen
import com.example.trainer_room.ui.theme.TrainerRoomTheme

enum class AppScreen {
    ROUTINE,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: RoutineViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RoutineViewModel(
                    preferences = TrainerPreferences(applicationContext),
                    soundPlayer = AlarmSoundPlayer(applicationContext)
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isSystemDark = isSystemInDarkTheme()
            val state by viewModel.uiState.collectAsState()

            // Sincronizar tema del sistema cuando el modo sea SYSTEM
            LaunchedEffect(isSystemDark, state.themeMode) {
                viewModel.updateSystemDarkTheme(isSystemDark)
            }

            var currentScreen by rememberSaveable { mutableStateOf(AppScreen.ROUTINE) }

            // Manejo del botón atrás del sistema
            BackHandler(enabled = currentScreen == AppScreen.SETTINGS) {
                currentScreen = AppScreen.ROUTINE
            }

            TrainerRoomTheme(darkTheme = state.isDarkTheme) {
                when (currentScreen) {
                    AppScreen.ROUTINE -> {
                        RoutineScreen(
                            viewModel = viewModel,
                            onOpenSettings = { currentScreen = AppScreen.SETTINGS }
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.ROUTINE }
                        )
                    }
                }
            }
        }
    }
}
