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

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.don.slideslide.domain.image.BitmapCache
import ke.don.slideslide.domain.image.BitmapSlicer
import ke.don.slideslide.domain.manager.PuzzleManager
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.ui.state.PuzzleUiState
import ke.don.slideslide.ui.utils.calculateElapsedSeconds
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
        private val bitmapSlicer: BitmapSlicer,
        private val bitmapCache: BitmapCache,
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
                            timerSeconds =
                                calculateElapsedSeconds(
                                    game?.startTime,
                                    game?.endTime,
                                    clock,
                                ),
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
                                    calculateElapsedSeconds(gameStartTime, gameEndTime, clock),
                            )
                        } else {
                            this
                        }
                    }
                    delay(TIMER_UPDATE_INTERVAL_MILLIS.milliseconds)
                }
            }
        }

        fun createGame(difficulty: Difficulty) {
            executeAction {
                puzzleManager.createGame(difficulty)
                updateState {
                    copy(
                        difficulty = difficulty,
                        solutionMoves = emptyList(),
                        imageTiles = emptyList(),
                    )
                }
            }
        }

        fun selectImage(uri: Uri) {
            updateState {
                copy(
                    selectedImageUri = uri,
                    imageTiles = emptyList(),
                    error = null,
                )
            }
        }

        fun processSelectedImage(
            bitmap: Bitmap,
            difficulty: Difficulty,
        ) {
            executeAction {
                val imageUri = uiState.value.selectedImageUri ?: error("No image selected")
                val tiles =
                    bitmapCache.get(imageUri, difficulty)
                        ?: bitmapSlicer.slice(bitmap, difficulty).also { slicedTiles ->
                            bitmapCache.put(imageUri, difficulty, slicedTiles)
                        }

                updateState {
                    copy(
                        imageTiles = tiles,
                        difficulty = difficulty,
                    )
                }
            }
        }

        fun clearSelectedImage() {
            updateState {
                copy(
                    selectedImageUri = null,
                    imageTiles = emptyList(),
                )
            }
        }

        fun moveTile(move: Move) {
            executeAction {
                if (puzzleManager.moveTile(move)) {
                    updateState {
                        val recommendedMove = solutionMoves.firstOrNull()
                        val followsRecommendation =
                            recommendedMove != null &&
                                recommendedMove.fromPosition == move.fromPosition &&
                                recommendedMove.toPosition == move.toPosition

                        copy(
                            solutionMoves =
                                when {
                                    recommendedMove == null -> solutionMoves
                                    followsRecommendation -> solutionMoves.drop(1)
                                    else -> emptyList()
                                },
                        )
                    }
                }
            }
        }

        fun shuffle() {
            executeAction {
                puzzleManager.shuffle()
                updateState {
                    copy(solutionMoves = emptyList())
                }
            }
        }

        fun undo() {
            executeAction {
                if (puzzleManager.undo()) {
                    updateState {
                        copy(solutionMoves = emptyList())
                    }
                }
            }
        }

        fun reset() {
            executeAction {
                puzzleManager.reset()
                updateState {
                    copy(solutionMoves = emptyList())
                }
            }
        }

        fun clearAll() {
            executeAction {
                puzzleManager.clearAll()
                updateState {
                    copy(
                        tiles = emptyList(),
                        moveCount = 0,
                        isWon = false,
                        timerSeconds = 0,
                        gameStartTime = null,
                        gameEndTime = null,
                        solutionMoves = emptyList(),
                        selectedImageUri = null,
                        imageTiles = emptyList(),
                    )
                }
            }
        }

        fun requestSolution() {
            executeAction {
                val solution = puzzleManager.autoSolve().orEmpty()
                updateState {
                    copy(
                        solutionMoves = solution,
                    )
                }
            }
        }

        private fun updateState(reducer: PuzzleUiState.() -> PuzzleUiState) {
            _uiState.update(reducer)
        }

        private fun handleError(throwable: Throwable? = null) {
            updateState {
                copy(
                    isLoading = throwable == null,
                    error = throwable?.message ?: if (throwable == null) null else "Something went wrong",
                )
            }
        }

        private fun executeAction(action: suspend () -> Unit) {
            viewModelScope.launch {
                updateState {
                    copy(
                        isLoading = true,
                        error = null,
                    )
                }

                runCatching {
                    action()
                }.onFailure(::handleError)

                updateState {
                    copy(isLoading = false)
                }
            }
        }

        private companion object {
            const val TIMER_UPDATE_INTERVAL_MILLIS = 1_000L
        }
    }
