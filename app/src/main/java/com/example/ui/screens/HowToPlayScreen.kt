package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.LevelsRepository
import com.example.model.ArrowDirection
import com.example.model.CubeCell
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.ArrowCubeView
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton

@Composable
fun HowToPlayScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    // Interactive Demo Board State (3x3 grid)
    val demoCubes = remember {
        mutableStateListOf(
            // Cube 1 at (0, 1) pointing UP (Free to escape!)
            CubeCell(1, 0, 1, ArrowDirection.UP),
            // Cube 2 at (1, 1) pointing UP (Blocked by Cube 1!)
            CubeCell(2, 1, 1, ArrowDirection.UP),
            // Cube 3 at (1, 0) pointing LEFT (Free to escape!)
            CubeCell(3, 1, 0, ArrowDirection.LEFT),
            // Cube 4 at (1, 2) pointing RIGHT (Free to escape!)
            CubeCell(4, 1, 2, ArrowDirection.RIGHT),
            // Cube 5 at (2, 1) pointing DOWN (Free to escape!)
            CubeCell(5, 2, 1, ArrowDirection.DOWN)
        )
    }

    var demoFlyingId by remember { mutableStateOf<Int?>(null) }
    var demoShakingId by remember { mutableStateOf<Int?>(null) }
    var demoStatusText by remember { mutableStateOf("Tap any arrow on the mini-board below to try!") }

    fun handleDemoTap(cube: CubeCell) {
        if (demoFlyingId != null) return

        val isClear = LevelsRepository.isPathClear(cube, 3, 3, demoCubes)
        if (isClear) {
            demoFlyingId = cube.id
            demoStatusText = "Clear! Arrow #${cube.id} escaped the grid!"
            soundManager.playFlySuccess(preferences.isSoundEnabled)
            soundManager.vibrateSuccess(preferences.isVibrationEnabled)
        } else {
            demoShakingId = cube.id
            demoStatusText = "Blocked! Arrow #${cube.id} is blocked by another cube."
            soundManager.playBlocked(preferences.isSoundEnabled)
            soundManager.vibrateBlocked(preferences.isVibrationEnabled)
        }
    }

    fun resetDemo() {
        demoCubes.clear()
        demoCubes.addAll(
            listOf(
                CubeCell(1, 0, 1, ArrowDirection.UP),
                CubeCell(2, 1, 1, ArrowDirection.UP),
                CubeCell(3, 1, 0, ArrowDirection.LEFT),
                CubeCell(4, 1, 2, ArrowDirection.RIGHT),
                CubeCell(5, 2, 1, ArrowDirection.DOWN)
            )
        )
        demoFlyingId = null
        demoShakingId = null
        demoStatusText = "Mini-board reset. Try tapping the middle arrow!"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("how_to_play_screen")
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
                testTag = "how_to_play_back_button"
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
                    text = "HOW TO PLAY",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Rules & Interactive Demo",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Mini Sandbox
            CubicalCard(
                backgroundColor = theme.surface,
                borderColor = theme.accentGold,
                shadowColor = theme.cubeShadow,
                shadowDepth = 4.dp,
                cornerRadius = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = theme.accentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INTERACTIVE DEMO",
                                color = theme.accentGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        CubicalIconButton(
                            onClick = {
                                soundManager.playTap(preferences.isSoundEnabled)
                                resetDemo()
                            },
                            size = 32.dp,
                            backgroundColor = Color(0xFF1E2333),
                            bottomShadowColor = theme.cubeShadow,
                            borderColor = theme.surfaceBorder
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Demo",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = demoStatusText,
                        color = theme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3x3 Mini Grid
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.boardTray)
                            .border(1.5.dp, Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (r in 0 until 3) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (c in 0 until 3) {
                                        val cube = demoCubes.firstOrNull { it.row == r && it.col == c }
                                        if (cube != null) {
                                            ArrowCubeView(
                                                cube = cube,
                                                sizeDp = 50.dp,
                                                themeColors = theme,
                                                isHinted = false,
                                                isShaking = cube.id == demoShakingId,
                                                isFlying = cube.id == demoFlyingId,
                                                onAnimationDone = {
                                                    if (cube.id == demoShakingId) demoShakingId = null
                                                    if (cube.id == demoFlyingId) {
                                                        demoCubes.remove(cube)
                                                        demoFlyingId = null
                                                        if (demoCubes.isEmpty()) {
                                                            demoStatusText = "All arrows cleared! Perfect!"
                                                        }
                                                    }
                                                },
                                                onClick = { handleDemoTap(cube) }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(theme.boardSlot)
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rule Card 1
            RuleCard(
                step = "1",
                title = "Tapping an Arrow Flies Forward",
                description = "Each cube has an arrow indicating its flight direction. When tapped, it attempts to travel straight off the edge of the board.",
                icon = Icons.Default.CheckCircle,
                iconColor = theme.accentGreen,
                theme = theme
            )

            // Rule Card 2
            RuleCard(
                step = "2",
                title = "Obstacles Block the Path",
                description = "If another cube stands in front of the arrow, it cannot escape! The cube will shake and stay in place until its path is cleared.",
                icon = Icons.Default.Close,
                iconColor = theme.accentRed,
                theme = theme
            )

            // Rule Card 3
            RuleCard(
                step = "3",
                title = "Peel Away from Outside In",
                description = "Look for outer cubes that face outward with clear trajectories. Clearing them creates empty channels for the inner trapped cubes.",
                icon = Icons.Default.Lightbulb,
                iconColor = theme.accentGold,
                theme = theme
            )

            // Rule Card 4
            RuleCard(
                step = "4",
                title = "Earn 3 Stars on Every Level",
                description = "Clear all arrows without wasted blocked taps to earn a 3-star rating and qualify for special Admin rewards!",
                icon = Icons.Default.CheckCircle,
                iconColor = theme.arrowUpColor,
                theme = theme
            )
        }

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }
}

@Composable
private fun RuleCard(
    step: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    theme: GameConfig.ThemeColors
) {
    CubicalCard(
        backgroundColor = theme.surface,
        borderColor = theme.surfaceBorder,
        shadowColor = theme.cubeShadow,
        shadowDepth = 3.dp,
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor)
                    .border(1.5.dp, Color(0xFF0F172A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step,
                    color = Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
