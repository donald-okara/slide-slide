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

import ke.don.slideslide.data.entity.GameEntity
import ke.don.slideslide.data.entity.GameWithTiles
import ke.don.slideslide.data.entity.MoveEntity
import ke.don.slideslide.data.entity.TileEntity
import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile

/**
 * Maps [GameWithTiles] data entity to [Game] domain model.
 */
fun GameWithTiles.toDomain(): Game =
    Game(
        id = game.id,
        difficulty = game.difficulty,
        tiles = tiles.map { it.toDomain() },
        moveCount = game.moveCount,
        isWon = game.isWon,
        startTime = game.startTime,
        endTime = game.endTime,
    )

/**
 * Maps [TileEntity] data entity to [Tile] domain model.
 */
fun TileEntity.toDomain(): Tile =
    Tile(
        id = id,
        value = value,
        currentPosition = currentPosition,
        correctPosition = correctPosition,
        isBlank = isBlank,
    )

/**
 * Maps [Game] domain model to [GameEntity] data entity.
 */
fun Game.toEntity(): GameEntity =
    GameEntity(
        id = id,
        difficulty = difficulty,
        moveCount = moveCount,
        isWon = isWon,
        startTime = startTime,
        endTime = endTime,
    )

/**
 * Maps [Tile] domain model to [TileEntity] data entity.
 */
fun Tile.toEntity(gameId: Long): TileEntity =
    TileEntity(
        gameId = gameId,
        id = id,
        value = value,
        currentPosition = currentPosition,
        correctPosition = correctPosition,
        isBlank = isBlank,
    )

/**
 * Maps [Move] domain model to [MoveEntity] data entity.
 */
fun Move.toEntity(): MoveEntity =
    MoveEntity(
        id = id,
        gameId = gameId,
        fromPosition = fromPosition,
        toPosition = toPosition,
        timestamp = timestamp,
    )
