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
package ke.don.slideslide.data.mapper

import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {
    @Test
    fun `test tile mapping`() {
        val domainTile = Tile(id = 1, value = 1, currentPosition = 0, correctPosition = 0, isBlank = false)
        val entityTile = domainTile.toEntity(gameId = 100L)

        assertEquals(100L, entityTile.gameId)
        assertEquals(domainTile.id, entityTile.id)
        assertEquals(domainTile.value, entityTile.value)
        assertEquals(domainTile.currentPosition, entityTile.currentPosition)
        assertEquals(domainTile.correctPosition, entityTile.correctPosition)
        assertEquals(domainTile.isBlank, entityTile.isBlank)

        val mappedBack = entityTile.toDomain()
        assertEquals(domainTile, mappedBack)
    }

    @Test
    fun `test game mapping`() {
        val domainGame =
            Game(
                id = 1L,
                difficulty = Difficulty.EASY,
                tiles = listOf(Tile(id = 1, value = 1, currentPosition = 0, correctPosition = 0, isBlank = false)),
                moveCount = 5,
                isWon = false,
                startTime = 1000L,
                endTime = null,
            )

        val entity = domainGame.toEntity()
        assertEquals(domainGame.id, entity.id)
        assertEquals(domainGame.difficulty, entity.difficulty)
        assertEquals(domainGame.moveCount, entity.moveCount)
        assertEquals(domainGame.isWon, entity.isWon)
        assertEquals(domainGame.startTime, entity.startTime)
    }
}
