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
package ke.don.slideslide.domain.manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.don.slideslide.data.dao.PuzzleDao
import ke.don.slideslide.data.database.AppDatabase
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.usecase.IsGameOverUseCaseImpl
import ke.don.slideslide.domain.usecase.ShuffleUseCaseImpl
import ke.don.slideslide.domain.usecase.SolveUseCaseImpl
import ke.don.slideslide.domain.usecase.ValidateMoveUseCaseImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PuzzleManagerImplTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var puzzleDao: PuzzleDao
    private lateinit var puzzleManager: PuzzleManager

    @Before
    fun setup() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    AppDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        puzzleDao = database.puzzleDao()

        puzzleManager =
            PuzzleManagerImpl(
                puzzleDao = puzzleDao,
                validateMoveUseCase = ValidateMoveUseCaseImpl(),
                isGameOverUseCase = IsGameOverUseCaseImpl(),
                shuffleUseCase = ShuffleUseCaseImpl(),
                solveUseCase = SolveUseCaseImpl(),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test create game saves to database`() =
        runTest {
            val game = puzzleManager.createGame(Difficulty.EASY)
            assertNotNull(game.id)
            assertEquals(Difficulty.EASY, game.difficulty)
            assertEquals(9, game.tiles.size)

            val observed = puzzleManager.observeGame().first()
            assertNotNull(observed)
            assertEquals(game.id, observed?.id)
        }

    @Test
    fun `test move tile updates state atomically`() =
        runTest {
            // We need a predictable state for this test, so let's bypass shuffle
            // in createGame or just use the observed state
            val game = puzzleManager.createGame(Difficulty.EASY)
            val observedGame = puzzleManager.observeGame().first()!!

            val blankTile = observedGame.tiles.find { it.isBlank }!!
            // Find an adjacent tile
            val gridSize = observedGame.difficulty.size
            val blankRow = blankTile.currentPosition / gridSize

            val adjPos =
                if (blankRow >
                    0
                ) {
                    blankTile.currentPosition - gridSize
                } else {
                    blankTile.currentPosition + gridSize
                }

            val move =
                Move(
                    gameId = game.id,
                    fromPosition = adjPos,
                    toPosition = blankTile.currentPosition,
                )
            val result = puzzleManager.moveTile(move)

            assertTrue(result)

            val updatedGame = puzzleManager.observeGame().first()
            assertEquals(1, updatedGame?.moveCount)
        }

    @Test
    fun `test undo reverts state`() =
        runTest {
            val game = puzzleManager.createGame(Difficulty.EASY)
            val observedGame = puzzleManager.observeGame().first()!!
            val blankTile = observedGame.tiles.find { it.isBlank }!!
            val adjPos =
                if (blankRow(blankTile, 3) >
                    0
                ) {
                    blankTile.currentPosition - 3
                } else {
                    blankTile.currentPosition + 3
                }

            puzzleManager.moveTile(
                Move(gameId = game.id, fromPosition = adjPos, toPosition = blankTile.currentPosition),
            )
            assertEquals(1, puzzleManager.observeGame().first()?.moveCount)

            val undoResult = puzzleManager.undo()
            assertTrue(undoResult)

            val afterUndo = puzzleManager.observeGame().first()
            assertEquals(0, afterUndo?.moveCount)
        }

    @Test
    fun `test clearAll deletes all data`() =
        runTest {
            puzzleManager.createGame(Difficulty.EASY)
            assertNotNull(puzzleManager.observeGame().first())

            puzzleManager.clearAll()

            val afterClear = puzzleManager.observeGame().first()
            assertTrue(afterClear == null)
        }

    private fun blankRow(
        tile: ke.don.slideslide.domain.model.Tile,
        size: Int,
    ) = tile.currentPosition / size
}
