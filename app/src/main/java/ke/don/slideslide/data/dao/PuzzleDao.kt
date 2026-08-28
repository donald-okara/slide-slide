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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ke.don.slideslide.data.entity.GameEntity
import ke.don.slideslide.data.entity.GameWithTiles
import ke.don.slideslide.data.entity.MoveEntity
import ke.don.slideslide.data.entity.TileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for puzzle-related operations.
 */
@Dao
@Suppress("TooManyFunctions")
interface PuzzleDao {
    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    fun observeGame(gameId: Long): Flow<GameWithTiles?>

    @Transaction
    @Query("SELECT * FROM games ORDER BY id DESC LIMIT 1")
    fun observeCurrentGame(): Flow<GameWithTiles?>

    @Transaction
    @Query("SELECT * FROM games ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentGame(): GameWithTiles?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiles(tiles: List<TileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMove(move: MoveEntity): Long

    @Transaction
    suspend fun upsertGame(
        game: GameEntity,
        tiles: List<TileEntity>,
    ) {
        insertGame(game)
        insertTiles(tiles)
    }

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)

    @Query("DELETE FROM moves WHERE gameId = :gameId")
    suspend fun deleteMoves(gameId: Long)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()

    @Query("UPDATE tiles SET currentPosition = :newPosition WHERE gameId = :gameId AND id = :tileId")
    suspend fun updateTilePosition(
        gameId: Long,
        tileId: Int,
        newPosition: Int,
    )

    @Query("UPDATE games SET moveCount = :moveCount, isWon = :isWon, endTime = :endTime WHERE id = :gameId")
    suspend fun updateGameStatus(
        gameId: Long,
        moveCount: Int,
        isWon: Boolean,
        endTime: Long?,
    )

    @Query("SELECT * FROM moves WHERE gameId = :gameId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMove(gameId: Long): MoveEntity?

    @Query("DELETE FROM moves WHERE id = :moveId")
    suspend fun deleteMove(moveId: Long)

    @Transaction
    @Suppress("LongParameterList")
    suspend fun executeMove(
        gameId: Long,
        moveCount: Int,
        isWon: Boolean,
        endTime: Long?,
        move: MoveEntity,
        movingTileId: Int,
        blankTileId: Int,
    ) {
        updateGameStatus(
            gameId = gameId,
            moveCount = moveCount,
            isWon = isWon,
            endTime = endTime,
        )
        insertMove(move)
        // The tile being moved goes to the 'to' position (where blank was)
        updateTilePosition(gameId, movingTileId, move.toPosition)
        // The blank tile goes to the 'from' position (where the tile was)
        updateTilePosition(gameId, blankTileId, move.fromPosition)
    }

    @Transaction
    @Suppress("LongParameterList")
    suspend fun executeUndo(
        gameId: Long,
        moveCount: Int,
        moveId: Long,
        movingTileId: Int,
        originalPosition: Int,
        blankTileId: Int,
        blankOriginalPosition: Int,
    ) {
        updateGameStatus(
            gameId = gameId,
            moveCount = moveCount,
            isWon = false,
            endTime = null,
        )
        deleteMove(moveId)
        updateTilePosition(gameId, movingTileId, originalPosition)
        updateTilePosition(gameId, blankTileId, blankOriginalPosition)
    }
}
