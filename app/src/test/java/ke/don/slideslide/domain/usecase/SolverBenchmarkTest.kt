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
import org.junit.Test
import java.io.File

class SolverBenchmarkTest {
    private val solve: SolveUseCase = SolveUseCaseImpl()

    @Test
    fun `run solver benchmarks`() {
        val results = mutableListOf<String>()
        val iterationsPerDifficulty = 3

        Difficulty.entries.forEach { difficulty ->
            val movesList = mutableListOf<Int>()
            val timesList = mutableListOf<Long>()
            var successCount = 0

            val solvedTiles =
                (0 until difficulty.totalTiles).map { index ->
                    Tile(
                        id = index,
                        value = index,
                        currentPosition = index,
                        correctPosition = index,
                        isBlank = index == difficulty.totalTiles - 1,
                    )
                }

            val benchmarkShuffleDepth =
                when (difficulty) {
                    Difficulty.EASY -> 100
                    Difficulty.MEDIUM -> 20
                    Difficulty.HARD -> 10
                }

            repeat(iterationsPerDifficulty) {
                val testShuffledTiles = performLimitedShuffle(solvedTiles, difficulty, benchmarkShuffleDepth)
                val game = Game(id = 1, difficulty = difficulty, tiles = testShuffledTiles)

                val startTime = System.currentTimeMillis()
                val solution = solve(game)
                val duration = System.currentTimeMillis() - startTime

                if (solution != null) {
                    movesList.add(solution.size)
                    timesList.add(duration)
                    successCount++
                }
            }

            val avgMoves = if (movesList.isNotEmpty()) movesList.average() else -1.0
            val avgTime = if (timesList.isNotEmpty()) timesList.average() else -1.0
            val status =
                if (successCount == iterationsPerDifficulty) {
                    "SUCCESS"
                } else if (successCount > 0) {
                    "PARTIAL_SUCCESS ($successCount/$iterationsPerDifficulty)"
                } else {
                    "FAILED"
                }

            results.add(
                """
                {
                    "difficulty": "${difficulty.name}",
                    "status": "$status",
                    "avg_moves": ${String.format("%.2f", avgMoves)},
                    "avg_time_ms": ${String.format("%.2f", avgTime)}
                }
                """.trimIndent(),
            )

            println("Benchmark [${difficulty.name}]: $status, Avg Moves: $avgMoves, Avg Time: ${avgTime}ms")
        }

        val json = results.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]")
        val reportFile = File("../solver_benchmarks.json")
        reportFile.writeText(json)
        println("Benchmark report written to: ${reportFile.absolutePath}")
    }

    private fun performLimitedShuffle(
        tiles: List<Tile>,
        difficulty: Difficulty,
        depth: Int,
    ): List<Tile> {
        val currentTiles = tiles.map { it.copy() }.toMutableList()
        val size = difficulty.size
        var lastPos = -1

        repeat(depth) {
            val blank = currentTiles.find { it.isBlank }!!
            val blankPos = blank.currentPosition
            val row = blankPos / size
            val col = blankPos % size

            val neighbors = mutableListOf<Int>()
            if (row > 0) neighbors.add(blankPos - size)
            if (row < size - 1) neighbors.add(blankPos + size)
            if (col > 0) neighbors.add(blankPos - 1)
            if (col < size - 1) neighbors.add(blankPos + 1)

            val nextPos = neighbors.filter { it != lastPos }.random()

            val tileToMoveIndex = currentTiles.indexOfFirst { it.currentPosition == nextPos }
            val blankIndex = currentTiles.indexOf(blank)

            currentTiles[tileToMoveIndex] = currentTiles[tileToMoveIndex].copy(currentPosition = blankPos)
            currentTiles[blankIndex] = blank.copy(currentPosition = nextPos)
            lastPos = blankPos
        }
        return currentTiles
    }
}
