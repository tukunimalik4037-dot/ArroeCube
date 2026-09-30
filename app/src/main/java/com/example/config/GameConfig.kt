package com.example.config

import androidx.compose.ui.graphics.Color

/**
 * Game configuration for ArrowCube.
 * Contains game constants, AdMob test IDs, coin economic balances, and themes.
 * Strictly flat colors, NO gradients.
 */
object GameConfig {
    const val GAME_NAME = "ArrowCube"
    const val GAME_VERSION = "1.1.0"
    const val MAX_LEVELS = 1000

    // AdMob Unit IDs
    const val ADMOB_BANNER_ID = "ca-app-pub-4264172536899087/6660989062"
    const val ADMOB_INTERSTITIAL_ID = "ca-app-pub-4264172536899087/5347907398"
    const val ADMOB_REWARDED_ID = "ca-app-pub-4264172536899087/6035137258"

    // Ad switches & frequency
    var ADS_ENABLED = true
    const val INTERSTITIAL_EVERY_N_LEVELS = 2

    // Admin & Rewards configuration
    const val DEFAULT_ADMIN_PIN = "7777"
    const val ADMIN_CONTACT_EMAIL = "admin@arrowcube.game"
    
    // Built-in Admin Reward Promo Codes that grant official rewards
    val INITIAL_ADMIN_CODES = mapOf(
        "ADMIN2026" to "VIP Grandmaster Badge",
        "LEVELWIN" to "Master Solver Certificate",
        "ARROWPRO" to "Pro Arrow Stylist Badge",
        "BONUSGIFT" to "Admin Special Star Chest"
    )

    // Color Palettes (Flat solid vibrant colours only - strictly NO gradients)
    enum class ThemeStyle(val displayName: String) {
        WHITE_VIBRANT("Pure White & Vivid"),
        CLASSIC_VIBRANT("Pure White"),
        CANDY_PASTEL("Candy Pastel"),
        NEON_CYBER("Neon Mint"),
        DARK_SLATE("Dark Slate")
    }

    data class ThemeColors(
        val background: Color,
        val surface: Color,
        val surfaceBorder: Color,
        val textPrimary: Color,
        val textSecondary: Color,
        val cubeFace: Color,
        val cubeBevel: Color,
        val cubeShadow: Color,
        val boardTray: Color,
        val boardSlot: Color,
        val arrowUpColor: Color,
        val arrowRightColor: Color,
        val arrowDownColor: Color,
        val arrowLeftColor: Color,
        val accentGold: Color,
        val accentGreen: Color,
        val accentRed: Color
    )

    private val WhiteVibrantTheme = ThemeColors(
        background = Color(0xFFF8FAFC),      // Crisp, clean modern luminous white
        surface = Color(0xFFFFFFFF),         // Clean pure white card surface
        surfaceBorder = Color(0xFFCBD5E1),   // Crisp light slate border
        textPrimary = Color(0xFF0F172A),     // Deep slate black text
        textSecondary = Color(0xFF64748B),   // Muted slate secondary text
        cubeFace = Color(0xFFFFFFFF),        // White ceramic 3D tactile block
        cubeBevel = Color(0xFFF1F5F9),       // Top bevel edge highlight
        cubeShadow = Color(0xFF94A3B8),      // Solid 3D tactile cast shadow
        boardTray = Color(0xFFF1F5F9),       // Sunken puzzle board tray
        boardSlot = Color(0xFFE2E8F0),       // Recessed empty grid socket
        arrowUpColor = Color(0xFF0284C7),     // Rich Vivid Sky Blue
        arrowRightColor = Color(0xFFF59E0B),  // Rich Sunshine Gold / Amber
        arrowDownColor = Color(0xFFE11D48),   // Vibrant Ruby Coral
        arrowLeftColor = Color(0xFF059669),   // Vibrant Emerald Green
        accentGold = Color(0xFFF59E0B),
        accentGreen = Color(0xFF16A34A),
        accentRed = Color(0xFFDC2626)
    )

    val THEMES = mapOf(
        ThemeStyle.WHITE_VIBRANT to WhiteVibrantTheme,
        ThemeStyle.CLASSIC_VIBRANT to WhiteVibrantTheme,
        ThemeStyle.CANDY_PASTEL to ThemeColors(
            background = Color(0xFFFFFBEB),      // Warm playful cream white
            surface = Color(0xFFFFFFFF),
            surfaceBorder = Color(0xFFFDE68A),
            textPrimary = Color(0xFF1E293B),
            textSecondary = Color(0xFF78716C),
            cubeFace = Color(0xFFFFFFFF),
            cubeBevel = Color(0xFFFEF3C7),
            cubeShadow = Color(0xFFFCD34D),
            boardTray = Color(0xFFFEF3C7),
            boardSlot = Color(0xFFFDE68A),
            arrowUpColor = Color(0xFF38BDF8),     // Baby Blue
            arrowRightColor = Color(0xFFFB923C),  // Tangerine
            arrowDownColor = Color(0xFFF43F5E),   // Strawberry Rose
            arrowLeftColor = Color(0xFF10B981),   // Mint Green
            accentGold = Color(0xFFF59E0B),
            accentGreen = Color(0xFF10B981),
            accentRed = Color(0xFFEF4444)
        ),
        ThemeStyle.NEON_CYBER to ThemeColors(
            background = Color(0xFFF0FDF4),      // Ultra fresh pale mint white
            surface = Color(0xFFFFFFFF),
            surfaceBorder = Color(0xFFBBF7D0),
            textPrimary = Color(0xFF064E3B),
            textSecondary = Color(0xFF047857),
            cubeFace = Color(0xFFFFFFFF),
            cubeBevel = Color(0xFFDCFCE7),
            cubeShadow = Color(0xFF86EFAC),
            boardTray = Color(0xFFDCFCE7),
            boardSlot = Color(0xFFBBF7D0),
            arrowUpColor = Color(0xFF0EA5E9),     // Cyan
            arrowRightColor = Color(0xFFEAB308),  // Neon Yellow
            arrowDownColor = Color(0xFFFB7185),   // Neon Pink
            arrowLeftColor = Color(0xFF10B981),   // Vivid Emerald
            accentGold = Color(0xFFEAB308),
            accentGreen = Color(0xFF059669),
            accentRed = Color(0xFFE11D48)
        ),
        ThemeStyle.DARK_SLATE to ThemeColors(
            background = Color(0xFF0F172A),      // Dark theme option
            surface = Color(0xFF1E293B),
            surfaceBorder = Color(0xFF334155),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            cubeFace = Color(0xFF1E293B),
            cubeBevel = Color(0xFF334155),
            cubeShadow = Color(0xFF090D16),
            boardTray = Color(0xFF161E2E),
            boardSlot = Color(0xFF0F172A),
            arrowUpColor = Color(0xFF38BDF8),
            arrowRightColor = Color(0xFFF59E0B),
            arrowDownColor = Color(0xFFEC4899),
            arrowLeftColor = Color(0xFF10B981),
            accentGold = Color(0xFFFBBF24),
            accentGreen = Color(0xFF22C55E),
            accentRed = Color(0xFFEF4444)
        )
    )
}
