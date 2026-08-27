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
import java.util.PriorityQueue
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use case to solve a sliding puzzle using Weighted A* for a balance between speed and optimality.
 */
interface SolveUseCase {
    /**
     * Solves the given [game].
     *
     * @param game The current game state.
     * @return A list of moves to reach the solved state, or null if no solution found.
     */
    operator fun invoke(game: Game): List<Move>?
}

class SolveUseCaseImpl
    @Inject
    constructor() : SolveUseCase {
        companion object {
            private const val MAX_ITERATIONS = 200000
            private const val HEURISTIC_WEIGHT = 3.0
        }

        private class Node(
            val tiles: IntArray,
            val blankPos: Int,
            val g: Int,
            val h: Int,
            val parent: Node? = null,
            val movePos: Int = -1,
        ) : Comparable<Node> {
            val f: Double get() = g + HEURISTIC_WEIGHT * h

            override fun compareTo(other: Node): Int = f.compareTo(other.f)

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Node) return false
                return tiles.contentEquals(other.tiles)
            }

            override fun hashCode(): Int = tiles.contentHashCode()
        }

        override fun invoke(game: Game): List<Move>? {
            val size = game.difficulty.size
            val totalTiles = game.difficulty.totalTiles
            val initialTiles = IntArray(totalTiles)
            game.tiles.forEach { initialTiles[it.currentPosition] = it.value }

            val blankValue = totalTiles - 1
            val initialBlankPos = initialTiles.indexOf(blankValue)

            val openSet = PriorityQueue<Node>()
            val closedSet = mutableSetOf<IntArrayWrapper>()

            val initialH = calculateHeuristic(initialTiles, size)
            openSet.add(Node(initialTiles, initialBlankPos, 0, initialH))

            var iterations = 0
            while (openSet.isNotEmpty() && iterations < MAX_ITERATIONS) {
                iterations++
                val current = openSet.poll() ?: break

                if (current.h == 0) return reconstructPath(current, game.id)

                val tilesWrapper = IntArrayWrapper(current.tiles)
                if (tilesWrapper in closedSet) continue
                closedSet.add(tilesWrapper)

                val row = current.blankPos / size
                val col = current.blankPos % size

                val neighbors = mutableListOf<Int>()
                if (row > 0) neighbors.add(current.blankPos - size)
                if (row < size - 1) neighbors.add(current.blankPos + size)
                if (col > 0) neighbors.add(current.blankPos - 1)
                if (col < size - 1) neighbors.add(current.blankPos + 1)

                for (nextPos in neighbors) {
                    val nextTiles = current.tiles.copyOf()
                    nextTiles[current.blankPos] = nextTiles[nextPos]
                    nextTiles[nextPos] = blankValue

                    if (IntArrayWrapper(nextTiles) in closedSet) continue

                    val nextH = calculateHeuristic(nextTiles, size)
                    openSet.add(Node(nextTiles, nextPos, current.g + 1, nextH, current, nextPos))
                }
            }

            return null
        }

        private fun calculateHeuristic(
            tiles: IntArray,
            size: Int,
        ): Int {
            var manhattanDist = 0
            val blankValue = size * size - 1
            for (i in tiles.indices) {
                val value = tiles[i]
                if (value == blankValue) continue
                val targetRow = value / size
                val targetCol = value % size
                val currentRow = i / size
                val currentCol = i % size
                manhattanDist += abs(currentRow - targetRow) + abs(currentCol - targetCol)
            }
            return manhattanDist + calculateLinearConflict(tiles, size)
        }

        private fun calculateLinearConflict(
            tiles: IntArray,
            size: Int,
        ): Int {
            var conflict = 0
            val blankValue = size * size - 1
            for (r in 0 until size) {
                for (c1 in 0 until size) {
                    val v1 = tiles[r * size + c1]
                    if (v1 == blankValue || v1 / size != r) continue
                    for (c2 in c1 + 1 until size) {
                        val v2 = tiles[r * size + c2]
                        if (v2 == blankValue || v2 / size != r) continue
                        if (v1 > v2) conflict += 2
                    }
                }
            }
            for (c in 0 until size) {
                for (r1 in 0 until size) {
                    val v1 = tiles[r1 * size + c]
                    if (v1 == blankValue || v1 % size != c) continue
                    for (r2 in r1 + 1 until size) {
                        val v2 = tiles[r2 * size + c]
                        if (v2 == blankValue || v2 % size != c) continue
                        if (v1 > v2) conflict += 2
                    }
                }
            }
            return conflict
        }

        private fun reconstructPath(
            node: Node,
            gameId: Long,
        ): List<Move> {
            val path = mutableListOf<Move>()
            var current: Node? = node
            while (current != null) {
                val p = current.parent ?: break
                // p.blankPos is where the blank was
                // current.movePos is where the blank moved to (nextPos in the loop)
                // This means the tile at current.movePos moved to p.blankPos
                path.add(0, Move(gameId = gameId, fromPosition = current.movePos, toPosition = p.blankPos))
                current = p
            }
            return path
        }

        private class IntArrayWrapper(val array: IntArray) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is IntArrayWrapper) return false
                return array.contentEquals(other.array)
            }

            override fun hashCode(): Int = array.contentHashCode()
        }
    }
