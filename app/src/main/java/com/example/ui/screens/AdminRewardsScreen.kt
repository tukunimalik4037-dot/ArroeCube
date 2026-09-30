package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun AdminRewardsScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    adMobManager: AdMobManager,
    onBackClicked: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    var inputCode by remember { mutableStateOf("") }
    var rewardsList by remember { mutableStateOf(preferences.getAdminRewards()) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminPinDialog = false
                pinError = false
                enteredPin = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = theme.arrowUpColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Admin Access", fontWeight = FontWeight.Bold, color = theme.textPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter Admin PIN to manage & issue rewards:",
                        color = theme.textSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError,
                        label = { Text("PIN") },
                        placeholder = { Text("Default: 7777") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text(
                            text = "Incorrect Admin PIN!",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (enteredPin.trim() == preferences.adminPin.trim()) {
                            showAdminPinDialog = false
                            enteredPin = ""
                            pinError = false
                            onOpenAdminPanel()
                        } else {
                            pinError = true
                            soundManager.playBlocked(preferences.isSoundEnabled)
                        }
                    }
                ) {
                    Text("LOGIN", fontWeight = FontWeight.Black, color = theme.arrowUpColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminPinDialog = false
                        pinError = false
                        enteredPin = ""
                    }
                ) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("admin_rewards_screen")
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
                    text = "ADMIN REWARDS",
                    color = theme.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Official Rewards Issued by Admin",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Admin Portal Access Button
            CubicalIconButton(
                onClick = {
                    soundManager.playTap(preferences.isSoundEnabled)
                    enteredPin = ""
                    pinError = false
                    showAdminPinDialog = true
                },
                size = 40.dp,
                backgroundColor = Color(0xFF0F172A),
                bottomShadowColor = Color.Black,
                borderColor = Color(0xFF0F172A),
                testTag = "admin_portal_button"
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Portal",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player Identification & Milestone Card
            item {
                CubicalCard(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 4.dp,
                    cornerRadius = 14.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "YOUR PLAYER ID",
                                    color = theme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = preferences.playerId,
                                    color = Color(0xFF0F172A),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Copy Player ID Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.arrowUpColor)
                                    .border(1.5.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .clickable {
                                        soundManager.playTap(preferences.isSoundEnabled)
                                        copyToClipboard("Player ID", preferences.playerId)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "COPY ID",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(10.dp))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LEVEL REACHED",
                                    color = theme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${preferences.unlockedLevel} / 1000",
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TOTAL STARS",
                                    color = theme.textSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = theme.accentGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${preferences.getTotalStars()}",
                                        color = Color(0xFF0F172A),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Admin Notice Banner
            if (preferences.adminNotice.isNotEmpty()) {
                item {
                    CubicalCard(
                        backgroundColor = Color(0xFFFEF3C7),
                        borderColor = Color(0xFFD97706),
                        shadowColor = theme.cubeShadow,
                        shadowDepth = 3.dp,
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NOTICE FROM ADMIN",
                                    color = Color(0xFFB45309),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = preferences.adminNotice,
                                    color = Color(0xFF78350F),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Redeem Code Section
            item {
                CubicalCard(
                    backgroundColor = Color.White,
                    borderColor = Color(0xFF0F172A),
                    shadowColor = theme.cubeShadow,
                    shadowDepth = 4.dp,
                    cornerRadius = 14.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "REDEEM ADMIN CODE / VOUCHER",
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Enter promo codes or vouchers given by Admin:",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it.uppercase() },
                                singleLine = true,
                                placeholder = { Text("e.g. ADMIN2026", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            CubicalButton(
                                text = "REDEEM",
                                onClick = {
                                    if (inputCode.isNotBlank()) {
                                        val reward = preferences.redeemAdminCode(inputCode)
                                        if (reward != null) {
                                            soundManager.playLevelWon(preferences.isSoundEnabled)
                                            soundManager.vibrateSuccess(preferences.isVibrationEnabled)
                                            rewardsList = preferences.getAdminRewards()
                                            Toast.makeText(
                                                context,
                                                "Success! Admin Reward Claimed: $reward",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            inputCode = ""
                                        } else {
                                            soundManager.playBlocked(preferences.isSoundEnabled)
                                            Toast.makeText(
                                                context,
                                                "Code invalid or already redeemed!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                backgroundColor = theme.accentGreen,
                                bottomShadowColor = Color(0xFF15803D),
                                borderColor = Color(0xFF0F172A),
                                textColor = Color.White,
                                fontSize = 13,
                                height = 50.dp
                            )
                        }
                    }
                }
            }

            // Submit Claim to Admin Button
            item {
                CubicalButton(
                    text = "REQUEST REWARD FROM ADMIN",
                    onClick = {
                        soundManager.playTap(preferences.isSoundEnabled)
                        val claimText = """
                            🏆 ArrowCube Player Reward Claim
                            -------------------------------
                            Player ID: ${preferences.playerId}
                            Current Level: ${preferences.unlockedLevel} / 1000
                            Total Stars: ${preferences.getTotalStars()}
                            Verified Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}
                            
                            Please verify my milestone and issue my reward!
                        """.trimIndent()

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "ArrowCube Reward Claim - ${preferences.playerId}")
                            putExtra(Intent.EXTRA_TEXT, claimText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Send Claim to Admin via"))
                    },
                    backgroundColor = Color(0xFF0284C7),
                    bottomShadowColor = Color(0xFF0369A1),
                    borderColor = Color(0xFF0F172A),
                    textColor = Color.White,
                    fontSize = 15,
                    height = 54.dp,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Granted Admin Rewards Section Title
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
                        text = "REWARDS GRANTED BY ADMIN (${rewardsList.size})",
                        color = theme.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Granted Rewards List
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
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Admin Rewards Granted Yet",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Complete levels and share your Player ID with the Admin to receive special rewards!",
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
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7))
                                    .border(2.dp, Color(0xFFD97706), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rwd.title,
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = rwd.note,
                                    color = Color(0xFF16A34A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: ${rwd.id} • ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(rwd.timestamp))}",
                                    color = theme.textSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VERIFIED",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
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

        // Bottom AdMob Banner
        AdMobBannerView(adMobManager = adMobManager)
    }
}
