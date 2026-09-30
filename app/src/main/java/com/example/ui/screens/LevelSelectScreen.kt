package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.config.GameConfig
import com.example.data.GamePreferences
import com.example.model.LevelTier
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton

@Composable
fun LevelSelectScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onLevelSelected: (levelNumber: Int) -> Unit,
    onCustomLevelSelected: (customLevelId: String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    val playerUnlocked = preferences.unlockedLevel.coerceIn(1, GameConfig.MAX_LEVELS)

    // Compute chapter start (1, 101, 201, ... 901)
    fun getChapterStartForLevel(level: Int): Int {
        val chapterIdx = (level - 1) / 100
        return (chapterIdx * 100) + 1
    }

    // Compute pack start (e.g. 1, 26, 51, 76 for chapter 1)
    fun getPackStartForLevel(level: Int): Int {
        val chStart = getChapterStartForLevel(level)
        val packIdx = ((level - chStart) / 25).coerceIn(0, 3)
        return chStart + (packIdx * 25)
    }

    var selectedChapterStart by remember { mutableIntStateOf(getChapterStartForLevel(playerUnlocked)) }
    var selectedPackStart by remember { mutableIntStateOf(getPackStartForLevel(playerUnlocked)) }
    var isCustomTabSelected by remember { mutableStateOf(false) }
    var selectedTierFilter by remember { mutableStateOf<LevelTier?>(null) }

    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpInputText by remember { mutableStateOf("") }

    val customLevels = preferences.getCustomLevels()

    if (showJumpDialog) {
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = {
                Text(
                    text = "Jump to Level (1-1000)",
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter any level number up to 1000:",
                        color = theme.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jumpInputText,
                        onValueChange = { input ->
                            jumpInputText = input.filter { it.isDigit() }.take(4)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("e.g. 500") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lvl = jumpInputText.toIntOrNull()
                        if (lvl != null && lvl in 1..GameConfig.MAX_LEVELS) {
                            selectedChapterStart = getChapterStartForLevel(lvl)
                            selectedPackStart = getPackStartForLevel(lvl)
                            isCustomTabSelected = false
                            selectedTierFilter = null
                            showJumpDialog = false
                            if (lvl <= preferences.unlockedLevel) {
                                onLevelSelected(lvl)
                            }
                        }
                    }
                ) {
                    Text("GO", fontWeight = FontWeight.Black, color = theme.arrowUpColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("level_select_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CubicalIconButton(
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    onBackClicked()
                },
                size = 40.dp,
                backgroundColor = theme.surface,
                bottomShadowColor = theme.cubeShadow,
                borderColor = Color(0xFF0F172A),
                testTag = "back_button"
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SELECT LEVEL",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "1,000 Levels • Random Challenge Variety",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Jump button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.arrowUpColor)
                    .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .clickable {
                        soundManager.playTap(preferences.isSoundEnabled)
                        jumpInputText = ""
                        showJumpDialog = true
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("jump_level_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Jump",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "JUMP",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Chapters Tab Bar (1-100, 101-200, ..., 901-1000, and Custom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chapterStarts = (1..GameConfig.MAX_LEVELS step 100).toList()
            chapterStarts.forEach { chStart ->
                val chEnd = (chStart + 99).coerceAtMost(GameConfig.MAX_LEVELS)
                val isSelected = !isCustomTabSelected && selectedChapterStart == chStart
                val hasPlayerLevel = playerUnlocked in chStart..chEnd

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) theme.arrowUpColor else theme.surface)
                        .border(
                            2.dp,
                            if (isSelected) Color(0xFF0F172A) else if (hasPlayerLevel) theme.accentGold else theme.surfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            soundManager.playTap(preferences.isSoundEnabled)
                            isCustomTabSelected = false
                            selectedChapterStart = chStart
                            selectedPackStart = if (playerUnlocked in chStart..chEnd) {
                                getPackStartForLevel(playerUnlocked)
                            } else {
                                chStart
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("chapter_tab_$chStart"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasPlayerLevel) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) Color.White else theme.accentGold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "$chStart–$chEnd",
                            color = if (isSelected) Color.White else theme.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Custom levels tab
            if (customLevels.isNotEmpty()) {
                val isSelected = isCustomTabSelected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) theme.accentGold else theme.surface)
                        .border(
                            2.dp,
                            if (isSelected) Color(0xFF0F172A) else theme.surfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            soundManager.playTap(preferences.isSoundEnabled)
                            isCustomTabSelected = true
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("chapter_tab_custom"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My (${customLevels.size})",
                        color = if (isSelected) Color(0xFF0F172A) else theme.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Sub-Pack Selector Bar (Packs of 25 levels) + Difficulty Filter
        if (!isCustomTabSelected) {
            val packStarts = (selectedChapterStart..(selectedChapterStart + 99) step 25)
                .filter { it <= GameConfig.MAX_LEVELS }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                packStarts.forEach { pStart ->
                    val pEnd = (pStart + 24).coerceAtMost(GameConfig.MAX_LEVELS)
                    val isPackSelected = selectedPackStart == pStart
                    val hasCurrentLevel = playerUnlocked in pStart..pEnd

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPackSelected) Color(0xFF0F172A) else theme.surface)
                            .border(
                                1.5.dp,
                                if (hasCurrentLevel && !isPackSelected) theme.accentGold else if (isPackSelected) Color(0xFF0F172A) else theme.surfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                soundManager.playTap(preferences.isSoundEnabled)
                                selectedPackStart = pStart
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (hasCurrentLevel) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(theme.accentGold)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = "$pStart–$pEnd",
                                color = if (isPackSelected) Color.White else theme.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Difficulty Filter Chips (ALL, Novice, Normal, Hard, Expert)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAllSelected = selectedTierFilter == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isAllSelected) theme.arrowUpColor.copy(alpha = 0.2f) else theme.surface)
                        .border(
                            1.dp,
                            if (isAllSelected) theme.arrowUpColor else theme.surfaceBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            soundManager.playTap(preferences.isSoundEnabled)
                            selectedTierFilter = null
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ALL",
                        color = if (isAllSelected) theme.arrowUpColor else theme.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LevelTier.values().forEach { tier ->
                    val isTierSelected = selectedTierFilter == tier
                    val tierColor = Color(tier.badgeColorHex)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTierSelected) tierColor.copy(alpha = 0.2f) else theme.surface)
                            .border(
                                1.dp,
                                if (isTierSelected) tierColor else theme.surfaceBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                soundManager.playTap(preferences.isSoundEnabled)
                                selectedTierFilter = if (isTierSelected) null else tier
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(tierColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tier.title,
                                color = if (isTierSelected) tierColor else theme.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Level Cards Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (!isCustomTabSelected) {
                val packStart = selectedPackStart
                val packEnd = (packStart + 24).coerceAtMost(GameConfig.MAX_LEVELS)
                val rawLevels = (packStart..packEnd).toList()
                val levels = if (selectedTierFilter == null) {
                    rawLevels
                } else {
                    rawLevels.filter { LevelTier.fromLevel(it) == selectedTierFilter }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(levels) { lvl ->
                        val isUnlocked = lvl <= preferences.unlockedLevel
                        val stars = preferences.getStarsForLevel(lvl)
                        val bestMoves = preferences.getBestMovesForLevel(lvl)

                        LevelCardItem(
                            levelNumber = lvl,
                            isUnlocked = isUnlocked,
                            stars = stars,
                            bestMoves = bestMoves,
                            theme = theme,
                            onClick = {
                                if (isUnlocked) {
                                    soundManager.playTap(preferences.isSoundEnabled)
                                    onLevelSelected(lvl)
                                } else {
                                    soundManager.playBlocked(preferences.isSoundEnabled)
                                    soundManager.vibrateBlocked(preferences.isVibrationEnabled)
                                }
                            }
                        )
                    }
                }
            } else {
                // Custom levels list
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(customLevels) { customLvl ->
                        CubicalCard(
                            backgroundColor = Color.White,
                            borderColor = Color(0xFF0F172A),
                            shadowColor = theme.cubeShadow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    soundManager.playTap(preferences.isSoundEnabled)
                                    onCustomLevelSelected(customLvl.id)
                                }
                                .testTag("custom_level_${customLvl.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(
                                    text = customLvl.name,
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${customLvl.rows}x${customLvl.cols} • ${customLvl.cubes.size} Cubes",
                                    color = theme.textSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = theme.accentGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PLAY",
                                        color = theme.accentGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }
}

@Composable
private fun LevelCardItem(
    levelNumber: Int,
    isUnlocked: Boolean,
    stars: Int,
    bestMoves: Int,
    theme: GameConfig.ThemeColors,
    onClick: () -> Unit
) {
    val tier = LevelTier.fromLevel(levelNumber)
    val tierColor = Color(tier.badgeColorHex)

    val cardBg = if (isUnlocked) Color.White else Color(0xFFF8FAFC)
    val borderCol = if (isUnlocked) {
        if (stars > 0) theme.accentGold else tierColor
    } else Color(0xFFE2E8F0)
    val shadowCol = theme.cubeShadow

    CubicalCard(
        backgroundColor = cardBg,
        borderColor = borderCol,
        shadowColor = shadowCol,
        shadowDepth = 3.dp,
        cornerRadius = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("level_cell_$levelNumber")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = "$levelNumber",
                    color = theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Randomized difficulty badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(tierColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = tier.title.uppercase(),
                        color = tierColor,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Star rating icons (1-3 stars)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    for (s in 1..3) {
                        val earned = s <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (earned) theme.accentGold else Color(0xFFCBD5E1),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$levelNumber",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tier.title.uppercase(),
                    color = Color(0xFF94A3B8),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
