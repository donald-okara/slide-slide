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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateMoveUseCaseTest {

    private val validateMove = ValidateMoveUseCase()

    /**
     * Helper to create a 3x3 game with the blank tile at a specific position.
     * Positions for 3x3:
     * 0 1 2
     * 3 4 5
     * 6 7 8
     */
    private fun create3x3Game(blankPosition: Int): Game {
        val tiles = (0..8).map { pos ->
            Tile(
                id = pos,
                value = pos,
                currentPosition = pos,
                isBlank = pos == blankPosition
            )
        }
        return Game(difficulty = Difficulty.EASY, tiles = tiles)
    }

    @Test
    fun `test move from center`() {
        val game = create3x3Game(blankPosition = 4) // Center

        // Adjacent tiles should be valid
        assertTrue("Up move should be valid", validateMove(game, game.tiles[1]))
        assertTrue("Down move should be valid", validateMove(game, game.tiles[7]))
        assertTrue("Left move should be valid", validateMove(game, game.tiles[3]))
        assertTrue("Right move should be valid", validateMove(game, game.tiles[5]))

        // Distant tiles should be invalid
        assertFalse("Corner move should be invalid", validateMove(game, game.tiles[0]))
        assertFalse("Corner move should be invalid", validateMove(game, game.tiles[2]))
        assertFalse("Corner move should be invalid", validateMove(game, game.tiles[6]))
        assertFalse("Corner move should be invalid", validateMove(game, game.tiles[8]))
    }

    @Test
    fun `test move from top-left corner`() {
        val game = create3x3Game(blankPosition = 0)

        assertTrue("Move from right should be valid", validateMove(game, game.tiles[1]))
        assertTrue("Move from below should be valid", validateMove(game, game.tiles[3]))

        assertFalse("Diagonal move should be invalid", validateMove(game, game.tiles[4]))
        assertFalse("Far move should be invalid", validateMove(game, game.tiles[8]))
    }

    @Test
    fun `test move blank tile is always invalid`() {
        val game = create3x3Game(blankPosition = 4)
        val blankTile = game.tiles[4]

        assertFalse("Moving the blank tile itself should be invalid", validateMove(game, blankTile))
    }

    @Test
    fun `test 4x4 grid boundary`() {
        // 0  1  2  3
        // 4  5  6  7
        // 8  9 10 11
        // 12 13 14 15
        val tiles = (0..15).map { pos ->
            Tile(id = pos, value = pos, currentPosition = pos, isBlank = pos == 3)
        }
        val game = Game(difficulty = Difficulty.MEDIUM, tiles = tiles)

        assertTrue("Move from left (pos 2) should be valid", validateMove(game, game.tiles[2]))
        assertTrue("Move from below (pos 7) should be valid", validateMove(game, game.tiles[7]))

        assertFalse("Move from pos 4 should be invalid (next row start)", validateMove(game, game.tiles[4]))
    }
}
