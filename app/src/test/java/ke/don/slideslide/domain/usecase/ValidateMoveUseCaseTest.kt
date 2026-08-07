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
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateMoveUseCaseTest {
    private val validateMove: ValidateMoveUseCase = ValidateMoveUseCaseImpl()

    /**
     * Helper to create a 3x3 game with the blank tile at a specific position.
     * Positions for 3x3:
     * 0 1 2
     * 3 4 5
     * 6 7 8
     */
    private fun create3x3Game(blankPosition: Int): Game {
        val tiles =
            (0..8).map { pos ->
                Tile(
                    id = pos,
                    value = pos,
                    currentPosition = pos,
                    correctPosition = pos,
                    isBlank = pos == blankPosition,
                )
            }
        return Game(id = 1L, difficulty = Difficulty.EASY, tiles = tiles)
    }

    private fun createMove(
        from: Int,
        to: Int,
    ) = Move(gameId = 1L, fromPosition = from, toPosition = to)

    @Test
    fun `test move from center`() {
        val game = create3x3Game(blankPosition = 4) // Center

        // Adjacent tiles moving into blank space should be valid
        assertNotNull("Up move should be valid", validateMove(game, createMove(1, 4)))
        assertNotNull("Down move should be valid", validateMove(game, createMove(7, 4)))
        assertNotNull("Left move should be valid", validateMove(game, createMove(3, 4)))
        assertNotNull("Right move should be valid", validateMove(game, createMove(5, 4)))

        // Distant tiles should be invalid
        assertNull("Corner move should be invalid", validateMove(game, createMove(0, 4)))
        assertNull("Corner move should be invalid", validateMove(game, createMove(2, 4)))
        assertNull("Corner move should be invalid", validateMove(game, createMove(6, 4)))
        assertNull("Corner move should be invalid", validateMove(game, createMove(8, 4)))
    }

    @Test
    fun `test move from top-left corner`() {
        val game = create3x3Game(blankPosition = 0)

        assertNotNull("Move from right should be valid", validateMove(game, createMove(1, 0)))
        assertNotNull("Move from below should be valid", validateMove(game, createMove(3, 0)))

        assertNull("Diagonal move should be invalid", validateMove(game, createMove(4, 0)))
        assertNull("Far move should be invalid", validateMove(game, createMove(8, 0)))
    }

    @Test
    fun `test move blank tile is always invalid`() {
        val game = create3x3Game(blankPosition = 4)

        assertNull("Moving the blank tile from its position should be invalid", validateMove(game, createMove(4, 4)))
        assertNull("Moving something to a non-blank position should be invalid", validateMove(game, createMove(1, 2)))
    }

    @Test
    fun `test 4x4 grid boundary`() {
        // 0  1  2  3
        // 4  5  6  7
        // 8  9 10 11
        // 12 13 14 15
        val tiles =
            (0..15).map { pos ->
                Tile(id = pos, value = pos, currentPosition = pos, correctPosition = pos, isBlank = pos == 3)
            }
        val game = Game(id = 1L, difficulty = Difficulty.MEDIUM, tiles = tiles)

        assertNotNull("Move from left (pos 2) should be valid", validateMove(game, createMove(2, 3)))
        assertNotNull("Move from below (pos 7) should be valid", validateMove(game, createMove(7, 3)))

        assertNull(
            "Move from pos 4 should be invalid (not adjacent despite being row-end to row-start)",
            validateMove(game, createMove(4, 3)),
        )
    }
}
