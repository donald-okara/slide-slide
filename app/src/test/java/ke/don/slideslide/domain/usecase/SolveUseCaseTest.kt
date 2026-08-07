/*
 * Copyright (C) 2026 Donald Isoe.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ke.don.slideslide.domain.usecase

import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SolveUseCaseTest {
    private val solve: SolveUseCase = SolveUseCaseImpl()

    private fun createGame(
        difficulty: Difficulty,
        positions: List<Int>,
    ): Game {
        val tiles =
            positions.mapIndexed { index, value ->
                Tile(
                    id = value,
                    value = value,
                    currentPosition = index,
                    correctPosition = value,
                    isBlank = value == difficulty.totalTiles - 1,
                )
            }
        return Game(difficulty = difficulty, tiles = tiles)
    }

    @Test
    fun `test solver find solution for simple move`() {
        // Solved: 0 1 2, 3 4 5, 6 7 [8]
        // One move away: 0 1 2, 3 4 5, 6 [8] 7 (Swap 7 and 8)
        val game = createGame(Difficulty.EASY, listOf(0, 1, 2, 3, 4, 5, 6, 8, 7))

        val solution = solve(game)

        assertNotNull(solution)
        val moveCount = solution?.size ?: 0
        println("Solver took $moveCount moves to solve a simple board.")

        assertEquals(1, moveCount)
        assertEquals(8, solution?.get(0)?.fromPosition) // Tile 7 was at 8
        assertEquals(7, solution?.get(0)?.toPosition) // To 7 (where blank was)
    }

    @Test
    fun `test solver find solution for medium complexity`() {
        // One move from blank at 3
        val game = createGame(Difficulty.EASY, listOf(0, 1, 2, 8, 3, 5, 6, 4, 7))

        val startTime = System.currentTimeMillis()
        val solution = solve(game)
        val duration = System.currentTimeMillis() - startTime

        assertNotNull(solution)
        val moveCount = solution?.size ?: 0
        println("Solver took $moveCount moves and ${duration}ms to solve medium complexity board.")

        assertTrue("Solution should have more than 0 moves", moveCount > 0)
    }

    @Test
    fun `test solver handles hard 5x5 board`() {
        // Create a 5x5 board that is 2 moves from solved
        // Solved blank is at 24.
        // Let's swap 23 and 24, then 18 and 23.
        val hardPositions = (0..24).toMutableList()
        // Target: ... 18 19, 20 21 22 23 [24]
        // 1. Swap 23 and 24: ... 18 19, 20 21 22 [24] 23
        hardPositions[23] = 24
        hardPositions[24] = 23

        // 2. Swap 18 and 23 (pos 18 and 23): ... [24] 19, 20 21 22 18 23
        // No, let's keep it simple: just move blank up then left.
        // Solved 5x5:
        // 0  1  2  3  4
        // 5  6  7  8  9
        // 10 11 12 13 14
        // 15 16 17 18 19
        // 20 21 22 23 24(B)

        // Move 24 up (swap with 19 at pos 19):
        // ... 15 16 17 18 24(B)
        // ... 20 21 22 23 19
        val game =
            createGame(
                Difficulty.HARD,
                (0..18).toList() + listOf(24, 20, 21, 22, 23, 19),
            )

        val startTime = System.currentTimeMillis()
        val solution = solve(game)
        val duration = System.currentTimeMillis() - startTime

        assertNotNull("Solver should find solution for hard board", solution)
        println("Hard Solver took ${solution?.size} moves and ${duration}ms")
        assertTrue(solution!!.isNotEmpty())
    }
}
