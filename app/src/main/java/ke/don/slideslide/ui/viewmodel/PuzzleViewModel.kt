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
package ke.don.slideslide.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.don.slideslide.domain.manager.PuzzleManager
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.ui.state.PuzzleUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PuzzleViewModel
    @RequiresApi(Build.VERSION_CODES.O)
    @Inject
    constructor(
        private val puzzleManager: PuzzleManager,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PuzzleUiState())

        val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

        init {
            observeGame()
            startTimer()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun observeGame() {
            viewModelScope.launch {
                puzzleManager.observeGame().collect { game ->
                    updateState {
                        copy(
                            tiles = game?.tiles.orEmpty(),
                            moveCount = game?.moveCount ?: 0,
                            isWon = game?.isWon ?: false,
                            difficulty = game?.difficulty ?: difficulty,
                            gameStartTime = game?.startTime,
                            gameEndTime = game?.endTime,
                            timerSeconds = calculateElapsedSeconds(game?.startTime, game?.endTime),
                        )
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun startTimer() {
            viewModelScope.launch {
                while (isActive) {
                    updateState {
                        if (gameStartTime != null && !isWon) {
                            copy(
                                timerSeconds =
                                    calculateElapsedSeconds(
                                        gameStartTime,
                                        gameEndTime,
                                    ),
                            )
                        } else {
                            this
                        }
                    }
                    delay(TIMER_UPDATE_INTERVAL_MILLIS.milliseconds)
                }
            }
        }

        fun clearError() {
            updateState {
                copy(error = null)
            }
        }

        private fun updateState(state: PuzzleUiState.() -> PuzzleUiState) {
            _uiState.update(state)
        }

        private fun setLoading(isLoading: Boolean) {
            updateState {
                copy(isLoading = isLoading)
            }
        }

        private fun handleError(throwable: Throwable) {
            updateState {
                copy(
                    isLoading = false,
                    error = throwable.message ?: "Something went wrong",
                )
            }
        }

        fun createGame(difficulty: Difficulty) {
            executeAction {
                puzzleManager.createGame(difficulty)
                updateState {
                    copy(difficulty = difficulty)
                }
            }
        }

        fun moveTile(move: Move) {
            executeAction {
                puzzleManager.moveTile(move)
            }
        }

        fun shuffle() {
            executeAction {
                puzzleManager.shuffle()
            }
        }

        fun undo() {
            executeAction {
                puzzleManager.undo()
            }
        }

        fun reset() {
            executeAction {
                puzzleManager.reset()
            }
        }

        private fun executeAction(action: suspend () -> Unit) {
            viewModelScope.launch {
                setLoading(true)
                clearError()

                runCatching {
                    action()
                }.onFailure(::handleError)

                setLoading(false)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun calculateElapsedSeconds(
            startTime: Long?,
            endTime: Long?,
        ): Long {
            if (startTime == null) return 0

            val effectiveEndTime = endTime ?: clock.millis()
            return ((effectiveEndTime - startTime) / MILLIS_PER_SECOND).coerceAtLeast(0)
        }

        private companion object {
            const val MILLIS_PER_SECOND = 1_000L
            const val TIMER_UPDATE_INTERVAL_MILLIS = 1_000L
        }
    }
