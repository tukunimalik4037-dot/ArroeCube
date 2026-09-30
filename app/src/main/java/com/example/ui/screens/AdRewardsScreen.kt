package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.audio.SoundManager
import com.example.config.GameConfig
import com.example.data.GamePreferences
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdRewardsScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!
    val isOnline by adMobManager.isOnline.collectAsState()

    var updateCounter by remember { mutableIntStateOf(0) }
    val rewardsList = remember(updateCounter) { preferences.getAdRewards() }
    val unlockedThemes = remember(updateCounter) { preferences.getUnlockedThemesByAds() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("ad_rewards_screen")
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
                testTag = "rewards_back_button"
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
                    text = "AD REWARDS CENTER",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Watch Ads to Unlock Exclusive Rewards",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Ads Watched Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${preferences.adsWatchedCount}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Overview Strip
            item {
                CubicalCard(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 3.dp,
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ADS WATCHED",
                                color = theme.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preferences.adsWatchedCount}",
                                color = Color(0xFF0F172A),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SKIP TICKETS",
                                color = theme.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preferences.levelSkipsAvailable}",
                                color = theme.arrowUpColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "REWARDS WON",
                                color = theme.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${rewardsList.size}",
                                color = theme.accentGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Reward 1: Watch Ad for Level Skip Ticket
            item {
                CubicalCard(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 4.dp,
                    cornerRadius = 14.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE))
                                    .border(2.dp, Color(0xFF0284C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LEVEL SKIP TICKET",
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Stuck on a tricky level? Watch an ad to earn 1 skip ticket!",
                                    color = theme.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CubicalButton(
                            text = "WATCH AD (+1 SKIP TICKET)",
                            onClick = {
                                adMobManager.showRewarded(
                                    onRewarded = {
                                        preferences.grantLevelSkipTicket()
                                        updateCounter++
                                        soundManager.playLevelWon(preferences.isSoundEnabled)
                                        Toast.makeText(context, "+1 Level Skip Ticket Earned!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            backgroundColor = Color(0xFF0284C7),
                            bottomShadowColor = Color(0xFF0369A1),
                            borderColor = Color(0xFF0F172A),
                            textColor = Color.White,
                            fontSize = 13,
                            height = 46.dp,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "watch_ad_skip_ticket_button"
                        )
                    }
                }
            }

            // Reward 2: Watch Ad to Open Lucky Mystery Chest
            item {
                CubicalCard(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 4.dp,
                    cornerRadius = 14.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7))
                                    .border(2.dp, Color(0xFFD97706), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LUCKY MYSTERY CHEST",
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Watch an ad to open the mystery chest and win rare badges!",
                                    color = theme.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CubicalButton(
                            text = "WATCH AD (OPEN CHEST)",
                            onClick = {
                                adMobManager.showRewarded(
                                    onRewarded = {
                                        val prize = preferences.openMysteryChest()
                                        updateCounter++
                                        soundManager.playLevelWon(preferences.isSoundEnabled)
                                        Toast.makeText(context, "Congratulations! Won: $prize", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            backgroundColor = Color(0xFFF59E0B),
                            bottomShadowColor = Color(0xFFD97706),
                            borderColor = Color(0xFF0F172A),
                            textColor = Color(0xFF0F172A),
                            fontSize = 13,
                            height = 46.dp,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "watch_ad_mystery_chest_button"
                        )
                    }
                }
            }

            // Reward 3: Watch Ad to Unlock Premium Themes
            item {
                CubicalCard(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 4.dp,
                    cornerRadius = 14.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFCE7F3))
                                    .border(2.dp, Color(0xFFBE185D), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFFBE185D),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UNLOCK COLOR THEMES",
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Watch 1 ad to instantly unlock vibrant styles:",
                                    color = theme.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val unlockableThemes = listOf(
                            GameConfig.ThemeStyle.CANDY_PASTEL,
                            GameConfig.ThemeStyle.NEON_CYBER,
                            GameConfig.ThemeStyle.DARK_SLATE
                        )

                        unlockableThemes.forEach { tStyle ->
                            val isUnlocked = unlockedThemes.contains(tStyle.name)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tStyle.displayName,
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (isUnlocked) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Unlocked",
                                            tint = Color(0xFF16A34A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "UNLOCKED",
                                            color = Color(0xFF16A34A),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    CubicalButton(
                                        text = "WATCH AD",
                                        onClick = {
                                            adMobManager.showRewarded(
                                                onRewarded = {
                                                    preferences.unlockThemeByAd(tStyle)
                                                    updateCounter++
                                                    soundManager.playLevelWon(preferences.isSoundEnabled)
                                                    Toast.makeText(context, "${tStyle.displayName} Theme Unlocked!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                        backgroundColor = Color(0xFF22C55E),
                                        bottomShadowColor = Color(0xFF15803D),
                                        borderColor = Color(0xFF0F172A),
                                        textColor = Color.White,
                                        fontSize = 11,
                                        height = 36.dp,
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Your Claimed Ad Rewards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = theme.accentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REWARDS WON FROM ADS (${rewardsList.size})",
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (rewardsList.isEmpty()) {
                item {
                    CubicalCard(
                        backgroundColor = Color(0xFFF8FAFC),
                        borderColor = theme.surfaceBorder,
                        shadowColor = theme.cubeShadow,
                        shadowDepth = 2.dp,
                        cornerRadius = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Ad Rewards Claimed Yet",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Watch a rewarded video ad above to unlock skip tickets, themes, and mystery trophies!",
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(rewardsList) { rwd ->
                    CubicalCard(
                        backgroundColor = Color.White,
                        borderColor = Color(0xFF0F172A),
                        shadowColor = theme.cubeShadow,
                        shadowDepth = 3.dp,
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7))
                                    .border(1.5.dp, Color(0xFF16A34A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rwd.title,
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${rwd.type} • ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(rwd.timestamp))}",
                                    color = theme.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Banner Ad
        AdMobBannerView(adMobManager = adMobManager)
    }
}
