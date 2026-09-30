package com.example.model

enum class ArrowDirection(val deltaRow: Int, val deltaCol: Int, val angleDegrees: Float) {
    UP(-1, 0, 0f),
    RIGHT(0, 1, 90f),
    DOWN(1, 0, 180f),
    LEFT(0, -1, 270f);

    fun opposite(): ArrowDirection = when (this) {
        UP -> DOWN
        RIGHT -> LEFT
        DOWN -> UP
        LEFT -> RIGHT
    }
}

enum class LevelTier(
    val title: String,
    val badgeColorHex: Long,
    val minGridSize: Int,
    val maxGridSize: Int
) {
    EASY("Novice", 0xFF10B981, 3, 3),
    NORMAL("Normal", 0xFF0284C7, 4, 4),
    HARD("Hard", 0xFFF59E0B, 5, 5),
    EXPERT("Expert", 0xFFEF4444, 6, 6);

    companion object {
        /**
         * Returns a randomized, deterministic tier for any level between 1 and 1000.
         * Ensures introductory levels (1-2) start with Novice, while levels 3 to 1000
         * are varied with a dynamic mix of Novice, Normal, Hard, and Expert.
         */
        fun fromLevel(level: Int): LevelTier {
            if (level <= 2) return EASY
            val hash = ((level * 1103515245L + 12345L) xor (level.toLong() * 2654435761L)).toInt() and 0x7FFFFFFF
            val r = hash % 100
            return when {
                r < 28 -> EASY    // ~28% Novice
                r < 60 -> NORMAL  // ~32% Normal
                r < 84 -> HARD    // ~24% Hard
                else -> EXPERT    // ~16% Expert
            }
        }
    }
}

data class CubeCell(
    val id: Int,
    val row: Int,
    val col: Int,
    val direction: ArrowDirection
)

data class LevelData(
    val id: Int,
    val levelNumber: Int,
    val tier: LevelTier,
    val rows: Int,
    val cols: Int,
    val cubes: List<CubeCell>,
    val parMoves: Int
)

data class CustomLevel(
    val id: String,
    val name: String,
    val rows: Int,
    val cols: Int,
    val cubes: List<CubeCell>,
    val createdAt: Long = System.currentTimeMillis()
)
