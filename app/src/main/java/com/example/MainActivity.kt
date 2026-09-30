package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.data.LevelsRepository
import com.example.model.CustomLevel
import com.example.model.LevelData
import com.example.model.LevelTier
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AdminRewardsScreen
import com.example.ui.screens.CreateLevelScreen
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ArrowCubeTheme

sealed interface Screen {
    data object Splash : Screen
    data object Home : Screen
    data object LevelSelect : Screen
    data class Gameplay(val levelNumber: Int, val customLevelData: LevelData? = null) : Screen
    data object Rewards : Screen
    data object AdminPanel : Screen
    data object CreateLevel : Screen
    data object HowToPlay : Screen
    data object Settings : Screen
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager
    private lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = GamePreferences(this)
        soundManager = SoundManager(this)
        adMobManager = AdMobManager(this)

        setContent {
            ArrowCubeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        ArrowCubeApp(
                            preferences = preferences,
                            soundManager = soundManager,
                            adMobManager = adMobManager
                        )

                        // Global Test Ad Dialog Overlays
                        InterstitialAdDialog(adMobManager = adMobManager)
                        RewardedAdDialog(adMobManager = adMobManager)
                    }
                }
            }
        }
    }
}

@Composable
fun ArrowCubeApp(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    fun refreshState() {
        refreshTrigger++
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                onSplashFinished = {
                    currentScreen = Screen.Home
                }
            )
        }

        is Screen.Home -> {
            HomeScreen(
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onPlayClicked = { levelNumber ->
                    currentScreen = Screen.Gameplay(levelNumber = levelNumber)
                },
                onLevelsClicked = {
                    currentScreen = Screen.LevelSelect
                },
                onRewardsClicked = {
                    currentScreen = Screen.Rewards
                },
                onCreateClicked = {
                    currentScreen = Screen.CreateLevel
                },
                onHowToPlayClicked = {
                    currentScreen = Screen.HowToPlay
                },
                onSettingsClicked = {
                    currentScreen = Screen.Settings
                },
                onUpdatePreferences = {
                    refreshState()
                }
            )
        }

        is Screen.Rewards -> {
            BackHandler { currentScreen = Screen.Home }
            AdminRewardsScreen(
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onBackClicked = { currentScreen = Screen.Home },
                onOpenAdminPanel = { currentScreen = Screen.AdminPanel }
            )
        }

        is Screen.AdminPanel -> {
            BackHandler { currentScreen = Screen.Rewards }
            AdminPanelScreen(
                preferences = preferences,
                soundManager = soundManager,
                onBackClicked = { currentScreen = Screen.Rewards }
            )
        }

        is Screen.LevelSelect -> {
            BackHandler { currentScreen = Screen.Home }
            LevelSelectScreen(
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onLevelSelected = { lvl ->
                    currentScreen = Screen.Gameplay(levelNumber = lvl)
                },
                onCustomLevelSelected = { customId ->
                    val custom = preferences.getCustomLevels().firstOrNull { it.id == customId }
                    if (custom != null) {
                        val levelData = LevelData(
                            id = 8888,
                            levelNumber = 0,
                            tier = LevelTier.EASY,
                            rows = custom.rows,
                            cols = custom.cols,
                            cubes = custom.cubes,
                            parMoves = custom.cubes.size
                        )
                        currentScreen = Screen.Gameplay(levelNumber = 0, customLevelData = levelData)
                    }
                },
                onBackClicked = {
                    currentScreen = Screen.Home
                }
            )
        }

        is Screen.Gameplay -> {
            BackHandler {
                currentScreen = if (screen.customLevelData != null) Screen.LevelSelect else Screen.LevelSelect
            }
            val levelData = screen.customLevelData ?: LevelsRepository.getLevel(screen.levelNumber)

            GameplayScreen(
                levelData = levelData,
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onBackClicked = {
                    currentScreen = Screen.LevelSelect
                },
                onNextLevelRequested = { nextLvl ->
                    if (nextLvl <= GameConfig.MAX_LEVELS) {
                        currentScreen = Screen.Gameplay(levelNumber = nextLvl)
                    } else {
                        currentScreen = Screen.LevelSelect
                    }
                }
            )
        }

        is Screen.CreateLevel -> {
            BackHandler { currentScreen = Screen.Home }
            CreateLevelScreen(
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onBackClicked = {
                    currentScreen = Screen.Home
                },
                onTestLevelClicked = { testLevelData ->
                    currentScreen = Screen.Gameplay(levelNumber = 0, customLevelData = testLevelData)
                }
            )
        }

        is Screen.HowToPlay -> {
            BackHandler { currentScreen = Screen.Home }
            HowToPlayScreen(
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onBackClicked = {
                    currentScreen = Screen.Home
                }
            )
        }

        is Screen.Settings -> {
            BackHandler { currentScreen = Screen.Home }
            SettingsScreen(
                preferences = preferences,
                soundManager = soundManager,
                adMobManager = adMobManager,
                onBackClicked = {
                    currentScreen = Screen.Home
                },
                onSettingsChanged = {
                    refreshState()
                }
            )
        }
    }
}
