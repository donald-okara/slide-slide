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

import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Tile
import kotlin.math.abs

/**
 * Use case to validate if a tile can be moved in a sliding puzzle.
 * A tile can only move if it is horizontally or vertically adjacent to the blank space.
 */
class ValidateMoveUseCase {
    /**
     * Validates if the given [tile] can be moved within the current [game] state.
     *
     * @param game The current state of the game.
     * @param tile The tile the user intends to move.
     * @return true if the move is legal, false otherwise.
     */
    operator fun invoke(game: Game, tile: Tile): Boolean {
        // Cannot move the blank tile itself
        if (tile.isBlank) return false

        val blankTile = game.tiles.find { it.isBlank } ?: return false

        val gridSize = game.difficulty.size

        val tileRow = tile.currentPosition / gridSize
        val tileCol = tile.currentPosition % gridSize

        val blankRow = blankTile.currentPosition / gridSize
        val blankCol = blankTile.currentPosition % gridSize

        val rowDiff = abs(tileRow - blankRow)
        val colDiff = abs(tileCol - blankCol)

        // Legal move if the tile is adjacent to the blank tile (Manhattan distance == 1)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }
}
