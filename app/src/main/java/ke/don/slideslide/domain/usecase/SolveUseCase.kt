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
import java.util.PriorityQueue
import kotlin.math.abs

/**
 * Use case to solve a sliding puzzle using the A* algorithm.
 */
interface SolveUseCase {
    /**
     * Solves the given [game] using A* search.
     *
     * @param game The current game state.
     * @return A list of moves to reach the solved state, or null if no solution found (or timeout).
     */
    operator fun invoke(game: Game): List<Move>?
}

class SolveUseCaseImpl : SolveUseCase {
    companion object {
        private const val MAX_ITERATIONS = 50000
    }

    private data class Node(
        val tiles: List<Tile>,
        val moves: List<Move>,
        val g: Int, // Cost from start
        val h: Int, // Heuristic (Manhattan distance)
    ) : Comparable<Node> {
        val f: Int get() = g + h

        override fun compareTo(other: Node): Int = f.compareTo(other.f)
    }

    override fun invoke(game: Game): List<Move>? {
        val gridSize = game.difficulty.size
        val openSet = PriorityQueue<Node>()
        val closedSet = mutableSetOf<List<Int>>()

        val initialTiles = game.tiles.sortedBy { it.currentPosition }
        openSet.add(Node(initialTiles, emptyList(), 0, calculateHeuristic(initialTiles, gridSize)))

        var iterations = 0
        while (openSet.isNotEmpty() && iterations < MAX_ITERATIONS) {
            iterations++
            val current = openSet.poll() ?: break
            val currentStateSignature = current.tiles.map { it.id }

            if (currentStateSignature !in closedSet) {
                closedSet.add(currentStateSignature)

                if (current.h == 0) return current.moves

                processNeighbors(current, game.id, gridSize, closedSet, openSet)
            }
        }

        return null
    }

    private fun processNeighbors(
        current: Node,
        gameId: Long,
        gridSize: Int,
        closedSet: Set<List<Int>>,
        openSet: PriorityQueue<Node>,
    ) {
        val blankTile = current.tiles.find { it.isBlank } ?: return
        val blankPos = blankTile.currentPosition
        val neighbors = getAdjacentPositions(blankPos, gridSize)

        for (nextPos in neighbors) {
            val nextTiles = swapTiles(current.tiles, blankTile, nextPos)
            val signature = nextTiles.map { it.id }

            if (signature !in closedSet) {
                val move = Move(gameId = gameId, fromPosition = nextPos, toPosition = blankPos)
                openSet.add(
                    Node(
                        nextTiles,
                        current.moves + move,
                        current.g + 1,
                        calculateHeuristic(nextTiles, gridSize),
                    ),
                )
            }
        }
    }

    private fun swapTiles(
        tiles: List<Tile>,
        blankTile: Tile,
        nextPos: Int,
    ): List<Tile> {
        val nextTiles = tiles.toMutableList()
        val movingTileIndex = nextTiles.indexOfFirst { it.currentPosition == nextPos }
        val blankTileIndex = nextTiles.indexOf(blankTile)

        if (movingTileIndex == -1) return tiles

        val movingTile = nextTiles[movingTileIndex]
        nextTiles[movingTileIndex] = movingTile.copy(currentPosition = blankTile.currentPosition)
        nextTiles[blankTileIndex] = blankTile.copy(currentPosition = nextPos)

        return nextTiles.sortedBy { it.currentPosition }
    }

    private fun calculateHeuristic(
        tiles: List<Tile>,
        size: Int,
    ): Int {
        var manhattanDist = 0
        for (tile in tiles) {
            if (tile.isBlank) continue
            val currentPos = tile.currentPosition
            val targetPos = tile.correctPosition

            manhattanDist += abs(currentPos / size - targetPos / size) +
                abs(currentPos % size - targetPos % size)
        }

        return manhattanDist + calculateLinearConflict(tiles, size)
    }

    private fun calculateLinearConflict(
        tiles: List<Tile>,
        size: Int,
    ): Int {
        var conflict = 0

        for (i in 0 until size) {
            conflict += calculateRowConflict(tiles, i, size)
            conflict += calculateColConflict(tiles, i, size)
        }

        return conflict
    }

    private fun calculateRowConflict(
        tiles: List<Tile>,
        row: Int,
        size: Int,
    ): Int {
        var conflict = 0
        val rowTiles = tiles.filter { it.currentPosition / size == row && !it.isBlank }

        for (i in 0 until rowTiles.size) {
            for (j in i + 1 until rowTiles.size) {
                val tile1 = rowTiles[i]
                val tile2 = rowTiles[j]

                if (tile1.correctPosition / size == row &&
                    tile2.correctPosition / size == row &&
                    tile1.correctPosition > tile2.correctPosition
                ) {
                    conflict += 2
                }
            }
        }
        return conflict
    }

    private fun calculateColConflict(
        tiles: List<Tile>,
        col: Int,
        size: Int,
    ): Int {
        var conflict = 0
        val colTiles = tiles.filter { it.currentPosition % size == col && !it.isBlank }

        for (i in 0 until colTiles.size) {
            for (j in i + 1 until colTiles.size) {
                val tile1 = colTiles[i]
                val tile2 = colTiles[j]

                if (tile1.correctPosition % size == col &&
                    tile2.correctPosition % size == col &&
                    tile1.correctPosition > tile2.correctPosition
                ) {
                    conflict += 2
                }
            }
        }
        return conflict
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
