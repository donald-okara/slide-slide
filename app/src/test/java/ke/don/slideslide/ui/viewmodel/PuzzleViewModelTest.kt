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
package ke.don.slideslide.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import ke.don.slideslide.domain.manager.PuzzleManager
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PuzzleViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `timer tracks elapsed time for an active game`() =
        runTest(testDispatcher) {
            val clock = TestClock(1_000L)
            val manager = FakePuzzleManager()
            val viewModel = PuzzleViewModel(manager, clock)

            manager.emitGame(startTime = 1_000L)
            runCurrent()

            assertEquals(0L, viewModel.uiState.value.timerSeconds)

            clock.currentTimeMillis = 4_000L
            advanceTimeBy(1_000L.milliseconds)
            runCurrent()

            assertEquals(3L, viewModel.uiState.value.timerSeconds)
            viewModel.viewModelScope.cancel()
        }

    @Test
    fun `timer stops at the completed game end time`() =
        runTest(testDispatcher) {
            val clock = TestClock(5_000L)
            val manager = FakePuzzleManager()
            val viewModel = PuzzleViewModel(manager, clock)

            manager.emitGame(
                startTime = 1_000L,
                endTime = 5_000L,
                isWon = true,
            )
            runCurrent()

            assertEquals(4L, viewModel.uiState.value.timerSeconds)
            viewModel.viewModelScope.cancel()

            clock.currentTimeMillis = 20_000L
            advanceTimeBy(1_000L.milliseconds)
            runCurrent()

            assertEquals(4L, viewModel.uiState.value.timerSeconds)
        }

    @Test
    fun `recommendation is consumed by a matching move and cleared by a different move`() =
        runTest(testDispatcher) {
            val manager = FakePuzzleManager()
            val firstMove = Move(gameId = 1L, fromPosition = 1, toPosition = 0)
            val secondMove = Move(gameId = 1L, fromPosition = 2, toPosition = 0)
            manager.solution = listOf(firstMove, secondMove)
            val viewModel = PuzzleViewModel(manager, TestClock(0L))

            viewModel.requestSolution()
            runCurrent()
            assertEquals(2, viewModel.uiState.value.solutionMoves.size)

            viewModel.moveTile(firstMove)
            runCurrent()
            assertEquals(listOf(secondMove), viewModel.uiState.value.solutionMoves)

            viewModel.moveTile(
                Move(gameId = 1L, fromPosition = 3, toPosition = 0),
            )
            runCurrent()
            assertEquals(emptyList<Move>(), viewModel.uiState.value.solutionMoves)
            viewModel.viewModelScope.cancel()
        }

    @Test
    fun `clear all removes recommendations from UI state`() =
        runTest(testDispatcher) {
            val manager = FakePuzzleManager()
            manager.solution = listOf(Move(gameId = 1L, fromPosition = 1, toPosition = 0))
            val viewModel = PuzzleViewModel(manager, TestClock(0L))

            viewModel.requestSolution()
            runCurrent()
            assertEquals(1, viewModel.uiState.value.solutionMoves.size)

            viewModel.clearAll()
            runCurrent()

            assertEquals(emptyList<Move>(), viewModel.uiState.value.solutionMoves)
            assertEquals(emptyList<Tile>(), viewModel.uiState.value.tiles)
            viewModel.viewModelScope.cancel()
        }

    private class TestClock(
        var currentTimeMillis: Long,
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")

        override fun withZone(zone: ZoneId): Clock = this

        override fun instant(): Instant = Instant.ofEpochMilli(currentTimeMillis)
    }

    private class FakePuzzleManager : PuzzleManager {
        private val gameState = MutableStateFlow<Game?>(null)
        var solution: List<Move> = emptyList()

        override fun observeGame(): Flow<Game?> = gameState.asStateFlow()

        fun emitGame(
            startTime: Long,
            endTime: Long? = null,
            isWon: Boolean = false,
        ) {
            gameState.value =
                Game(
                    id = 1L,
                    difficulty = Difficulty.EASY,
                    tiles = emptyTiles(),
                    isWon = isWon,
                    startTime = startTime,
                    endTime = endTime,
                )
        }

        override suspend fun createGame(difficulty: Difficulty): Game = error("Not used in this test")

        override suspend fun moveTile(move: Move): Boolean = true

        override suspend fun shuffle() = error("Not used in this test")

        override suspend fun undo(): Boolean = error("Not used in this test")

        override suspend fun reset() = error("Not used in this test")

        override suspend fun bestNextMove(): Move = error("Not used in this test")

        override suspend fun autoSolve(): List<Move> = solution

        override suspend fun clearAll() = Unit

        private fun emptyTiles(): List<Tile> = emptyList()
    }
}
