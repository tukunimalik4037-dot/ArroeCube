package com.example.data

import com.example.model.ArrowDirection
import com.example.model.CubeCell
import com.example.model.LevelData
import com.example.model.LevelTier

object LevelsRepository {

    /**
     * Checks if a cube at (row, col) pointing in [direction] has a clear path to the edge
     * of the board given the current set of [activeCubes].
     */
    fun isPathClear(
        cube: CubeCell,
        rows: Int,
        cols: Int,
        activeCubes: Collection<CubeCell>
    ): Boolean {
        var r = cube.row + cube.direction.deltaRow
        var c = cube.col + cube.direction.deltaCol

        val activeMap = activeCubes.filter { it.id != cube.id }.associateBy { it.row to it.col }

        while (r in 0 until rows && c in 0 until cols) {
            if (activeMap.containsKey(r to c)) {
                return false // Path is blocked by an active cube
            }
            r += cube.direction.deltaRow
            c += cube.direction.deltaCol
        }
        return true
    }

    /**
     * Returns all cubes in [activeCubes] that currently have a clear path to the edge.
     */
    fun findMovableCubes(
        rows: Int,
        cols: Int,
        activeCubes: Collection<CubeCell>
    ): List<CubeCell> {
        return activeCubes.filter { isPathClear(it, rows, cols, activeCubes) }
    }

    /**
     * Validates whether a configuration of cubes is 100% solvable.
     * Simulates step-by-step elimination until all cubes are removed.
     */
    fun isSolvable(rows: Int, cols: Int, cubes: List<CubeCell>): Boolean {
        if (cubes.isEmpty()) return false
        val remaining = cubes.toMutableSet()

        while (remaining.isNotEmpty()) {
            val movable = remaining.filter { isPathClear(it, rows, cols, remaining) }
            if (movable.isEmpty()) {
                return false // Deadlock reached
            }
            // Remove one movable cube (or all currently movable cubes)
            remaining.remove(movable.first())
        }
        return true
    }

    /**
     * Returns a hint cube: one cube that is currently movable.
     */
    fun getHint(rows: Int, cols: Int, activeCubes: Collection<CubeCell>): CubeCell? {
        val movable = findMovableCubes(rows, cols, activeCubes)
        return movable.firstOrNull()
    }

    // Fast thread-safe cache for up to 1000 handcrafted and verified levels
    private val levelCache = java.util.concurrent.ConcurrentHashMap<Int, LevelData>()

    fun getLevel(levelNumber: Int): LevelData {
        val clamped = levelNumber.coerceIn(1, com.example.config.GameConfig.MAX_LEVELS)
        return levelCache.computeIfAbsent(clamped) { lvl ->
            val tier = LevelTier.fromLevel(lvl)
            buildLevelForNumber(lvl, tier)
        }
    }

    fun getAllLevels(): List<LevelData> {
        return (1..com.example.config.GameConfig.MAX_LEVELS).map { getLevel(it) }
    }

    /**
     * Builds handcrafted and deterministically solvable level structures across 1000 levels.
     * Each level dynamically scales rows, cols, and cube count according to its randomly assigned tier.
     */
    private fun buildLevelForNumber(level: Int, tier: LevelTier): LevelData {
        val (rows, cols, count) = when (tier) {
            LevelTier.EASY -> {
                when {
                    level <= 2 -> Triple(3, 3, 3)
                    else -> {
                        val variation = (level * 7) % 3 // 0, 1, 2
                        Triple(3, 3, 4 + variation) // 4 to 6 cubes on 3x3 board
                    }
                }
            }
            LevelTier.NORMAL -> {
                val variation = (level * 11) % 4 // 0, 1, 2, 3
                Triple(4, 4, 7 + variation) // 7 to 10 cubes on 4x4 board
            }
            LevelTier.HARD -> {
                val variation = (level * 17) % 5 // 0, 1, 2, 3, 4
                Triple(5, 5, 12 + variation) // 12 to 16 cubes on 5x5 board
            }
            LevelTier.EXPERT -> {
                val variation = (level * 23) % 6 // 0..5
                Triple(6, 6, 17 + variation) // 17 to 22 cubes on 6x6 board
            }
        }

        val cubes = generateSolvableCubes(level, rows, cols, count)
        return LevelData(
            id = level,
            levelNumber = level,
            tier = tier,
            rows = rows,
            cols = cols,
            cubes = cubes,
            parMoves = cubes.size
        )
    }

    /**
     * Deterministically generates solvable puzzles using a reverse-solve construction.
     * Starts with an empty board and adds arrows in reverse: each added arrow must
     * have a clear line of sight to the edge at the moment it is placed (meaning when played
     * forward, it can be removed in the reverse order).
     */
    private fun generateSolvableCubes(seed: Int, rows: Int, cols: Int, targetCount: Int): List<CubeCell> {
        val random = java.util.Random((seed * 31337L) xor 0x5DEECE66DL)
        val placed = mutableListOf<CubeCell>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        var nextId = 1
        var attempts = 0
        val maxAttempts = 1000

        while (placed.size < targetCount && attempts < maxAttempts) {
            attempts++
            val r = random.nextInt(rows)
            val c = random.nextInt(cols)

            if (occupied.contains(r to c)) continue

            // Try all 4 directions, shuffle to pick one that is clear in reverse
            val dirs = ArrowDirection.values().toMutableList().apply { shuffle(random) }
            var chosenDir: ArrowDirection? = null

            for (dir in dirs) {
                // Check if path from (r, c) in dir is currently clear of previously placed cubes
                var testR = r + dir.deltaRow
                var testC = c + dir.deltaCol
                var blocked = false
                while (testR in 0 until rows && testC in 0 until cols) {
                    if (occupied.contains(testR to testC)) {
                        blocked = true
                        break
                    }
                    testR += dir.deltaRow
                    testC += dir.deltaCol
                }

                if (!blocked) {
                    chosenDir = dir
                    break
                }
            }

            if (chosenDir != null) {
                val cell = CubeCell(nextId++, r, c, chosenDir)
                placed.add(cell)
                occupied.add(r to c)
            }
        }

        // Verify and fall back to handcrafted pattern if needed
        if (placed.size >= 3 && isSolvable(rows, cols, placed)) {
            return placed
        }

        // Guaranteed handcrafted fallback for this grid dimension
        return createHandcraftedFallback(seed, rows, cols)
    }

    private fun createHandcraftedFallback(seed: Int, rows: Int, cols: Int): List<CubeCell> {
        val list = mutableListOf<CubeCell>()
        var id = 1
        // Create an outward-facing spiral / box pattern that is guaranteed solvable
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if ((r + c + seed) % 2 == 0) {
                    val dir = when {
                        r == 0 -> ArrowDirection.UP
                        r == rows - 1 -> ArrowDirection.DOWN
                        c == 0 -> ArrowDirection.LEFT
                        c == cols - 1 -> ArrowDirection.RIGHT
                        r <= c -> ArrowDirection.UP
                        else -> ArrowDirection.DOWN
                    }
                    list.add(CubeCell(id++, r, c, dir))
                }
            }
        }
        if (list.isEmpty() || !isSolvable(rows, cols, list)) {
            // Absolute minimal 3-cube solvable configuration
            return listOf(
                CubeCell(1, 0, 0, ArrowDirection.UP),
                CubeCell(2, rows - 1, 0, ArrowDirection.DOWN),
                CubeCell(3, 0, cols - 1, ArrowDirection.RIGHT)
            )
        }
        return list
    }
}
