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

import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import kotlinx.coroutines.flow.Flow

/**
 * The core contract for managing puzzle logic and state.
 * This is the primary entry point into the domain layer.
 */
interface PuzzleManager {
    /**
     * Creates a new game with the specified difficulty.
     */
    suspend fun createGame(difficulty: Difficulty): Game

    /**
     * Moves a tile to the blank space if it is a legal move.
     * @return true if the move was successful, false otherwise.
     */
    suspend fun moveTile(move: Move): Boolean

    /**
     * Shuffles the tiles of the current game.
     */
    suspend fun shuffle()

    /**
     * Reverts the last move made.
     * @return true if undo was successful.
     */
    suspend fun undo(): Boolean

    suspend fun reset()

    /**
     * Observes the current game state.
     */
    fun observeGame(): Flow<Game?>

    /**
     * Provides a hint for the best next move.
     */
    suspend fun bestNextMove(): Move?

    /**
     * Automatically solves the puzzle from the current state.
     */
    suspend fun autoSolve(): List<Move>?

    /**
     * Clears all game data from persistence and resets the manager state.
     */
    suspend fun clearAll()
}
