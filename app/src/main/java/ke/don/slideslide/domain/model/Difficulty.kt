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

private const val GRID_SIZE_EASY = 3
private const val GRID_SIZE_MEDIUM = 4
private const val GRID_SIZE_HARD = 5

/**
 * Represents the difficulty levels of the puzzle, determined by the grid size.
 */
enum class Difficulty(
    val size: Int,
) {
    EASY(GRID_SIZE_EASY),
    MEDIUM(GRID_SIZE_MEDIUM),
    HARD(GRID_SIZE_HARD),
    ;

    val totalTiles: Int get() = size * size
}
