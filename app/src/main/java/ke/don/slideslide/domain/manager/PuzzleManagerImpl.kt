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

import ke.don.slideslide.data.dao.PuzzleDao
import ke.don.slideslide.data.mapper.toDomain
import ke.don.slideslide.data.mapper.toEntity
import ke.don.slideslide.di.IoDispatcher
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.domain.usecase.IsGameOverUseCase
import ke.don.slideslide.domain.usecase.ShuffleUseCase
import ke.don.slideslide.domain.usecase.SolveUseCase
import ke.don.slideslide.domain.usecase.ValidateMoveUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PuzzleManagerImpl
    @Inject
    constructor(
        private val puzzleDao: PuzzleDao,
        private val validateMoveUseCase: ValidateMoveUseCase,
        private val isGameOverUseCase: IsGameOverUseCase,
        private val shuffleUseCase: ShuffleUseCase,
        private val solveUseCase: SolveUseCase,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : PuzzleManager {
        override suspend fun createGame(difficulty: Difficulty): Game =
            withContext(ioDispatcher) {
                puzzleDao.deleteAllGames()

                val totalTiles = difficulty.totalTiles
                val solvedTiles =
                    (0 until totalTiles).map { index ->
                        Tile(
                            id = index,
                            value = index,
                            currentPosition = index,
                            correctPosition = index,
                            isBlank = index == totalTiles - 1,
                        )
                    }

                val shuffledTiles = shuffleUseCase(solvedTiles, difficulty)

                val game =
                    Game(
                        difficulty = difficulty,
                        tiles = shuffledTiles,
                        startTime = System.currentTimeMillis(),
                    )

                val gameId = puzzleDao.insertGame(game.toEntity())
                puzzleDao.insertTiles(shuffledTiles.map { it.toEntity(gameId) })

                game.copy(id = gameId)
            }

        override suspend fun moveTile(move: Move): Boolean =
            withContext(ioDispatcher) {
                val game = puzzleDao.getCurrentGame()?.toDomain()
                val validationResult = game?.let { validateMoveUseCase(it, move) }

                when {
                    game == null || validationResult == null -> {
                        false
                    }

                    else -> {
                        val movingTile = validationResult.first
                        val blankTile = validationResult.second

                        val newMoveCount = game.moveCount + 1
                        val updatedTiles =
                            game.tiles.map { tile ->
                                when (tile.id) {
                                    movingTile.id -> tile.copy(currentPosition = move.toPosition)
                                    blankTile.id -> tile.copy(currentPosition = move.fromPosition)
                                    else -> tile
                                }
                            }

                        val isWon = isGameOverUseCase(updatedTiles)
                        val endTime = if (isWon) System.currentTimeMillis() else null

                        puzzleDao.executeMove(
                            gameId = game.id,
                            moveCount = newMoveCount,
                            isWon = isWon,
                            endTime = endTime,
                            move = move.toEntity(),
                            movingTileId = movingTile.id,
                            blankTileId = blankTile.id,
                        )
                        true
                    }
                }
            }

        override suspend fun shuffle() {
            withContext(ioDispatcher) {
                puzzleDao.getCurrentGame()?.toDomain()?.let { game ->
                    val id = game.id
                    val shuffledTiles = shuffleUseCase(game.tiles, game.difficulty)

                    puzzleDao.upsertGame(
                        game
                            .copy(
                                tiles = shuffledTiles,
                                moveCount = 0,
                                isWon = false,
                                startTime = System.currentTimeMillis(),
                            ).toEntity(),
                        shuffledTiles.map { it.toEntity(id) },
                    )
                    puzzleDao.deleteMoves(id)
                }
            }
        }

        override suspend fun undo(): Boolean =
            withContext(ioDispatcher) {
                val game = puzzleDao.getCurrentGame()?.toDomain()
                val gameId = game?.id
                val latestMove = gameId?.let { puzzleDao.getLatestMove(it) }

                when {
                    game == null || latestMove == null -> {
                        false
                    }

                    else -> {
                        val movingTile = game.tiles.find { it.currentPosition == latestMove.toPosition }
                        val blankTile = game.tiles.find { it.currentPosition == latestMove.fromPosition }

                        if (movingTile == null || blankTile == null) {
                            false
                        } else {
                            puzzleDao.executeUndo(
                                gameId = gameId,
                                moveCount = (game.moveCount - 1).coerceAtLeast(0),
                                moveId = latestMove.id,
                                movingTileId = movingTile.id,
                                originalPosition = latestMove.fromPosition,
                                blankTileId = blankTile.id,
                                blankOriginalPosition = latestMove.toPosition,
                            )

                            true
                        }
                    }
                }
            }

        override suspend fun reset() {
            withContext(ioDispatcher) {
                val difficulty =
                    puzzleDao.getCurrentGame()?.toDomain()?.difficulty
                        ?: Difficulty.EASY

                createGame(difficulty)
            }
        }

        override fun observeGame(): Flow<Game?> =
            puzzleDao
                .observeCurrentGame()
                .map { it?.toDomain() }
                .flowOn(ioDispatcher)

        override suspend fun bestNextMove(): Move? =
            withContext(ioDispatcher) {
                autoSolve()?.firstOrNull()
            }

        override suspend fun autoSolve(): List<Move>? =
            withContext(ioDispatcher) {
                puzzleDao.getCurrentGame()?.toDomain()?.let { game -> solveUseCase(game) }
            }

        override suspend fun clearAll() {
            withContext(ioDispatcher) {
                puzzleDao.deleteAllGames()
            }
        }
    }
