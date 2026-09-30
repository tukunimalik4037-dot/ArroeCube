package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.config.GameConfig
import com.example.data.GamePreferences
import com.example.model.LevelTier
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton

@Composable
fun HomeScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onPlayClicked: (levelNumber: Int) -> Unit,
    onLevelsClicked: () -> Unit,
    onRewardsClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    onHowToPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onUpdatePreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!
    val currentLevel = preferences.unlockedLevel.coerceIn(1, GameConfig.MAX_LEVELS)
    val currentTier = LevelTier.fromLevel(currentLevel)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("home_screen")
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total Stars Badge
            CubicalCard(
                backgroundColor = theme.surface,
                borderColor = theme.surfaceBorder,
                shadowColor = theme.cubeShadow,
                shadowDepth = 3.dp,
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Total Stars",
                        tint = theme.accentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${preferences.getTotalStars()}",
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            // Quick Audio & Rewards & Settings Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Admin Rewards Button
                CubicalButton(
                    text = "REWARDS",
                    onClick = {
                        soundManager.playTap(preferences.isSoundEnabled)
                        onRewardsClicked()
                    },
                    backgroundColor = Color(0xFFF59E0B),
                    bottomShadowColor = Color(0xFFD97706),
                    borderColor = Color(0xFF0F172A),
                    textColor = Color(0xFF0F172A),
                    fontSize = 12,
                    height = 40.dp,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Rewards",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    testTag = "header_rewards_button"
                )

                // Sound toggle
                CubicalIconButton(
                    onClick = {
                        preferences.isSoundEnabled = !preferences.isSoundEnabled
                        soundManager.playTap(preferences.isSoundEnabled)
                        onUpdatePreferences()
                    },
                    size = 40.dp,
                    backgroundColor = if (preferences.isSoundEnabled) theme.surface else Color(0xFFF1F5F9),
                    bottomShadowColor = theme.cubeShadow,
                    borderColor = theme.surfaceBorder,
                    testTag = "toggle_sound_button"
                ) {
                    Icon(
                        imageVector = if (preferences.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = if (preferences.isSoundEnabled) theme.accentGold else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Vibration toggle
                CubicalIconButton(
                    onClick = {
                        preferences.isVibrationEnabled = !preferences.isVibrationEnabled
                        soundManager.vibrateShort(preferences.isVibrationEnabled)
                        onUpdatePreferences()
                    },
                    size = 40.dp,
                    backgroundColor = if (preferences.isVibrationEnabled) theme.surface else Color(0xFFF1F5F9),
                    bottomShadowColor = theme.cubeShadow,
                    borderColor = theme.surfaceBorder,
                    testTag = "toggle_vibration_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibration Toggle",
                        tint = if (preferences.isVibrationEnabled) theme.arrowUpColor else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings
                CubicalIconButton(
                    onClick = {
                        soundManager.playTap(preferences.isSoundEnabled)
                        onSettingsClicked()
                    },
                    size = 40.dp,
                    backgroundColor = theme.surface,
                    bottomShadowColor = theme.cubeShadow,
                    borderColor = theme.surfaceBorder,
                    testTag = "settings_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Title with 3D block letters
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val titleLetters = listOf(
                    'A' to theme.arrowUpColor,
                    'R' to theme.arrowRightColor,
                    'R' to theme.arrowDownColor,
                    'O' to theme.arrowLeftColor,
                    'W' to theme.accentGold
                )
                for ((char, col) in titleLetters) {
                    CubicalCard(
                        backgroundColor = col,
                        borderColor = Color(0xFF0F172A),
                        shadowColor = theme.cubeShadow,
                        shadowDepth = 3.dp,
                        cornerRadius = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val titleLetters2 = listOf(
                    'C' to theme.arrowUpColor,
                    'U' to theme.arrowRightColor,
                    'B' to theme.arrowDownColor,
                    'E' to theme.arrowLeftColor
                )
                for ((char, col) in titleLetters2) {
                    CubicalCard(
                        backgroundColor = col,
                        borderColor = Color(0xFF0F172A),
                        shadowColor = theme.cubeShadow,
                        shadowDepth = 3.dp,
                        cornerRadius = 8.dp
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surface)
                    .border(1.5.dp, theme.surfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "NEXT: LEVEL $currentLevel • ${currentTier.title.uppercase()}",
                    color = Color(currentTier.badgeColorHex),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons Menu
            // 1. PLAY (Primary highlighted button)
            CubicalButton(
                text = "PLAY LEVEL $currentLevel",
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onPlayClicked(currentLevel)
                },
                backgroundColor = theme.accentGreen,
                bottomShadowColor = Color(0xFF15803D),
                borderColor = Color(0xFF0F172A),
                textColor = Color.White,
                fontSize = 18,
                height = 58.dp,
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "play_level_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. LEVELS (1,000 levels)
            CubicalButton(
                text = "LEVELS (1,000)",
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onLevelsClicked()
                },
                backgroundColor = theme.arrowUpColor,
                bottomShadowColor = Color(0xFF0284C7),
                borderColor = Color(0xFF0F172A),
                textColor = Color.White,
                fontSize = 16,
                icon = {
                    Icon(
                        imageVector = Icons.Default.ViewModule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "levels_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2.5 REWARDS (ADMIN)
            CubicalButton(
                text = "REWARDS (ADMIN)",
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onRewardsClicked()
                },
                backgroundColor = Color(0xFFF59E0B),
                bottomShadowColor = Color(0xFFD97706),
                borderColor = Color(0xFF0F172A),
                textColor = Color(0xFF0F172A),
                fontSize = 16,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "menu_rewards_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. CREATE (Level Editor)
            CubicalButton(
                text = "CREATE LEVEL",
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onCreateClicked()
                },
                backgroundColor = theme.arrowRightColor,
                bottomShadowColor = Color(0xFFD97706),
                borderColor = Color(0xFF0F172A),
                textColor = Color(0xFF0F172A),
                fontSize = 16,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "create_level_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. HOW TO PLAY (Tutorial)
            CubicalButton(
                text = "HOW TO PLAY",
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onHowToPlayClicked()
                },
                backgroundColor = theme.arrowDownColor,
                bottomShadowColor = Color(0xFF9D174D),
                borderColor = Color(0xFF0F172A),
                textColor = Color.White,
                fontSize = 16,
                icon = {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "how_to_play_button"
            )
        }

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }
}
