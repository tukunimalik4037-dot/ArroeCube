package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.config.GameConfig
import com.example.model.ArrowDirection
import com.example.model.CubeCell
import com.example.model.CustomLevel
import org.json.JSONArray
import org.json.JSONObject

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arrowcube_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_UNLOCKED_LEVEL = "unlocked_level"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_THEME = "current_theme"
        private const val KEY_CUSTOM_LEVELS = "custom_levels_json"
        private const val KEY_LEVELS_PLAYED_SINCE_AD = "levels_played_since_ad"
        private const val KEY_ADS_WATCHED = "ads_watched_count"
        private const val KEY_LEVEL_SKIPS = "level_skips_available"
        private const val KEY_UNLOCKED_THEMES_ADS = "unlocked_themes_ads_json"
        private const val KEY_AD_REWARDS = "ad_rewards_json"
        private const val PREFIX_STAR = "stars_lvl_"
        private const val PREFIX_BEST_MOVES = "best_moves_lvl_"
    }

    var unlockedLevel: Int
        get() = prefs.getInt(KEY_UNLOCKED_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_UNLOCKED_LEVEL, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var isAnimationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ANIMATIONS_ENABLED, value).apply()

    var currentTheme: GameConfig.ThemeStyle
        get() {
            val name = prefs.getString(KEY_THEME, GameConfig.ThemeStyle.WHITE_VIBRANT.name)
            return try {
                GameConfig.ThemeStyle.valueOf(name ?: GameConfig.ThemeStyle.WHITE_VIBRANT.name)
            } catch (e: Exception) {
                GameConfig.ThemeStyle.WHITE_VIBRANT
            }
        }
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var levelsPlayedSinceAd: Int
        get() = prefs.getInt(KEY_LEVELS_PLAYED_SINCE_AD, 0)
        set(value) = prefs.edit().putInt(KEY_LEVELS_PLAYED_SINCE_AD, value).apply()

    var adsWatchedCount: Int
        get() = prefs.getInt(KEY_ADS_WATCHED, 0)
        set(value) = prefs.edit().putInt(KEY_ADS_WATCHED, value).apply()

    var levelSkipsAvailable: Int
        get() = prefs.getInt(KEY_LEVEL_SKIPS, 0)
        set(value) = prefs.edit().putInt(KEY_LEVEL_SKIPS, value.coerceAtLeast(0)).apply()

    fun getTotalStars(): Int {
        var total = 0
        for (i in 1..GameConfig.MAX_LEVELS) {
            total += getStarsForLevel(i)
        }
        return total
    }

    fun getStarsForLevel(levelNumber: Int): Int {
        return prefs.getInt(PREFIX_STAR + levelNumber, 0)
    }

    fun setStarsForLevel(levelNumber: Int, stars: Int) {
        val current = getStarsForLevel(levelNumber)
        if (stars > current) {
            prefs.edit().putInt(PREFIX_STAR + levelNumber, stars.coerceIn(1, 3)).apply()
        }
    }

    fun getBestMovesForLevel(levelNumber: Int): Int {
        return prefs.getInt(PREFIX_BEST_MOVES + levelNumber, 0)
    }

    fun setBestMovesForLevel(levelNumber: Int, moves: Int) {
        val current = getBestMovesForLevel(levelNumber)
        if (current == 0 || moves < current) {
            prefs.edit().putInt(PREFIX_BEST_MOVES + levelNumber, moves).apply()
        }
    }

    fun completeLevel(levelNumber: Int, moves: Int, stars: Int) {
        setStarsForLevel(levelNumber, stars)
        setBestMovesForLevel(levelNumber, moves)

        if (levelNumber >= unlockedLevel && levelNumber < GameConfig.MAX_LEVELS) {
            unlockedLevel = levelNumber + 1
        }
        levelsPlayedSinceAd += 1
    }

    // Ad Rewards System (Rewards earned by watching ads)
    data class AdReward(
        val id: String,
        val title: String,
        val type: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getAdRewards(): List<AdReward> {
        val raw = prefs.getString(KEY_AD_REWARDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<AdReward>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AdReward(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        type = obj.optString("type", "Ad Reward"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addAdReward(title: String, type: String) {
        adsWatchedCount += 1
        val current = getAdRewards().toMutableList()
        val newReward = AdReward(
            id = "ADR-${System.currentTimeMillis() % 100000}",
            title = title,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        current.add(0, newReward)
        try {
            val arr = JSONArray()
            for (r in current) {
                val obj = JSONObject()
                obj.put("id", r.id)
                obj.put("title", r.title)
                obj.put("type", r.type)
                obj.put("timestamp", r.timestamp)
                arr.put(obj)
            }
            prefs.edit().putString(KEY_AD_REWARDS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUnlockedThemesByAds(): Set<String> {
        val raw = prefs.getString(KEY_UNLOCKED_THEMES_ADS, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                set.add(arr.getString(i))
            }
            set
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun unlockThemeByAd(themeStyle: GameConfig.ThemeStyle) {
        val current = getUnlockedThemesByAds().toMutableSet()
        current.add(themeStyle.name)
        try {
            val arr = JSONArray()
            current.forEach { arr.put(it) }
            prefs.edit().putString(KEY_UNLOCKED_THEMES_ADS, arr.toString()).apply()
            addAdReward("Unlocked ${themeStyle.displayName} Theme", "Theme")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun grantLevelSkipTicket() {
        levelSkipsAvailable += 1
        addAdReward("+1 Level Skip Ticket", "Skip Ticket")
    }

    fun openMysteryChest(): String {
        val chestRewards = listOf(
            "👑 Diamond Solver Crown",
            "⚡ Lightning Speed Badge",
            "🛡️ Master Shield Trophy",
            "🌟 Golden Star Medallion",
            "🎯 Arrow Sharpshooter Ribbon",
            "💎 Grandmaster Crystal Badge"
        )
        val selected = chestRewards.random()
        addAdReward(selected, "Mystery Chest")
        return selected
    }

    fun saveCustomLevel(level: CustomLevel) {
        val existing = getCustomLevels().toMutableList()
        existing.removeAll { it.id == level.id }
        existing.add(0, level)
        saveCustomLevelsList(existing)
    }

    fun deleteCustomLevel(levelId: String) {
        val existing = getCustomLevels().toMutableList()
        existing.removeAll { it.id == levelId }
        saveCustomLevelsList(existing)
    }

    fun getCustomLevels(): List<CustomLevel> {
        val raw = prefs.getString(KEY_CUSTOM_LEVELS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<CustomLevel>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val rows = obj.getInt("rows")
                val cols = obj.getInt("cols")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val cubesArr = obj.getJSONArray("cubes")
                val cubes = mutableListOf<CubeCell>()
                for (j in 0 until cubesArr.length()) {
                    val cObj = cubesArr.getJSONObject(j)
                    val cId = cObj.getInt("id")
                    val r = cObj.getInt("row")
                    val c = cObj.getInt("col")
                    val dir = ArrowDirection.valueOf(cObj.getString("dir"))
                    cubes.add(CubeCell(cId, r, c, dir))
                }
                list.add(CustomLevel(id, name, rows, cols, cubes, createdAt))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomLevelsList(levels: List<CustomLevel>) {
        try {
            val jsonArray = JSONArray()
            for (level in levels) {
                val obj = JSONObject()
                obj.put("id", level.id)
                obj.put("name", level.name)
                obj.put("rows", level.rows)
                obj.put("cols", level.cols)
                obj.put("createdAt", level.createdAt)
                val cubesArr = JSONArray()
                for (cube in level.cubes) {
                    val cObj = JSONObject()
                    cObj.put("id", cube.id)
                    cObj.put("row", cube.row)
                    cObj.put("col", cube.col)
                    cObj.put("dir", cube.direction.name)
                    cubesArr.put(cObj)
                }
                obj.put("cubes", cubesArr)
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_CUSTOM_LEVELS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetAllProgress() {
        prefs.edit().clear().apply()
        unlockedLevel = 1
    }
}
