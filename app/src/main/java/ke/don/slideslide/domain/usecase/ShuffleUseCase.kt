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
import ke.don.slideslide.domain.model.Tile

/**
 * Use case to perform a solvable shuffle of the puzzle tiles.
 * It starts from a solved state and performs random valid moves.
 */
interface ShuffleUseCase {
    /**
     * Shuffles the given [tiles] based on the [difficulty].
     *
     * @param tiles The initial solved list of tiles.
     * @param difficulty The game difficulty (determines shuffle depth).
     * @return A new list of tiles in a shuffled but solvable state.
     */
    operator fun invoke(
        tiles: List<Tile>,
        difficulty: Difficulty,
    ): List<Tile>
}

class ShuffleUseCaseImpl : ShuffleUseCase {
    companion object {
        private const val SHUFFLE_DEPTH_EASY = 100
        private const val SHUFFLE_DEPTH_MEDIUM = 200
        private const val SHUFFLE_DEPTH_HARD = 300
    }

    override fun invoke(
        tiles: List<Tile>,
        difficulty: Difficulty,
    ): List<Tile> {
        if (tiles.isEmpty()) return tiles

        val currentTiles = tiles.map { it.copy() }.toMutableList()
        val gridSize = difficulty.size
        // Fixed depth for shuffling to ensure complexity
        val shuffleDepth =
            when (difficulty) {
                Difficulty.EASY -> SHUFFLE_DEPTH_EASY
                Difficulty.MEDIUM -> SHUFFLE_DEPTH_MEDIUM
                Difficulty.HARD -> SHUFFLE_DEPTH_HARD
            }

        var lastPos = -1
        repeat(shuffleDepth) {
            val blankTile = currentTiles.find { it.isBlank } ?: return@repeat
            val blankPos = blankTile.currentPosition
            val neighbors = getAdjacentPositions(blankPos, gridSize)

            // Avoid moving the same tile back immediately
            val possibleMoves = neighbors.filter { it != lastPos }
            val nextPos =
                if (possibleMoves.isNotEmpty()) {
                    possibleMoves.random()
                } else {
                    neighbors.random()
                }

            val tileToMoveIndex = currentTiles.indexOfFirst { it.currentPosition == nextPos }
            if (tileToMoveIndex != -1) {
                val tileToMove = currentTiles[tileToMoveIndex]
                val blankIndex = currentTiles.indexOf(blankTile)

                currentTiles[tileToMoveIndex] = tileToMove.copy(currentPosition = blankPos)
                currentTiles[blankIndex] = blankTile.copy(currentPosition = nextPos)

                lastPos = blankPos
            }
        }

        return currentTiles
    }

    private fun getAdjacentPositions(
        pos: Int,
        size: Int,
    ): List<Int> {
        val row = pos / size
        val col = pos % size
        val adjacent = mutableListOf<Int>()

        if (row > 0) adjacent.add(pos - size)
        if (row < size - 1) adjacent.add(pos + size)
        if (col > 0) adjacent.add(pos - 1)
        if (col < size - 1) adjacent.add(pos + 1)

        return adjacent
    }
}
