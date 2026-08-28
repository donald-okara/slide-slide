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

import android.graphics.Bitmap
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import ke.don.slideslide.domain.image.BitmapSlicerImpl
import ke.don.slideslide.domain.manager.FeedbackManager
import ke.don.slideslide.domain.manager.PuzzleManager
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.ui.state.PuzzleIntent
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
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
            val viewModel = createViewModel(manager, clock)

            try {
                manager.emitGame(startTime = 1_000L)
                runCurrent()

                assertEquals(0L, viewModel.uiState.value.timerSeconds)

                clock.currentTimeMillis = 4_000L
                advanceTimeBy(1_000L.milliseconds)
                runCurrent()

                assertEquals(3L, viewModel.uiState.value.timerSeconds)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `timer stops at the completed game end time`() =
        runTest(testDispatcher) {
            val clock = TestClock(5_000L)
            val manager = FakePuzzleManager()
            val viewModel = createViewModel(manager, clock)

            try {
                manager.emitGame(
                    startTime = 1_000L,
                    endTime = 5_000L,
                    isWon = true,
                )
                runCurrent()

                assertEquals(4L, viewModel.uiState.value.timerSeconds)

                clock.currentTimeMillis = 20_000L
                advanceTimeBy(1_000L.milliseconds)
                runCurrent()

                assertEquals(4L, viewModel.uiState.value.timerSeconds)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `recommendation is consumed by a matching move and cleared by a different move`() =
        runTest(testDispatcher) {
            val manager = FakePuzzleManager()
            val firstMove = Move(gameId = 1L, fromPosition = 1, toPosition = 0)
            val secondMove = Move(gameId = 1L, fromPosition = 2, toPosition = 0)
            manager.solution = listOf(firstMove, secondMove)
            val viewModel = createViewModel(manager, TestClock(0L))

            try {
                viewModel.onIntent(PuzzleIntent.RequestHint)
                runCurrent()
                assertEquals(2, viewModel.uiState.value.solutionMoves.size)

                viewModel.onIntent(PuzzleIntent.MoveTile(firstMove))
                runCurrent()
                assertEquals(listOf(secondMove), viewModel.uiState.value.solutionMoves)

                viewModel.onIntent(
                    PuzzleIntent.MoveTile(Move(gameId = 1L, fromPosition = 3, toPosition = 0)),
                )
                runCurrent()
                assertEquals(emptyList<Move>(), viewModel.uiState.value.solutionMoves)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `shuffle creates a game if the grid is empty`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L))
            try {
                runCurrent()
                assertEquals(0, viewModel.uiState.value.tiles.size)

                viewModel.onIntent(PuzzleIntent.Shuffle)
                runCurrent()

                assertEquals(9, viewModel.uiState.value.tiles.size)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `clear all removes recommendations from UI state`() =
        runTest(testDispatcher) {
            val manager = FakePuzzleManager()
            manager.solution = listOf(Move(gameId = 1L, fromPosition = 1, toPosition = 0))
            val viewModel = createViewModel(manager, TestClock(0L))

            try {
                viewModel.onIntent(PuzzleIntent.RequestHint)
                runCurrent()
                assertEquals(1, viewModel.uiState.value.solutionMoves.size)

                viewModel.onIntent(PuzzleIntent.ClearAll)
                runCurrent()

                assertEquals(emptyList<Move>(), viewModel.uiState.value.solutionMoves)
                assertEquals(emptyList<Tile>(), viewModel.uiState.value.tiles)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `select image sets isCropping to true`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L))
            try {
                val imageUri = Uri.parse("content://images/1")
                runCurrent()

                viewModel.onIntent(PuzzleIntent.SelectImage(imageUri))

                assertEquals(imageUri, viewModel.uiState.value.selectedImageUri)
                assertEquals(true, viewModel.uiState.value.isCropping)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `confirm crop updates originalImage, populates grid, and sets isCropping to false`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L))
            try {
                val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                runCurrent()

                viewModel.onIntent(PuzzleIntent.ConfirmCrop(bitmap))
                runCurrent()

                assertEquals(bitmap, viewModel.uiState.value.originalImage)
                assertEquals(false, viewModel.uiState.value.isCropping)
                assertEquals(9, viewModel.uiState.value.imageTiles.size)
                assertEquals(9, viewModel.uiState.value.tiles.size)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `cancel crop resets cropping state`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L))
            try {
                runCurrent()
                viewModel.onIntent(PuzzleIntent.SelectImage(Uri.parse("content://images/1")))
                assertEquals(true, viewModel.uiState.value.isCropping)

                viewModel.onIntent(PuzzleIntent.CancelCrop)
                runCurrent()

                assertEquals(null, viewModel.uiState.value.selectedImageUri)
                assertEquals(false, viewModel.uiState.value.isCropping)
                assertEquals(null, viewModel.uiState.value.croppingImage)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `clear selected image removes the uri from UI state`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L))
            try {
                runCurrent()
                viewModel.onIntent(PuzzleIntent.SelectImage(Uri.parse("content://images/1")))

                viewModel.onIntent(PuzzleIntent.ClearImage)

                assertEquals(null, viewModel.uiState.value.selectedImageUri)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `move feedback is triggered on tile move`() =
        runTest(testDispatcher) {
            val manager = FakePuzzleManager()
            val feedback = FakeFeedbackManager()
            val viewModel = createViewModel(manager, TestClock(0L), feedback)
            val move = Move(gameId = 1L, fromPosition = 1, toPosition = 0)

            try {
                viewModel.onIntent(PuzzleIntent.MoveTile(move))
                runCurrent()

                assertEquals(1, feedback.moveFeedbackCount)
                // Also triggered click feedback from onIntent
                assertEquals(1, feedback.clickFeedbackCount)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `toggle sound updates UI state and feedback manager`() =
        runTest(testDispatcher) {
            val feedback = FakeFeedbackManager()
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L), feedback)

            try {
                assertEquals(true, viewModel.uiState.value.isSoundEnabled)

                viewModel.onIntent(PuzzleIntent.ToggleSound)
                runCurrent()

                assertEquals(false, viewModel.uiState.value.isSoundEnabled)
                assertEquals(false, feedback.soundEnabled)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `toggle vibration updates UI state and feedback manager`() =
        runTest(testDispatcher) {
            val feedback = FakeFeedbackManager()
            val viewModel = createViewModel(FakePuzzleManager(), TestClock(0L), feedback)

            try {
                assertEquals(true, viewModel.uiState.value.isVibrationEnabled)

                viewModel.onIntent(PuzzleIntent.ToggleVibration)
                runCurrent()

                assertEquals(false, viewModel.uiState.value.isVibrationEnabled)
                assertEquals(false, feedback.vibrationEnabled)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `victory feedback is triggered on win`() =
        runTest(testDispatcher) {
            val manager = FakePuzzleManager()
            val feedback = FakeFeedbackManager()
            val viewModel = createViewModel(manager, TestClock(0L), feedback)

            try {
                // First emission: not won
                manager.emitGame(startTime = 1000L, isWon = false)
                runCurrent()

                // Second emission: won
                manager.emitGame(startTime = 1000L, isWon = true)
                runCurrent()

                assertEquals(1, feedback.victoryFeedbackCount)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    private fun createViewModel(
        manager: PuzzleManager,
        clock: Clock,
        feedback: FeedbackManager = FakeFeedbackManager(),
    ): PuzzleViewModel =
        PuzzleViewModel(
            puzzleManager = manager,
            feedbackManager = feedback,
            clock = clock,
            bitmapSlicer = BitmapSlicerImpl(),
        )

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

        override suspend fun createGame(difficulty: Difficulty): Game {
            val game =
                Game(
                    id = 1L,
                    difficulty = difficulty,
                    tiles =
                        (0 until difficulty.totalTiles).map {
                            Tile(
                                id = it,
                                value = it,
                                currentPosition = it,
                                correctPosition = it,
                            )
                        },
                    startTime = 1000L,
                )
            gameState.value = game
            return game
        }

        override suspend fun moveTile(move: Move): Boolean = true

        override suspend fun shuffle() = error("Not used in this test")

        override suspend fun undo(): Boolean = error("Not used in this test")

        override suspend fun reset() = error("Not used in this test")

        override suspend fun bestNextMove(): Move = error("Not used in this test")

        override suspend fun autoSolve(): List<Move> = solution

        override suspend fun clearAll() = Unit

        private fun emptyTiles(): List<Tile> = emptyList()
    }

    private class FakeFeedbackManager : FeedbackManager {
        var moveFeedbackCount = 0
        var hintFeedbackCount = 0
        var victoryFeedbackCount = 0
        var clickFeedbackCount = 0
        var vibrateCount = 0
        var soundEnabled = true
        var vibrationEnabled = true
        var isReleased = false

        override fun setEnabled(
            soundEnabled: Boolean,
            vibrationEnabled: Boolean,
        ) {
            this.soundEnabled = soundEnabled
            this.vibrationEnabled = vibrationEnabled
        }

        override fun playMoveFeedback() {
            moveFeedbackCount++
        }

        override fun playHintFeedback() {
            hintFeedbackCount++
        }

        override fun playVictoryFeedback() {
            victoryFeedbackCount++
        }

        override fun playClickFeedback() {
            clickFeedbackCount++
        }

        override fun playVibrate() {
            vibrateCount++
        }

        override fun release() {
            isReleased = true
        }
    }
}
