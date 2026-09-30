package com.example

import com.example.data.LevelsRepository
import com.example.model.ArrowDirection
import com.example.model.CubeCell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelsRepositoryLoads1000Levels() {
        val allLevels = LevelsRepository.getAllLevels()
        assertEquals(com.example.config.GameConfig.MAX_LEVELS, allLevels.size)

        // Sample check across levels 1 to 1000
        val sampleLevels = listOf(1, 2, 5, 25, 50, 100, 250, 350, 500, 750, 900, 1000)
        for (lvl in sampleLevels) {
            val level = LevelsRepository.getLevel(lvl)
            assertEquals(lvl, level.levelNumber)
            assertTrue("Level $lvl should have at least 3 cubes", level.cubes.size >= 3)
            assertTrue("Level $lvl must be solvable", LevelsRepository.isSolvable(level.rows, level.cols, level.cubes))
        }
    }

    @Test
    fun testRandomizedTierDistributionAcross1000Levels() {
        val allLevels = LevelsRepository.getAllLevels()
        
        // Confirm all 4 tiers are present throughout the 1000 levels
        val noviceCount = allLevels.count { it.tier == com.example.model.LevelTier.EASY }
        val normalCount = allLevels.count { it.tier == com.example.model.LevelTier.NORMAL }
        val hardCount = allLevels.count { it.tier == com.example.model.LevelTier.HARD }
        val expertCount = allLevels.count { it.tier == com.example.model.LevelTier.EXPERT }

        assertTrue("Novice levels must exist", noviceCount > 100)
        assertTrue("Normal levels must exist", normalCount > 100)
        assertTrue("Hard levels must exist", hardCount > 100)
        assertTrue("Expert levels must exist", expertCount > 100)

        // Confirm difficulty is randomized within early levels (not strictly only Novice in 1..250)
        val first50 = allLevels.take(50)
        val first50Tiers = first50.map { it.tier }.toSet()
        assertTrue("First 50 levels should have varied tiers", first50Tiers.size >= 3)

        // Confirm later levels also have varied tiers (not strictly only Expert in 751..1000)
        val last50 = allLevels.takeLast(50)
        val last50Tiers = last50.map { it.tier }.toSet()
        assertTrue("Last 50 levels should have varied tiers", last50Tiers.size >= 3)
    }

    @Test
    fun testPathClearLogic() {
        // Cube at (0, 0) pointing DOWN blocked by cube at (1, 0)
        val cube1 = CubeCell(1, 0, 0, ArrowDirection.DOWN)
        val cube2 = CubeCell(2, 1, 0, ArrowDirection.DOWN)
        val cubes = listOf(cube1, cube2)

        assertFalse(LevelsRepository.isPathClear(cube1, 3, 3, cubes))
        assertTrue(LevelsRepository.isPathClear(cube2, 3, 3, cubes))

        // Movable cubes should only be cube2
        val movable = LevelsRepository.findMovableCubes(3, 3, cubes)
        assertEquals(1, movable.size)
        assertEquals(2, movable.first().id)
    }

    @Test
    fun testHintReturnsValidMovableCube() {
        val level1 = LevelsRepository.getLevel(1)
        val hint = LevelsRepository.getHint(level1.rows, level1.cols, level1.cubes)
        assertNotNull(hint)
        assertTrue(LevelsRepository.isPathClear(hint!!, level1.rows, level1.cols, level1.cubes))
    }
}
