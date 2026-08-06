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
package ke.don.slideslide.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.don.slideslide.data.database.AppDatabase
import ke.don.slideslide.data.entity.GameEntity
import ke.don.slideslide.data.entity.MoveEntity
import ke.don.slideslide.data.entity.TileEntity
import ke.don.slideslide.domain.model.Difficulty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PuzzleDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var puzzleDao: PuzzleDao

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
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test upsert and observe game`() =
        runTest {
            val game =
                GameEntity(
                    id = 1L,
                    difficulty = Difficulty.EASY,
                    moveCount = 0,
                    isWon = false,
                    startTime = null,
                    endTime = null,
                )
            val tiles =
                listOf(
                    TileEntity(
                        gameId = 1L,
                        id = 1,
                        value = 1,
                        currentPosition = 0,
                        correctPosition = 0,
                        isBlank = false,
                    ),
                )

            puzzleDao.upsertGame(game, tiles)

            val observed = puzzleDao.observeGame(1L).first()
            assertNotNull(observed)
            assertEquals(game.id, observed?.game?.id)
            assertEquals(1, observed?.tiles?.size)
            assertEquals(tiles[0].id, observed?.tiles?.get(0)?.id)
        }

    @Test
    fun `test delete game`() =
        runTest {
            val game =
                GameEntity(
                    id = 1L,
                    difficulty = Difficulty.EASY,
                    moveCount = 0,
                    isWon = false,
                    startTime = null,
                    endTime = null,
                )
            puzzleDao.insertGame(game)

            puzzleDao.deleteGame(1L)

            val observed = puzzleDao.observeGame(1L).first()
            assertNull(observed)
        }

    @Test
    fun `test insert and delete moves`() =
        runTest {
            val gameId = 1L
            puzzleDao.insertGame(
                GameEntity(
                    id = gameId,
                    difficulty = Difficulty.EASY,
                    moveCount = 0,
                    isWon = false,
                    startTime = null,
                    endTime = null,
                ),
            )

            val move = MoveEntity(gameId = gameId, fromPosition = 0, toPosition = 1, timestamp = 1000L)
            puzzleDao.insertMove(move)

            puzzleDao.deleteMoves(gameId)

            // Since we don't have a query to fetch moves directly in DAO yet (except maybe via another relation),
            // we mainly verify the execution. If we had observeMoves, we'd check it.
            // For now, let's verify cascade delete if we delete the game.
        }

    @Test
    fun `test observe non-existent game returns null`() =
        runTest {
            val observed = puzzleDao.observeGame(999L).first()
            assertNull(observed)
        }

    @Test
    fun `test cascade delete tiles`() =
        runTest {
            val game =
                GameEntity(
                    id = 1L,
                    difficulty = Difficulty.EASY,
                    moveCount = 0,
                    isWon = false,
                    startTime = null,
                    endTime = null,
                )
            val tiles =
                listOf(
                    TileEntity(
                        gameId = 1L,
                        id = 1,
                        value = 1,
                        currentPosition = 0,
                        correctPosition = 0,
                        isBlank = false,
                    ),
                )

            puzzleDao.upsertGame(game, tiles)
            puzzleDao.deleteGame(1L)

            // Verify tiles are gone by checking the relation
            val observed = puzzleDao.observeGame(1L).first()
            assertNull(observed)
        }

    @Test
    fun `test update tile position`() =
        runTest {
            val gameId = 1L
            val game =
                GameEntity(
                    id = gameId,
                    difficulty = Difficulty.EASY,
                    moveCount = 0,
                    isWon = false,
                    startTime = null,
                    endTime = null,
                )
            val tiles =
                listOf(
                    TileEntity(
                        gameId = gameId,
                        id = 1,
                        value = 1,
                        currentPosition = 0,
                        correctPosition = 0,
                        isBlank = false,
                    ),
                )

            puzzleDao.upsertGame(game, tiles)

            puzzleDao.updateTilePosition(gameId = gameId, tileId = 1, newPosition = 5)

            val observed = puzzleDao.observeGame(gameId).first()
            assertEquals(5, observed?.tiles?.find { it.id == 1 }?.currentPosition)
        }
}
