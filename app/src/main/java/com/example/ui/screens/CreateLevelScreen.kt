package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.ArrowDirection
import com.example.model.CubeCell
import com.example.model.CustomLevel
import com.example.model.LevelData
import com.example.model.LevelTier
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.ArrowCubeView
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton
import java.util.UUID

@Composable
fun CreateLevelScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onBackClicked: () -> Unit,
    onTestLevelClicked: (levelData: LevelData) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    var gridSize by remember { mutableIntStateOf(4) } // 3, 4, 5, 6
    val cubes = remember { mutableStateListOf<CubeCell>() }
    val history = remember { mutableStateListOf<List<CubeCell>>() }

    // Selected tool: UP, RIGHT, DOWN, LEFT, or null for ERASE
    var selectedDirection by remember { mutableStateOf<ArrowDirection?>(ArrowDirection.UP) }

    var isSaveDialogOpen by remember { mutableStateOf(false) }
    var levelNameInput by remember { mutableStateOf("My Puzzle") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var isValidSolvable by remember { mutableStateOf(false) }

    fun pushHistory() {
        history.add(cubes.toList())
    }

    fun onCellClicked(row: Int, col: Int) {
        pushHistory()
        val existingIndex = cubes.indexOfFirst { it.row == row && it.col == col }
        if (selectedDirection == null) {
            // Eraser mode
            if (existingIndex >= 0) {
                cubes.removeAt(existingIndex)
                soundManager.playTap(preferences.isSoundEnabled)
            }
        } else {
            val dir = selectedDirection!!
            if (existingIndex >= 0) {
                // If same direction, erase or cycle; otherwise update direction
                val existing = cubes[existingIndex]
                if (existing.direction == dir) {
                    cubes.removeAt(existingIndex)
                } else {
                    cubes[existingIndex] = existing.copy(direction = dir)
                }
            } else {
                cubes.add(CubeCell(cubes.size + 1, row, col, dir))
            }
            soundManager.playTap(preferences.isSoundEnabled)
        }
        // Invalidate solver result
        validationMessage = null
        isValidSolvable = false
    }

    fun validatePuzzle(): Boolean {
        if (cubes.isEmpty()) {
            validationMessage = "Puzzle is empty! Place some arrows first."
            isValidSolvable = false
            return false
        }
        val solvable = LevelsRepository.isSolvable(gridSize, gridSize, cubes.toList())
        if (solvable) {
            validationMessage = "Solvable! All arrows can escape cleanly."
            isValidSolvable = true
            soundManager.playFlySuccess(preferences.isSoundEnabled)
            soundManager.vibrateSuccess(preferences.isVibrationEnabled)
        } else {
            validationMessage = "Deadlock detected! Arrows block each other. Make sure at least one can escape."
            isValidSolvable = false
            soundManager.playBlocked(preferences.isSoundEnabled)
        }
        return solvable
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("create_level_screen")
    ) {
        // Top App Bar
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
                testTag = "create_back_button"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "LEVEL CREATOR",
                color = theme.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            // Undo button
            CubicalIconButton(
                onClick = {
                    if (history.isNotEmpty()) {
                        val prev = history.removeAt(history.lastIndex)
                        cubes.clear()
                        cubes.addAll(prev)
                        validationMessage = null
                        isValidSolvable = false
                        soundManager.playTap(preferences.isSoundEnabled)
                    }
                },
                size = 38.dp,
                backgroundColor = theme.surface,
                bottomShadowColor = theme.cubeShadow,
                borderColor = theme.surfaceBorder,
                enabled = history.isNotEmpty(),
                testTag = "editor_undo_button"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (history.isNotEmpty()) theme.textPrimary else Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Grid Size Selector Bar (3x3, 4x4, 5x5, 6x6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grid:",
                color = theme.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            listOf(3, 4, 5, 6).forEach { size ->
                val isSelected = gridSize == size
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) theme.arrowUpColor else theme.surface)
                        .border(
                            1.5.dp,
                            if (isSelected) Color(0xFF0F172A) else theme.surfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (gridSize != size) {
                                pushHistory()
                                gridSize = size
                                cubes.removeAll { it.row >= size || it.col >= size }
                                validationMessage = null
                                isValidSolvable = false
                            }
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${size}x$size",
                        color = if (isSelected) Color(0xFF0F172A) else theme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Direction Selector Tool Palette
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val toolItems = listOf(
                Triple("UP", ArrowDirection.UP, theme.arrowUpColor),
                Triple("RIGHT", ArrowDirection.RIGHT, theme.arrowRightColor),
                Triple("DOWN", ArrowDirection.DOWN, theme.arrowDownColor),
                Triple("LEFT", ArrowDirection.LEFT, theme.arrowLeftColor),
                Triple("ERASE", null, Color(0xFFEF4444))
            )

            for ((label, dir, col) in toolItems) {
                val isSelected = selectedDirection == dir
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) col else theme.surface)
                        .border(
                            2.dp,
                            if (isSelected) Color(0xFF0F172A) else theme.surfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            soundManager.playTap(preferences.isSoundEnabled)
                            selectedDirection = dir
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) (if (dir == ArrowDirection.DOWN || dir == null) Color.White else Color(0xFF0F172A)) else theme.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Validation message banner
        if (validationMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isValidSolvable) Color(0xFF14532D) else Color(0xFF7F1D1D))
                    .border(
                        1.dp,
                        if (isValidSolvable) Color(0xFF22C55E) else Color(0xFFEF4444),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = validationMessage!!,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Editor Grid Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val boardDim = min(maxWidth, maxHeight) - 12.dp
                val cellSize = (boardDim - (6.dp * (gridSize - 1))) / gridSize

                CubicalCard(
                    backgroundColor = theme.boardTray,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 6.dp,
                    cornerRadius = 16.dp,
                    modifier = Modifier.size(boardDim + 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (r in 0 until gridSize) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (c in 0 until gridSize) {
                                        val cube = cubes.firstOrNull { it.row == r && it.col == c }
                                        if (cube != null) {
                                            ArrowCubeView(
                                                cube = cube,
                                                sizeDp = cellSize,
                                                themeColors = theme,
                                                isHinted = false,
                                                isShaking = false,
                                                isFlying = false,
                                                onAnimationDone = {},
                                                onClick = { onCellClicked(r, c) }
                                            )
                                        } else {
                                            // Empty cell slot
                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(theme.boardSlot)
                                                    .border(
                                                        1.5.dp,
                                                        Color(0xFFCBD5E1),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { onCellClicked(r, c) }
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

        // Action Toolbar: Clear, Validate, Test Play, Save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear Grid Button
            CubicalIconButton(
                onClick = {
                    pushHistory()
                    cubes.clear()
                    validationMessage = null
                    isValidSolvable = false
                    soundManager.playTap(preferences.isSoundEnabled)
                },
                size = 46.dp,
                backgroundColor = theme.surface,
                bottomShadowColor = theme.cubeShadow,
                borderColor = theme.surfaceBorder,
                testTag = "clear_grid_button"
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Grid",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Check / Validate Button
            CubicalButton(
                text = "VALIDATE",
                onClick = { validatePuzzle() },
                backgroundColor = theme.arrowUpColor,
                bottomShadowColor = Color(0xFF0284C7),
                borderColor = Color(0xFF0F172A),
                textColor = Color(0xFF0F172A),
                fontSize = 13,
                height = 46.dp,
                modifier = Modifier.weight(1f),
                testTag = "validate_level_button"
            )

            // Test Play Button
            CubicalButton(
                text = "TEST",
                onClick = {
                    if (validatePuzzle()) {
                        val testLevel = LevelData(
                            id = 9999,
                            levelNumber = 0,
                            tier = LevelTier.EASY,
                            rows = gridSize,
                            cols = gridSize,
                            cubes = cubes.toList(),
                            parMoves = cubes.size
                        )
                        onTestLevelClicked(testLevel)
                    }
                },
                backgroundColor = theme.arrowRightColor,
                bottomShadowColor = Color(0xFFD97706),
                borderColor = Color(0xFF0F172A),
                textColor = Color(0xFF0F172A),
                fontSize = 13,
                height = 46.dp,
                modifier = Modifier.weight(1f),
                testTag = "test_level_button"
            )

            // Save Button
            CubicalButton(
                text = "SAVE",
                onClick = {
                    if (validatePuzzle()) {
                        isSaveDialogOpen = true
                    } else {
                        Toast.makeText(context, "Cannot save: puzzle must be solvable!", Toast.LENGTH_SHORT).show()
                    }
                },
                backgroundColor = theme.accentGreen,
                bottomShadowColor = Color(0xFF15803D),
                borderColor = Color(0xFF0F172A),
                textColor = Color(0xFF0F172A),
                fontSize = 13,
                height = 46.dp,
                modifier = Modifier.weight(1f),
                testTag = "save_level_button"
            )
        }

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }

    // Save Dialog
    if (isSaveDialogOpen) {
        AlertDialog(
            onDismissRequest = { isSaveDialogOpen = false },
            title = {
                Text(
                    text = "Save Custom Puzzle",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter a name for your custom puzzle:",
                        color = theme.textSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = levelNameInput,
                        onValueChange = { levelNameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedBorderColor = theme.accentGold,
                            unfocusedBorderColor = theme.surfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("level_name_input")
                    )
                }
            },
            confirmButton = {
                CubicalButton(
                    text = "SAVE PUZZLE",
                    onClick = {
                        val custom = CustomLevel(
                            id = UUID.randomUUID().toString(),
                            name = levelNameInput.trim().ifEmpty { "My Puzzle" },
                            rows = gridSize,
                            cols = gridSize,
                            cubes = cubes.toList()
                        )
                        preferences.saveCustomLevel(custom)
                        isSaveDialogOpen = false
                        Toast.makeText(context, "Puzzle saved to Level Select!", Toast.LENGTH_SHORT).show()
                        onBackClicked()
                    },
                    backgroundColor = theme.accentGreen,
                    bottomShadowColor = Color(0xFF15803D),
                    borderColor = Color(0xFF0F172A),
                    textColor = Color(0xFF0F172A),
                    fontSize = 13,
                    height = 44.dp,
                    testTag = "confirm_save_button"
                )
            },
            dismissButton = {
                CubicalButton(
                    text = "CANCEL",
                    onClick = { isSaveDialogOpen = false },
                    backgroundColor = theme.surface,
                    bottomShadowColor = theme.cubeShadow,
                    borderColor = theme.surfaceBorder,
                    textColor = theme.textPrimary,
                    fontSize = 13,
                    height = 44.dp,
                    testTag = "cancel_save_button"
                )
            },
            containerColor = theme.surface
        )
    }
}
