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
import java.util.Locale

class SolverBenchmarkTest {
    private val solve: SolveUseCase = SolveUseCaseImpl()

    @Test
    fun `run solver benchmarks`() {
        val jsonResults = mutableListOf<String>()
        val reportData = mutableListOf<BenchmarkResult>()
        val iterationsPerDifficulty = 3

        Difficulty.entries.forEach { difficulty ->
            val result = runBenchmarkForDifficulty(difficulty, iterationsPerDifficulty)
            jsonResults.add(result.json)
            reportData.add(result)
        }

        // Write JSON report
        val json = jsonResults.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]")
        val jsonReportFile = File("../solver_benchmarks.json")
        jsonReportFile.writeText(json)
        println("JSON Benchmark report written to: ${jsonReportFile.absolutePath}")

        // Write Markdown report
        val markdownReport = generateMarkdownReport(reportData)
        val mdReportFile = File("../solver_benchmarks.md")
        mdReportFile.writeText(markdownReport)
        println("Markdown Benchmark report written to: ${mdReportFile.absolutePath}")
    }

    private data class BenchmarkResult(
        val difficulty: String,
        val status: String,
        val successCount: Int,
        val totalIterations: Int,
        val avgMoves: Double,
        val avgTimeMs: Double,
        val json: String,
    )

    private fun generateMarkdownReport(results: List<BenchmarkResult>): String {
        val sb = StringBuilder()
        sb.append("## 🧩 Autosolver Benchmark Evaluation\n\n")
        sb.append("| Difficulty | Status | Avg Moves | Avg Time (ms) |\n")
        sb.append("| :--- | :--- | :--- | :--- |\n")

        results.forEach { result ->
            sb.append("| ${result.difficulty} | ${result.status} | ${String.format(Locale.US, "%.2f", result.avgMoves)} | ${String.format(Locale.US, "%.2f", result.avgTimeMs)} |\n")
        }

        sb.append("\n*Generated automatically by SolverBenchmarkTest*")
        return sb.toString()
    }

    private fun runBenchmarkForDifficulty(
        difficulty: Difficulty,
        iterations: Int,
    ): BenchmarkResult {
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
                Difficulty.EASY -> SHUFFLE_DEPTH_EASY
                Difficulty.MEDIUM -> SHUFFLE_DEPTH_MEDIUM
                Difficulty.HARD -> SHUFFLE_DEPTH_HARD
            }

        repeat(iterations) {
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
        val status = getStatus(successCount, iterations)

        println("Benchmark [${difficulty.name}]: $status, Avg Moves: $avgMoves, Avg Time: ${avgTime}ms")

        val json = """
            {
                "difficulty": "${difficulty.name}",
                "status": "$status",
                "success_count": $successCount,
                "total_iterations": $iterations,
                "avg_moves": ${String.format(Locale.US, "%.2f", avgMoves)},
                "avg_time_ms": ${String.format(Locale.US, "%.2f", avgTime)}
            }
            """.trimIndent()

        return BenchmarkResult(
            difficulty = difficulty.name,
            status = status,
            successCount = successCount,
            totalIterations = iterations,
            avgMoves = avgMoves,
            avgTimeMs = avgTime,
            json = json
        )
    }

    private fun getStatus(
        successCount: Int,
        total: Int,
    ): String =
        when {
            successCount == total -> "SUCCESS"
            successCount > 0 -> "PARTIAL_SUCCESS ($successCount/$total)"
            else -> "FAILED"
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

    companion object {
        private const val SHUFFLE_DEPTH_EASY = 100
        private const val SHUFFLE_DEPTH_MEDIUM = 20
        private const val SHUFFLE_DEPTH_HARD = 10
    }
}
