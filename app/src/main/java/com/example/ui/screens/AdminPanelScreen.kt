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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.config.GameConfig
import com.example.data.GamePreferences
import com.example.ui.components.CubicalButton
import com.example.ui.components.CubicalCard
import com.example.ui.components.CubicalIconButton

@Composable
fun AdminPanelScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = GameConfig.THEMES[preferences.currentTheme]
        ?: GameConfig.THEMES[GameConfig.ThemeStyle.WHITE_VIBRANT]!!

    var rewardTitleInput by remember { mutableStateOf("") }
    var rewardNoteInput by remember { mutableStateOf("Verified & Granted by Admin") }

    var noticeInput by remember { mutableStateOf(preferences.adminNotice) }

    var newCodeInput by remember { mutableStateOf("") }
    var newCodeRewardInput by remember { mutableStateOf("") }

    var newPinInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("admin_panel_screen")
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
                testTag = "admin_back_button"
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
                    text = "ADMIN CONTROL PANEL",
                    color = theme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Player: ${preferences.playerId} (Lvl ${preferences.unlockedLevel})",
                    color = Color(0xFF16A34A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
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
            // 1. Grant Reward Directly to Player
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
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = theme.arrowUpColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GRANT REWARD TO PLAYER",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = rewardTitleInput,
                            onValueChange = { rewardTitleInput = it },
                            label = { Text("Reward Title") },
                            placeholder = { Text("e.g. Milestone 100 Cash Prize / VIP Pass") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = rewardNoteInput,
                            onValueChange = { rewardNoteInput = it },
                            label = { Text("Admin Note / Proof") },
                            placeholder = { Text("e.g. Sent via UPI / Special Bonus") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CubicalButton(
                            text = "GRANT REWARD NOW",
                            onClick = {
                                if (rewardTitleInput.isNotBlank()) {
                                    preferences.grantAdminReward(
                                        title = rewardTitleInput.trim(),
                                        note = rewardNoteInput.trim()
                                    )
                                    soundManager.playLevelWon(preferences.isSoundEnabled)
                                    Toast.makeText(context, "Reward granted to Player!", Toast.LENGTH_SHORT).show()
                                    rewardTitleInput = ""
                                } else {
                                    Toast.makeText(context, "Please enter a reward title!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            backgroundColor = Color(0xFF16A34A),
                            bottomShadowColor = Color(0xFF15803D),
                            borderColor = Color(0xFF0F172A),
                            textColor = Color.White,
                            fontSize = 14,
                            height = 48.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 2. Admin Public Reward Notice
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
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ADMIN REWARD ANNOUNCEMENT",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = noticeInput,
                            onValueChange = { noticeInput = it },
                            label = { Text("Public Announcement") },
                            placeholder = { Text("Message shown to all players...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        CubicalButton(
                            text = "UPDATE NOTICE",
                            onClick = {
                                preferences.adminNotice = noticeInput.trim()
                                soundManager.playTap(preferences.isSoundEnabled)
                                Toast.makeText(context, "Admin announcement updated!", Toast.LENGTH_SHORT).show()
                            },
                            backgroundColor = Color(0xFFD97706),
                            bottomShadowColor = Color(0xFFB45309),
                            borderColor = Color(0xFF0F172A),
                            textColor = Color.White,
                            fontSize = 14,
                            height = 46.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Issue / Create New Promo Voucher Code
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
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = theme.arrowRightColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CREATE REWARD VOUCHER CODE",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newCodeInput,
                            onValueChange = { newCodeInput = it.uppercase() },
                            label = { Text("Code (e.g. LUCKY100)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newCodeRewardInput,
                            onValueChange = { newCodeRewardInput = it },
                            label = { Text("Reward Name") },
                            placeholder = { Text("e.g. Master Crown Badge") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        CubicalButton(
                            text = "REGISTER CODE",
                            onClick = {
                                if (newCodeInput.isNotBlank() && newCodeRewardInput.isNotBlank()) {
                                    preferences.addCustomAdminCode(newCodeInput, newCodeRewardInput)
                                    soundManager.playTap(preferences.isSoundEnabled)
                                    Toast.makeText(context, "Code ${newCodeInput.uppercase()} registered!", Toast.LENGTH_SHORT).show()
                                    newCodeInput = ""
                                    newCodeRewardInput = ""
                                } else {
                                    Toast.makeText(context, "Please fill both Code and Reward!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            backgroundColor = theme.arrowRightColor,
                            bottomShadowColor = Color(0xFFD97706),
                            borderColor = Color(0xFF0F172A),
                            textColor = Color(0xFF0F172A),
                            fontSize = 14,
                            height = 46.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Level Unlocker / Tester
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
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = theme.arrowUpColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LEVEL UNLOCKER / TESTER",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Instantly set unlocked level to test rewards & milestones:",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val testMilestones = listOf(1, 25, 50, 100, 500, 1000)
                            testMilestones.take(3).forEach { lvl ->
                                CubicalButton(
                                    text = "Lvl $lvl",
                                    onClick = {
                                        preferences.unlockedLevel = lvl
                                        soundManager.playTap(preferences.isSoundEnabled)
                                        Toast.makeText(context, "Unlocked level set to $lvl", Toast.LENGTH_SHORT).show()
                                    },
                                    backgroundColor = theme.surface,
                                    bottomShadowColor = theme.cubeShadow,
                                    borderColor = Color(0xFF0F172A),
                                    textColor = Color(0xFF0F172A),
                                    fontSize = 12,
                                    height = 40.dp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val testMilestones = listOf(1, 25, 50, 100, 500, 1000)
                            testMilestones.takeLast(3).forEach { lvl ->
                                CubicalButton(
                                    text = "Lvl $lvl",
                                    onClick = {
                                        preferences.unlockedLevel = lvl
                                        soundManager.playTap(preferences.isSoundEnabled)
                                        Toast.makeText(context, "Unlocked level set to $lvl", Toast.LENGTH_SHORT).show()
                                    },
                                    backgroundColor = theme.surface,
                                    bottomShadowColor = theme.cubeShadow,
                                    borderColor = Color(0xFF0F172A),
                                    textColor = Color(0xFF0F172A),
                                    fontSize = 12,
                                    height = 40.dp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Change Admin PIN
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
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CHANGE ADMIN PIN",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { newPinInput = it },
                            label = { Text("New PIN (Current: ${preferences.adminPin})") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        CubicalButton(
                            text = "UPDATE PIN",
                            onClick = {
                                if (newPinInput.length >= 4) {
                                    preferences.adminPin = newPinInput.trim()
                                    soundManager.playTap(preferences.isSoundEnabled)
                                    Toast.makeText(context, "Admin PIN updated successfully!", Toast.LENGTH_SHORT).show()
                                    newPinInput = ""
                                } else {
                                    Toast.makeText(context, "PIN must be at least 4 characters!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            backgroundColor = Color(0xFF0F172A),
                            bottomShadowColor = Color.Black,
                            borderColor = Color(0xFF0F172A),
                            textColor = Color.White,
                            fontSize = 13,
                            height = 44.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
