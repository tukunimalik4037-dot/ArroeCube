package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.config.GameConfig
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import kotlinx.coroutines.delay

@Composable
fun LevelCompleteDialog(
    levelNumber: Int,
    starsEarned: Int,
    movesTaken: Int,
    parMoves: Int,
    theme: GameConfig.ThemeColors,
    hasNextLevel: Boolean,
    onReplayClicked: () -> Unit,
    onNextLevelClicked: () -> Unit,
    onLevelSelectClicked: () -> Unit
) {
    val star1Scale = remember { Animatable(0f) }
    val star2Scale = remember { Animatable(0f) }
    val star3Scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(150)
        star1Scale.animateTo(1.2f, tween(180, easing = FastOutSlowInEasing))
        star1Scale.animateTo(1.0f, tween(100))

        if (starsEarned >= 2) {
            delay(120)
            star2Scale.animateTo(1.2f, tween(180, easing = FastOutSlowInEasing))
            star2Scale.animateTo(1.0f, tween(100))
        }

        if (starsEarned >= 3) {
            delay(120)
            star3Scale.animateTo(1.2f, tween(180, easing = FastOutSlowInEasing))
            star3Scale.animateTo(1.0f, tween(100))
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        CubicalCard(
            backgroundColor = theme.surface,
            borderColor = theme.accentGold,
            shadowColor = theme.cubeShadow,
            shadowDepth = 6.dp,
            cornerRadius = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("level_complete_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trophy Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(theme.accentGold)
                        .border(3.dp, Color(0xFF0F172A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Victory",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Victory Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LEVEL $levelNumber SOLVED!",
                        color = theme.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (starsEarned == 3) "PERFECT RUN!" else "LEVEL COMPLETED",
                        color = theme.accentGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Animated Stars (1, 2, 3)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Star 1
                    Box(modifier = Modifier.scale(star1Scale.value)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star 1",
                            tint = if (starsEarned >= 1) theme.accentGold else Color(0xFF475569),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    // Star 2 (Middle, slightly larger)
                    Box(modifier = Modifier.scale(star2Scale.value)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star 2",
                            tint = if (starsEarned >= 2) theme.accentGold else Color(0xFF475569),
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    // Star 3
                    Box(modifier = Modifier.scale(star3Scale.value)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star 3",
                            tint = if (starsEarned >= 3) theme.accentGold else Color(0xFF475569),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Stats Panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.5.dp, theme.surfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MOVES",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$movesTaken / $parMoves",
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ADMIN REWARD",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ELIGIBLE ★",
                                color = Color(0xFF16A34A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action buttons
                if (hasNextLevel) {
                    CubicalButton(
                        text = "NEXT LEVEL",
                        onClick = onNextLevelClicked,
                        backgroundColor = theme.accentGreen,
                        bottomShadowColor = Color(0xFF15803D),
                        borderColor = Color(0xFF0F172A),
                        textColor = Color(0xFF0F172A),
                        fontSize = 16,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "next_level_button"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CubicalButton(
                        text = "REPLAY",
                        onClick = onReplayClicked,
                        backgroundColor = theme.surface,
                        bottomShadowColor = theme.cubeShadow,
                        borderColor = theme.surfaceBorder,
                        textColor = theme.textPrimary,
                        fontSize = 13,
                        height = 46.dp,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = theme.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "replay_level_button"
                    )

                    CubicalButton(
                        text = "LEVELS",
                        onClick = onLevelSelectClicked,
                        backgroundColor = theme.surface,
                        bottomShadowColor = theme.cubeShadow,
                        borderColor = theme.surfaceBorder,
                        textColor = theme.textPrimary,
                        fontSize = 13,
                        height = 46.dp,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ViewModule,
                                contentDescription = null,
                                tint = theme.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "all_levels_button"
                    )
                }
            }
        }
    }
}
