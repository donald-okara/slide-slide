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
 * Represents a single tile in the sliding puzzle.
 *
 * @property id Unique identifier for the tile.
 * @property value The number or original position index this tile represents.
 * @property currentPosition The current position of the tile on the grid (0 to size*size - 1).
 * @property isBlank Whether this tile is the empty space.
 */
data class Tile(
    val id: Int,
    val value: Int,
    val currentPosition: Int,
    val isBlank: Boolean = false
)
