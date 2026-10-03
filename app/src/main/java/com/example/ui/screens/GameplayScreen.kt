package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.config.GameConfig
import com.example.data.GamePreferences
import com.example.data.LevelsRepository
import com.example.model.CubeCell
import com.example.model.LevelData
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.ArrowCubeView
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton
import com.example.ui.components.ParticleEffectCanvas
import com.example.ui.components.ParticleEmitter
import kotlinx.coroutines.delay

@Composable
fun GameplayScreen(
    levelData: LevelData,
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onBackClicked: () -> Unit,
    onNextLevelRequested: (nextLvl: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    // Active cubes on the board
    val activeCubes = remember(levelData.id) {
        mutableStateListOf<CubeCell>().apply { addAll(levelData.cubes) }
    }

    // Move history for Undo support
    val moveHistory = remember(levelData.id) { mutableStateListOf<CubeCell>() }

    // Animation states
    var shakingCubeId by remember { mutableStateOf<Int?>(null) }
    var flyingCubeId by remember { mutableStateOf<Int?>(null) }
    var hintedCubeId by remember { mutableStateOf<Int?>(null) }

    var movesCount by remember(levelData.id) { mutableIntStateOf(0) }
    var blockedMovesCount by remember(levelData.id) { mutableIntStateOf(0) }
    var isLevelComplete by remember(levelData.id) { mutableStateOf(false) }
    var starsEarned by remember(levelData.id) { mutableIntStateOf(0) }

    // Hint dialog state
    var showHintDialog by remember { mutableStateOf(false) }

    val isOnline by adMobManager.isOnline.collectAsState()

    // Particle emitter for color explosions on match
    val particleEmitter = remember { ParticleEmitter() }

    // Handle cube tap logic
    fun onCubeTapped(cube: CubeCell) {
        if (isLevelComplete || flyingCubeId != null) return

        // Clear any previous hint
        hintedCubeId = null

        val pathIsClear = LevelsRepository.isPathClear(
            cube = cube,
            rows = levelData.rows,
            cols = levelData.cols,
            activeCubes = activeCubes
        )

        movesCount++

        if (pathIsClear) {
            // Success: cube launches off board!
            flyingCubeId = cube.id
            particleEmitter.explode(
                startX = context.resources.displayMetrics.widthPixels / 2f,
                startY = context.resources.displayMetrics.heightPixels / 2.5f,
                cubeColor = Color(0xFF38BDF8)
            )
            soundManager.playFlySuccess(preferences.isSoundEnabled)
            soundManager.vibrateSuccess(preferences.isVibrationEnabled)
        } else {
            // Blocked: shake and trigger haptic bump
            shakingCubeId = cube.id
            blockedMovesCount++
            soundManager.playBlocked(preferences.isSoundEnabled)
            soundManager.vibrateBlocked(preferences.isVibrationEnabled)
        }
    }

    // Finish fly-off animation
    fun onCubeFlyAnimationFinished(cube: CubeCell) {
        activeCubes.remove(cube)
        moveHistory.add(cube)
        flyingCubeId = null

        // Check victory condition
        if (activeCubes.isEmpty()) {
            isLevelComplete = true
            // Calculate stars: 3 stars if optimal, 2 stars if <= 2 blocked, 1 star otherwise
            starsEarned = when {
                blockedMovesCount == 0 -> 3
                blockedMovesCount <= 2 -> 2
                else -> 1
            }
            preferences.completeLevel(levelData.levelNumber, movesCount, starsEarned)

            soundManager.playLevelWon(preferences.isSoundEnabled)
            soundManager.vibrateSuccess(preferences.isVibrationEnabled)
        }
    }

    // Restart level
    fun restartLevel() {
        activeCubes.clear()
        activeCubes.addAll(levelData.cubes)
        moveHistory.clear()
        shakingCubeId = null
        flyingCubeId = null
        hintedCubeId = null
        movesCount = 0
        blockedMovesCount = 0
        isLevelComplete = false
    }

    // Undo last move
    fun undoLastMove() {
        if (moveHistory.isNotEmpty() && !isLevelComplete && flyingCubeId == null) {
            val lastCube = moveHistory.removeAt(moveHistory.lastIndex)
            activeCubes.add(lastCube)
            soundManager.playTap(preferences.isSoundEnabled)
        }
    }

    // Apply hint
    fun executeHint() {
        val movable = LevelsRepository.getHint(levelData.rows, levelData.cols, activeCubes)
        if (movable != null) {
            hintedCubeId = movable.id
            soundManager.playHint(preferences.isSoundEnabled)
            Toast.makeText(context, "Hint: Arrow highlighted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No movable arrows found!", Toast.LENGTH_SHORT).show()
        }
    }

    // Hint button click handler
    fun onHintClicked() {
        if (isLevelComplete || flyingCubeId != null) return

        val movable = LevelsRepository.getHint(levelData.rows, levelData.cols, activeCubes)
        if (movable == null) {
            Toast.makeText(context, "All remaining arrows are blocked!", Toast.LENGTH_SHORT).show()
            return
        }

        // Show Hint Options Dialog
        showHintDialog = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("gameplay_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Top Navigation & Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                testTag = "gameplay_back_button"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Level & Tier Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LEVEL ${levelData.levelNumber}",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = levelData.tier.title.uppercase(),
                    color = Color(levelData.tier.badgeColorHex),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Ticket Button (if player has tickets won from ads)
                if (preferences.levelSkipsAvailable > 0 && !isLevelComplete) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0284C7))
                            .border(1.5.dp, Color(0xFF0F172A), RoundedCornerShape(14.dp))
                            .clickable {
                                preferences.levelSkipsAvailable--
                                soundManager.playLevelWon(preferences.isSoundEnabled)
                                preferences.completeLevel(levelData.levelNumber, movesCount.coerceAtLeast(1), 3)
                                starsEarned = 3
                                isLevelComplete = true
                                Toast.makeText(context, "Level Skipped via Ad Ticket!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Skip Level",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SKIP (${preferences.levelSkipsAvailable})",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Stars Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface)
                        .border(1.5.dp, theme.surfaceBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Total Stars",
                            tint = theme.accentGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${preferences.getTotalStars()}",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Progress bar / Stats strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remaining: ${activeCubes.size} / ${levelData.cubes.size}",
                color = theme.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Moves: $movesCount",
                color = if (blockedMovesCount > 0) theme.accentRed else theme.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Main Puzzle Board (Isometric / 3D Grid Canvas)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val availableW = maxWidth
                val availableH = maxHeight
                val boardDim = min(availableW, availableH) - 16.dp

                val maxGridDim = maxOf(levelData.rows, levelData.cols)
                val cellSize = (boardDim - (8.dp * (maxGridDim - 1))) / maxGridDim

                // Flat 3D Grid Base Plate
                CubicalCard(
                    backgroundColor = theme.boardTray,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 6.dp,
                    cornerRadius = 18.dp,
                    modifier = Modifier.size(boardDim + 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Position each cell on the board
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (r in 0 until levelData.rows) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (c in 0 until levelData.cols) {
                                        val cube = activeCubes.firstOrNull { it.row == r && it.col == c }
                                        if (cube != null) {
                                            ArrowCubeView(
                                                cube = cube,
                                                sizeDp = cellSize,
                                                themeColors = theme,
                                                isHinted = cube.id == hintedCubeId,
                                                isShaking = cube.id == shakingCubeId,
                                                isFlying = cube.id == flyingCubeId,
                                                onAnimationDone = {
                                                    if (cube.id == shakingCubeId) shakingCubeId = null
                                                    if (cube.id == flyingCubeId) onCubeFlyAnimationFinished(cube)
                                                },
                                                onClick = { onCubeTapped(cube) }
                                            )
                                        } else {
                                            // Empty grid placeholder slot (flat 3D indented look)
                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(theme.boardSlot)
                                                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Control Action Bar (Undo, Restart, Hint)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo Button
            CubicalIconButton(
                onClick = { undoLastMove() },
                size = 50.dp,
                backgroundColor = theme.surface,
                bottomShadowColor = theme.cubeShadow,
                borderColor = theme.surfaceBorder,
                enabled = moveHistory.isNotEmpty(),
                testTag = "undo_button"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (moveHistory.isNotEmpty()) theme.textPrimary else Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Restart Button
            CubicalIconButton(
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    restartLevel()
                },
                size = 50.dp,
                backgroundColor = theme.surface,
                bottomShadowColor = theme.cubeShadow,
                borderColor = theme.surfaceBorder,
                testTag = "restart_button"
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Hint Button (Outlined cubical button with Lightbulb)
            CubicalButton(
                text = "HINT",
                onClick = { onHintClicked() },
                backgroundColor = theme.accentGold,
                bottomShadowColor = Color(0xFFD97706),
                borderColor = Color(0xFF0F172A),
                textColor = Color(0xFF0F172A),
                fontSize = 15,
                height = 50.dp,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                testTag = "hint_button"
            )
        }

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }

    // Particle effect canvas overlay inside root Box
    ParticleEffectCanvas(
        emitter = particleEmitter,
        modifier = Modifier.fillMaxSize()
    )
}

// Hint Dialog Modal
if (showHintDialog) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { showHintDialog = false }
    ) {
        CubicalCard(
            backgroundColor = theme.surface,
            borderColor = theme.accentGold,
            shadowColor = theme.cubeShadow,
            shadowDepth = 6.dp,
            cornerRadius = 18.dp,
            modifier = Modifier.fillMaxWidth().testTag("hint_options_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = theme.accentGold,
                    modifier = Modifier.size(36.dp)
                )

                Text(
                    text = "NEED A HINT?",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "A hint will highlight one arrow that is completely free to fly off right now.",
                    color = theme.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                // Option 1: Watch Rewarded Ad for Free Hint
                if (isOnline && GameConfig.ADS_ENABLED) {
                    CubicalButton(
                        text = "WATCH AD (FREE HINT)",
                        onClick = {
                            showHintDialog = false
                            adMobManager.showRewardedAd(
                                onRewardGranted = {
                                    executeHint()
                                },
                                onAdUnavailable = { reason ->
                                    Toast.makeText(context, reason, Toast.LENGTH_SHORT).show()
                                    // Offline fallback if ad fails
                                    executeHint()
                                }
                            )
                        },
                        backgroundColor = Color(0xFF22C55E),
                        bottomShadowColor = Color(0xFF15803D),
                        borderColor = Color(0xFF0F172A),
                        textColor = Color.White,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "watch_ad_hint_button"
                    )
                }

                // Option 2: Instant Free Hint
                CubicalButton(
                    text = "FREE HINT",
                    onClick = {
                        showHintDialog = false
                        executeHint()
                    },
                    backgroundColor = theme.arrowUpColor,
                    bottomShadowColor = Color(0xFF0284C7),
                    borderColor = Color(0xFF0F172A),
                    textColor = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "free_hint_button"
                )

                // Option 3: Skip level using ticket won from ads
                if (preferences.levelSkipsAvailable > 0) {
                    CubicalButton(
                        text = "USE SKIP TICKET (${preferences.levelSkipsAvailable})",
                        onClick = {
                            preferences.levelSkipsAvailable--
                            showHintDialog = false
                            soundManager.playLevelWon(preferences.isSoundEnabled)
                            preferences.completeLevel(levelData.levelNumber, movesCount.coerceAtLeast(1), 3)
                            starsEarned = 3
                            isLevelComplete = true
                            Toast.makeText(context, "Level Skipped via Ad Ticket!", Toast.LENGTH_SHORT).show()
                        },
                        backgroundColor = Color(0xFF0284C7),
                        bottomShadowColor = Color(0xFF0369A1),
                        borderColor = Color(0xFF0F172A),
                        textColor = Color.White,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "use_skip_ticket_hint_dialog_button"
                    )
                }
            }
        }
    }
}

    // Level Complete Dialog
    if (isLevelComplete) {
        val hasNext = levelData.levelNumber < GameConfig.MAX_LEVELS
        LevelCompleteDialog(
            levelNumber = levelData.levelNumber,
            starsEarned = starsEarned,
            movesTaken = movesCount,
            parMoves = levelData.parMoves,
            theme = theme,
            hasNextLevel = hasNext,
            onReplayClicked = { restartLevel() },
            onNextLevelClicked = {
                // Check interstitial frequency: e.g. every 2 levels
                if (preferences.levelsPlayedSinceAd % GameConfig.INTERSTITIAL_EVERY_N_LEVELS == 0) {
                    adMobManager.showInterstitial {
                        onNextLevelRequested(levelData.levelNumber + 1)
                    }
                } else {
                    onNextLevelRequested(levelData.levelNumber + 1)
                }
            },
            onLevelSelectClicked = { onBackClicked() }
        )
    }
}
