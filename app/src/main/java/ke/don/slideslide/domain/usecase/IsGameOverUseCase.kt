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
package ke.don.slideslide.domain.usecase

import ke.don.slideslide.domain.model.Tile
import javax.inject.Inject

/**
 * Use case to determine if the puzzle is in its solved state.
 */
interface IsGameOverUseCase {
    /**
     * Checks if all [tiles] are in their correct positions.
     *
     * @param tiles The current list of tiles in the game.
     * @return true if every tile's currentPosition matches its correctPosition, false otherwise.
     */
    operator fun invoke(tiles: List<Tile>): Boolean
}

class IsGameOverUseCaseImpl @Inject constructor() : IsGameOverUseCase {
    override fun invoke(tiles: List<Tile>): Boolean {
        if (tiles.isEmpty()) return false
        return tiles.all { it.currentPosition == it.correctPosition }
    }
}
