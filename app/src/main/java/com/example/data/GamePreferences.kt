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
        private const val KEY_PLAYER_ID = "player_id"
        private const val KEY_UNLOCKED_LEVEL = "unlocked_level"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_THEME = "current_theme"
        private const val KEY_CUSTOM_LEVELS = "custom_levels_json"
        private const val KEY_LEVELS_PLAYED_SINCE_AD = "levels_played_since_ad"
        private const val KEY_ADMIN_NOTICE = "admin_notice"
        private const val KEY_ADMIN_PIN = "admin_pin"
        private const val KEY_ADMIN_REWARDS = "admin_rewards_json"
        private const val KEY_CUSTOM_CODES = "custom_admin_codes_json"
        private const val KEY_REDEEMED_CODES = "redeemed_codes_json"
        private const val PREFIX_STAR = "stars_lvl_"
        private const val PREFIX_BEST_MOVES = "best_moves_lvl_"
    }

    var playerId: String
        get() {
            var id = prefs.getString(KEY_PLAYER_ID, null)
            if (id.isNullOrEmpty()) {
                val rand = (1000..9999).random()
                id = "AC-$rand"
                prefs.edit().putString(KEY_PLAYER_ID, id).apply()
            }
            return id
        }
        set(value) = prefs.edit().putString(KEY_PLAYER_ID, value).apply()

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

    var adminNotice: String
        get() = prefs.getString(
            KEY_ADMIN_NOTICE,
            "Clear levels & share your Player ID with the Admin to claim official rewards!"
        ) ?: "Clear levels & share your Player ID with the Admin to claim official rewards!"
        set(value) = prefs.edit().putString(KEY_ADMIN_NOTICE, value).apply()

    var adminPin: String
        get() = prefs.getString(KEY_ADMIN_PIN, GameConfig.DEFAULT_ADMIN_PIN) ?: GameConfig.DEFAULT_ADMIN_PIN
        set(value) = prefs.edit().putString(KEY_ADMIN_PIN, value).apply()

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

    // Admin Rewards System
    data class AdminReward(
        val id: String,
        val title: String,
        val note: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun getAdminRewards(): List<AdminReward> {
        val raw = prefs.getString(KEY_ADMIN_REWARDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<AdminReward>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AdminReward(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        note = obj.optString("note", "Granted by Admin"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun grantAdminReward(title: String, note: String = "Granted by Admin") {
        val current = getAdminRewards().toMutableList()
        val newReward = AdminReward(
            id = "RWD-" + System.currentTimeMillis() % 100000,
            title = title,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        current.add(0, newReward)
        try {
            val arr = JSONArray()
            for (r in current) {
                val obj = JSONObject()
                obj.put("id", r.id)
                obj.put("title", r.title)
                obj.put("note", r.note)
                obj.put("timestamp", r.timestamp)
                arr.put(obj)
            }
            prefs.edit().putString(KEY_ADMIN_REWARDS, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCustomAdminCodes(): Map<String, String> {
        val map = GameConfig.INITIAL_ADMIN_CODES.toMutableMap()
        val raw = prefs.getString(KEY_CUSTOM_CODES, null)
        if (!raw.isNullOrEmpty()) {
            try {
                val obj = JSONObject(raw)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k.uppercase()] = obj.getString(k)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return map
    }

    fun addCustomAdminCode(code: String, rewardTitle: String) {
        val current = getCustomAdminCodes().toMutableMap()
        current[code.uppercase().trim()] = rewardTitle.trim()
        try {
            val obj = JSONObject()
            for ((k, v) in current) {
                obj.put(k, v)
            }
            prefs.edit().putString(KEY_CUSTOM_CODES, obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isCodeRedeemed(code: String): Boolean {
        val raw = prefs.getString(KEY_REDEEMED_CODES, null) ?: return false
        return try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                if (arr.getString(i).equals(code.trim(), ignoreCase = true)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun redeemAdminCode(code: String): String? {
        val cleanCode = code.uppercase().trim()
        if (isCodeRedeemed(cleanCode)) return null

        val codes = getCustomAdminCodes()
        val rewardTitle = codes[cleanCode] ?: return null

        // Mark redeemed
        try {
            val raw = prefs.getString(KEY_REDEEMED_CODES, null)
            val arr = if (raw != null) JSONArray(raw) else JSONArray()
            arr.put(cleanCode)
            prefs.edit().putString(KEY_REDEEMED_CODES, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Grant reward
        grantAdminReward(rewardTitle, "Redeemed with Code: $cleanCode")
        return rewardTitle
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
