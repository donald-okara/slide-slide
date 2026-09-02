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
            val f: Double get() = g + (HEURISTIC_WEIGHT * h)

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

            openSet.add(Node(initialTiles, initialBlankPos, 0, calculateHeuristic(initialTiles, size)))

            var iterations = 0
            while (openSet.isNotEmpty() && iterations < MAX_ITERATIONS) {
                iterations++
                val current = openSet.poll() ?: break
                if (current.h == 0) return reconstructPath(current, game.id)

                val tilesWrapper = IntArrayWrapper(current.tiles)
                if (closedSet.add(tilesWrapper)) {
                    expandNeighbors(current, size, blankValue, closedSet, openSet)
                }
            }

            return null
        }

        private fun expandNeighbors(
            current: Node,
            size: Int,
            blankValue: Int,
            closedSet: Set<IntArrayWrapper>,
            openSet: PriorityQueue<Node>,
        ) {
            val row = current.blankPos / size
            val col = current.blankPos % size

            val neighborOffsets = mutableListOf<Int>()
            if (row > 0) neighborOffsets.add(-size)
            if (row < size - 1) neighborOffsets.add(size)
            if (col > 0) neighborOffsets.add(-1)
            if (col < size - 1) neighborOffsets.add(1)

            neighborOffsets.forEach { offset ->
                val nextPos = current.blankPos + offset
                val nextTiles = current.tiles.copyOf()
                nextTiles[current.blankPos] = nextTiles[nextPos]
                nextTiles[nextPos] = blankValue

                if (IntArrayWrapper(nextTiles) !in closedSet) {
                    val nextH = calculateHeuristic(nextTiles, size)
                    openSet.add(Node(nextTiles, nextPos, current.g + 1, nextH, current, nextPos))
                }
            }
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
                manhattanDist += abs(i / size - value / size) + abs(i % size - value % size)
            }
            return manhattanDist + calculateLinearConflict(tiles, size)
        }

        private fun calculateLinearConflict(
            tiles: IntArray,
            size: Int,
        ): Int {
            val blankValue = size * size - 1
            var conflict = 0
            for (i in 0 until size) {
                conflict += countRowConflicts(tiles, i, size, blankValue)
                conflict += countColumnConflicts(tiles, i, size, blankValue)
            }
            return conflict
        }

        private fun countRowConflicts(
            tiles: IntArray,
            row: Int,
            size: Int,
            blankValue: Int,
        ): Int {
            var rowConflict = 0
            for (c1 in 0 until size) {
                val v1 = tiles[row * size + c1]
                if (v1 != blankValue && v1 / size == row) {
                    rowConflict += countRowConflictsForTile(tiles, row, size, blankValue, c1, v1)
                }
            }
            return rowConflict
        }

        @Suppress("LongParameterList")
        private fun countRowConflictsForTile(
            tiles: IntArray,
            row: Int,
            size: Int,
            blankValue: Int,
            col: Int,
            value: Int,
        ): Int {
            var count = 0
            for (c2 in col + 1 until size) {
                val v2 = tiles[row * size + c2]
                if (v2 != blankValue && v2 / size == row && value > v2) {
                    count += 2
                }
            }
            return count
        }

        private fun countColumnConflicts(
            tiles: IntArray,
            col: Int,
            size: Int,
            blankValue: Int,
        ): Int {
            var colConflict = 0
            for (r1 in 0 until size) {
                val v1 = tiles[r1 * size + col]
                if (v1 != blankValue && v1 % size == col) {
                    colConflict += countColumnConflictsForTile(tiles, col, size, blankValue, r1, v1)
                }
            }
            return colConflict
        }

        @Suppress("LongParameterList")
        private fun countColumnConflictsForTile(
            tiles: IntArray,
            col: Int,
            size: Int,
            blankValue: Int,
            row: Int,
            value: Int,
        ): Int {
            var count = 0
            for (r2 in row + 1 until size) {
                val v2 = tiles[r2 * size + col]
                if (v2 != blankValue && v2 % size == col && value > v2) {
                    count += 2
                }
            }
            return count
        }

        private fun reconstructPath(
            node: Node,
            gameId: Long,
        ): List<Move> {
            val path = mutableListOf<Move>()
            var current: Node? = node
            while (current != null) {
                val p = current.parent ?: break
                path.add(0, Move(gameId = gameId, fromPosition = current.movePos, toPosition = p.blankPos))
                current = p
            }
            return path
        }

        private class IntArrayWrapper(
            val array: IntArray,
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is IntArrayWrapper) return false
                return array.contentEquals(other.array)
            }

            override fun hashCode(): Int = array.contentHashCode()
        }
    }
