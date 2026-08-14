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

import ke.don.slideslide.domain.model.Game
import ke.don.slideslide.domain.model.Move
import javax.inject.Inject

/**
 * Use case to solve a sliding puzzle using the A* algorithm.
 */
interface SolveUseCase {
    /**
     * Solves the given [game] using A* search.
     *
     * @param game The current game state.
     * @return A list of moves to reach the solved state, or null if no solution found (or timeout).
     */
    operator fun invoke(game: Game): List<Move>?
}

/**
 * Welcome to the Slide Slide Hackathon!
 *
 * This class is intended to implement the an algorithm to find the optimal solution
 * for the sliding puzzle. Currently, it is just a skeleton with a [TODO] and does not
 * perform any solving logic.
 *
 * Your task is to implement the [invoke] method to return a list of [Move]s that lead
 * from the current [Game] state to the goal state.
 */
class SolveUseCaseImpl
    @Inject
    constructor() : SolveUseCase {
        override fun invoke(game: Game): List<Move>? {
            TODO()
        }
    }
