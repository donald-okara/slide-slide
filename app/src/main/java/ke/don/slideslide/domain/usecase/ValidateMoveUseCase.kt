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
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use case to validate if a move is legal in a sliding puzzle.
 * A move is legal if:
 * 1. The target position is currently occupied by the blank tile.
 * 2. The source position is occupied by a non-blank tile.
 * 3. The source and target positions are horizontally or vertically adjacent.
 */
interface ValidateMoveUseCase {
    /**
     * Validates if the given [move] is legal within the current [game] state.
     *
     * @param game The current state of the game.
     * @param move The move to validate.
     * @return A pair of (movingTile, blankTile) if the move is legal, null otherwise.
     */
    operator fun invoke(
        game: Game,
        move: Move,
    ): Pair<Tile, Tile>?
}

class ValidateMoveUseCaseImpl
    @Inject
    constructor() : ValidateMoveUseCase {
        override fun invoke(
            game: Game,
            move: Move,
        ): Pair<Tile, Tile>? {
            val fromTile = game.tiles.find { it.currentPosition == move.fromPosition }
            val toTile = game.tiles.find { it.currentPosition == move.toPosition }

            return when {
                fromTile == null || toTile == null -> null
                fromTile.isBlank || !toTile.isBlank -> null
                else -> {
                    val gridSize = game.difficulty.size

                    val fromRow = move.fromPosition / gridSize
                    val fromCol = move.fromPosition % gridSize

                    val toRow = move.toPosition / gridSize
                    val toCol = move.toPosition % gridSize

                    val rowDiff = abs(fromRow - toRow)
                    val colDiff = abs(fromCol - toCol)

                    val isAdjacent = (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)

                    if (isAdjacent) fromTile to toTile else null
                }
            }
        }
    }
