package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.config.GameConfig
import com.example.data.GamePreferences
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton

@Composable
fun SettingsScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onBackClicked: () -> Unit,
    onSettingsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    var isResetDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("settings_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CubicalIconButton(
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onBackClicked()
                },
                size = 38.dp,
                backgroundColor = theme.surface,
                bottomShadowColor = theme.cubeShadow,
                borderColor = theme.surfaceBorder,
                testTag = "settings_back_button"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "SETTINGS",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Audio, Themes & Game Data",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Settings Content List
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Audio & Haptics
            SectionHeader(title = "AUDIO & HAPTICS", color = theme.accentGold)

            SettingToggleItem(
                title = "Sound Effects",
                subtitle = "Chimes, pops & victory fanfares",
                icon = Icons.Default.VolumeUp,
                checked = preferences.isSoundEnabled,
                theme = theme,
                onCheckedChange = {
                    preferences.isSoundEnabled = it
                    soundManager.playTap(it)
                    onSettingsChanged()
                }
            )

            SettingToggleItem(
                title = "Vibration & Haptics",
                subtitle = "Physical feedback when sliding cubes",
                icon = Icons.Default.Vibration,
                checked = preferences.isVibrationEnabled,
                theme = theme,
                onCheckedChange = {
                    preferences.isVibrationEnabled = it
                    soundManager.vibrateShort(it)
                    onSettingsChanged()
                }
            )

            SettingToggleItem(
                title = "Animations",
                subtitle = "Smooth fly-off & shake physics",
                icon = Icons.Default.Animation,
                checked = preferences.isAnimationsEnabled,
                theme = theme,
                onCheckedChange = {
                    preferences.isAnimationsEnabled = it
                    onSettingsChanged()
                }
            )

            // Section 2: Color Theme Selector
            SectionHeader(title = "COLOR THEME (NO GRADIENTS)", color = theme.arrowUpColor)

            CubicalCard(
                backgroundColor = theme.surface,
                borderColor = theme.surfaceBorder,
                shadowColor = theme.cubeShadow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameConfig.ThemeStyle.values().forEach { style ->
                        val isSelected = preferences.currentTheme == style
                        val styleTheme = GameConfig.THEMES[style]!!

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) styleTheme.cubeFace else Color.Transparent)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) theme.accentGold else Color(0xFFCBD5E1),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    preferences.currentTheme = style
                                    soundManager.playTap(preferences.isSoundEnabled)
                                    onSettingsChanged()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = style.displayName,
                                color = styleTheme.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            // Palette preview cubes (flat colors)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    styleTheme.arrowUpColor,
                                    styleTheme.arrowRightColor,
                                    styleTheme.arrowDownColor,
                                    styleTheme.arrowLeftColor
                                ).forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(c)
                                            .border(1.dp, Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: AdMob Configuration & Offline Info
            SectionHeader(title = "ADMOB TEST IDS & OFFLINE", color = theme.accentGreen)

            CubicalCard(
                backgroundColor = theme.surface,
                borderColor = theme.surfaceBorder,
                shadowColor = theme.cubeShadow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ArrowCube is fully playable offline without internet. Ads are only active when connected.",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Official Android Test Ad Units:",
                        color = theme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    AdIdRow(label = "Banner", id = GameConfig.ADMOB_BANNER_ID)
                    AdIdRow(label = "Interstitial", id = GameConfig.ADMOB_INTERSTITIAL_ID)
                    AdIdRow(label = "Rewarded", id = GameConfig.ADMOB_REWARDED_ID)
                }
            }

            // Section 4: Reset Game Progress
            SectionHeader(title = "GAME PROGRESS", color = theme.accentRed)

            CubicalButton(
                text = "RESET ALL PROGRESS",
                onClick = { isResetDialogOpen = true },
                backgroundColor = Color(0xFFEF4444),
                bottomShadowColor = Color(0xFF991B1B),
                borderColor = Color(0xFF0F172A),
                textColor = Color.White,
                fontSize = 14,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "reset_progress_button"
            )

            // Section 5: About / Version
            CubicalCard(
                backgroundColor = Color(0xFF141724),
                borderColor = theme.surfaceBorder,
                shadowColor = theme.cubeShadow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ArrowCube v${GameConfig.GAME_VERSION}",
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Offline-first 3D cubical block puzzle game • 100 Levels",
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }

    // Reset Confirmation Dialog
    if (isResetDialogOpen) {
        AlertDialog(
            onDismissRequest = { isResetDialogOpen = false },
            title = {
                Text(
                    text = "Reset Game Progress?",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text(
                    text = "This will reset all unlocked levels to Level 1 and clear your stars and moves records. This cannot be undone.",
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                CubicalButton(
                    text = "YES, RESET",
                    onClick = {
                        preferences.resetAllProgress()
                        isResetDialogOpen = false
                        Toast.makeText(context, "Progress has been reset.", Toast.LENGTH_SHORT).show()
                        onSettingsChanged()
                    },
                    backgroundColor = Color(0xFFEF4444),
                    bottomShadowColor = Color(0xFF991B1B),
                    borderColor = Color(0xFF0F172A),
                    textColor = Color.White,
                    fontSize = 13,
                    height = 42.dp,
                    testTag = "confirm_reset_button"
                )
            },
            dismissButton = {
                CubicalButton(
                    text = "CANCEL",
                    onClick = { isResetDialogOpen = false },
                    backgroundColor = theme.surface,
                    bottomShadowColor = theme.cubeShadow,
                    borderColor = theme.surfaceBorder,
                    textColor = theme.textPrimary,
                    fontSize = 13,
                    height = 42.dp,
                    testTag = "cancel_reset_button"
                )
            },
            containerColor = theme.surface
        )
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    theme: GameConfig.ThemeColors,
    onCheckedChange: (Boolean) -> Unit
) {
    CubicalCard(
        backgroundColor = theme.surface,
        borderColor = theme.surfaceBorder,
        shadowColor = theme.cubeShadow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.accentGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = theme.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0F172A),
                    checkedTrackColor = theme.accentGreen,
                    uncheckedThumbColor = Color(0xFF94A3B8),
                    uncheckedTrackColor = Color(0xFFE2E8F0)
                )
            )
        }
    }
}

@Composable
private fun AdIdRow(label: String, id: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = id,
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
