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
package ke.don.slideslide.ui.state

import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Tile

/**
 * Represents the UI state for the puzzle screen.
 */
data class GameUiState(
    val tiles: List<Tile> = emptyList(),
    val moveCount: Int = 0,
    val isWon: Boolean = false,
    val difficulty: Difficulty = Difficulty.EASY,
    val timerSeconds: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
)
