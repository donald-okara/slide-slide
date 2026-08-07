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
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.domain.usecase.IsGameOverUseCase
import ke.don.slideslide.domain.usecase.ShuffleUseCase
import ke.don.slideslide.domain.usecase.SolveUseCase
import ke.don.slideslide.domain.usecase.ValidateMoveUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PuzzleManagerImpl(
    private val puzzleDao: PuzzleDao,
    private val validateMoveUseCase: ValidateMoveUseCase,
    private val isGameOverUseCase: IsGameOverUseCase,
    private val shuffleUseCase: ShuffleUseCase,
    private val solveUseCase: SolveUseCase,
) : PuzzleManager {
    private var currentGameId: Long? = null
    private var solutionMoves: MutableList<Move>? = null

    override suspend fun createGame(difficulty: Difficulty): Game {
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

        currentGameId = gameId
        solutionMoves = null
        return game.copy(id = gameId)
    }

    override suspend fun moveTile(move: Move): Boolean {
        val game = currentGameId?.let { puzzleDao.observeGame(it).first() }?.toDomain()
        val validationResult = game?.let { validateMoveUseCase(it, move) }

        return when {
            game == null || validationResult == null -> false
            else -> {
                val movingTile = validationResult.first
                val blankTile = validationResult.second

                // Check if move matches solution path
                if (solutionMoves?.firstOrNull()?.fromPosition == move.fromPosition &&
                    solutionMoves?.firstOrNull()?.toPosition == move.toPosition
                ) {
                    solutionMoves?.removeAt(0)
                } else {
                    solutionMoves = null
                }

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
        currentGameId?.let { id ->
            val game = puzzleDao.observeGame(id).first()?.toDomain() ?: return
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
            solutionMoves = null
        }
    }

    override suspend fun undo(): Boolean {
        val gameId = currentGameId
        val latestMove = gameId?.let { puzzleDao.getLatestMove(it) }
        val game = gameId?.let { puzzleDao.observeGame(it).first() }?.toDomain()

        return when {
            gameId == null || latestMove == null || game == null -> false
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

                    solutionMoves = null
                    true
                }
            }
        }
    }

    override fun observeGame(): Flow<Game?> =
        currentGameId?.let { id ->
            puzzleDao.observeGame(id).map { it?.toDomain() }
        } ?: kotlinx.coroutines.flow.flowOf(null)

    override suspend fun bestNextMove(): Move? {
        if (solutionMoves == null) {
            autoSolve()
        }
        return solutionMoves?.firstOrNull()
    }

    override suspend fun autoSolve() {
        val gameId = currentGameId ?: return
        val game = puzzleDao.observeGame(gameId).first()?.toDomain() ?: return

        val solution = solveUseCase(game)
        if (solution != null) {
            solutionMoves = solution.toMutableList()
        }
    }

    override suspend fun clearAll() {
        puzzleDao.deleteAllGames()
        currentGameId = null
        solutionMoves = null
    }
}
