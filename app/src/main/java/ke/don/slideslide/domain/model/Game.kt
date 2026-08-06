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
package ke.don.slideslide.domain.model

/**
 * Represents the state of a puzzle game.
 *
 * @property id Unique identifier for the game.
 * @property difficulty The difficulty (size) of the puzzle.
 * @property tiles The list of tiles in their current positions.
 * @property moveCount Total number of moves made.
 * @property isWon Whether the puzzle is in its solved state.
 * @property startTime Epoch time when the game started.
 * @property endTime Epoch time when the game was completed.
 */
data class Game(
    val id: Long = 0,
    val difficulty: Difficulty,
    val tiles: List<Tile>,
    val moveCount: Int = 0,
    val isWon: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null,
)
