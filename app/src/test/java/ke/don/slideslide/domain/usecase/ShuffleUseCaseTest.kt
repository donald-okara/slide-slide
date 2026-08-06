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

import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ShuffleUseCaseTest {
    private val shuffle: ShuffleUseCase = ShuffleUseCaseImpl()

    @Test
    fun `test shuffle moves tiles from original positions`() {
        val solvedTiles =
            (0..8).map { pos ->
                Tile(id = pos, value = pos, currentPosition = pos, correctPosition = pos, isBlank = pos == 8)
            }

        val shuffledTiles = shuffle(solvedTiles, Difficulty.EASY)

        assertEquals(solvedTiles.size, shuffledTiles.size)
        // Highly likely some tiles moved
        assertNotEquals(solvedTiles.map { it.currentPosition }, shuffledTiles.map { it.currentPosition })
    }
}
