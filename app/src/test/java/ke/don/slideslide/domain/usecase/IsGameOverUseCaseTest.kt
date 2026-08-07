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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsGameOverUseCaseTest {
    private val isGameOver: IsGameOverUseCase = IsGameOverUseCaseImpl()

    @Test
    fun `test solved state returns true`() {
        val tiles =
            listOf(
                Tile(id = 0, value = 0, currentPosition = 0, correctPosition = 0),
                Tile(id = 1, value = 1, currentPosition = 1, correctPosition = 1),
                Tile(id = 2, value = 2, currentPosition = 2, correctPosition = 2, isBlank = true),
            )
        assertTrue(isGameOver(tiles))
    }

    @Test
    fun `test unsolved state returns false`() {
        val tiles =
            listOf(
                Tile(id = 0, value = 0, currentPosition = 1, correctPosition = 0),
                Tile(id = 1, value = 1, currentPosition = 0, correctPosition = 1),
                Tile(id = 2, value = 2, currentPosition = 2, correctPosition = 2, isBlank = true),
            )
        assertFalse(isGameOver(tiles))
    }

    @Test
    fun `test empty tiles returns false`() {
        assertFalse(isGameOver(emptyList()))
    }
}
