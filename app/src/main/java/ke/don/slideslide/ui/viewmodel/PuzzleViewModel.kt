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
import ke.don.slideslide.ui.state.PuzzleIntent
import ke.don.slideslide.ui.state.PuzzleUiState
import ke.don.slideslide.ui.utils.calculateElapsedSeconds
import kotlinx.coroutines.Job
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
        private var autoSolveJob: Job? = null

        val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

        init {
            observeGame()
            startTimer()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun onIntent(intent: PuzzleIntent) {
            when (intent) {
                is PuzzleIntent.ChangeDifficulty -> createGame(intent.difficulty)
                is PuzzleIntent.MoveTile -> moveTile(intent.move)
                PuzzleIntent.Shuffle -> shuffle()
                PuzzleIntent.Undo -> undo()
                PuzzleIntent.Reset -> reset()
                PuzzleIntent.RequestHint -> requestSolution()
                PuzzleIntent.ToggleAutoSolve -> toggleAutoSolve()
                is PuzzleIntent.SelectImage -> selectImage(intent.uri)
                is PuzzleIntent.ProcessImage -> processSelectedImage(intent.bitmap, intent.difficulty)
                is PuzzleIntent.ConfirmCrop -> confirmCrop(intent.bitmap)
                PuzzleIntent.CancelCrop -> cancelCrop()
                PuzzleIntent.ClearImage -> clearSelectedImage()
                PuzzleIntent.ShowImagePreview -> updateState { copy(showImagePreview = true) }
                PuzzleIntent.DismissImagePreview -> updateState { copy(showImagePreview = false) }
                PuzzleIntent.DismissVictoryDialog -> updateState { copy(showVictoryDialog = false) }
                PuzzleIntent.PlayAgain -> {
                    updateState { copy(showVictoryDialog = false) }
                    shuffle()
                }
                PuzzleIntent.ClearAll -> clearAll()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun observeGame() {
            viewModelScope.launch {
                var isFirstEmission = true
                puzzleManager.observeGame().collect { game ->
                    updateState {
                        val isNewlyWon = !isFirstEmission && game?.isWon == true && !isWon
                        isFirstEmission = false
                        copy(
                            gameId = game?.id ?: 0L,
                            tiles = game?.tiles?.sortedBy { it.id }.orEmpty(),
                            moveCount = game?.moveCount ?: 0,
                            isWon = game?.isWon ?: false,
                            showVictoryDialog = if (isNewlyWon) true else showVictoryDialog,
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

        private fun createGame(difficulty: Difficulty) {
            updateState {
                copy(
                    isWon = false,
                    showVictoryDialog = false,
                    isHintActive = false,
                    solutionMoves = emptyList(),
                )
            }
            executeAction {
                puzzleManager.createGame(difficulty)
                updateState {
                    val newImageTiles = originalImage?.let {
                        bitmapSlicer.slice(it, difficulty)
                    } ?: emptyList()
                    copy(
                        difficulty = difficulty,
                        imageTiles = newImageTiles,
                    )
                }
            }
        }

        private fun selectImage(uri: Uri) {
            updateState {
                copy(
                    selectedImageUri = uri,
                    isCropping = true,
                    originalImage = null,
                    croppingImage = null,
                    imageTiles = emptyList(),
                    error = null,
                )
            }
        }

        private fun processSelectedImage(
            bitmap: Bitmap,
            difficulty: Difficulty,
        ) {
            updateState {
                copy(
                    croppingImage = bitmap,
                    difficulty = difficulty,
                )
            }
        }

        private fun confirmCrop(bitmap: Bitmap) {
            executeAction {
                val difficulty = uiState.value.difficulty
                val tiles = bitmapSlicer.slice(bitmap, difficulty)

                updateState {
                    copy(
                        originalImage = bitmap,
                        imageTiles = tiles,
                        isCropping = false,
                        croppingImage = null,
                    )
                }
            }
        }

        private fun cancelCrop() {
            updateState {
                copy(
                    isCropping = false,
                    selectedImageUri = null,
                    croppingImage = null,
                )
            }
        }

        private fun clearSelectedImage() {
            updateState {
                copy(
                    selectedImageUri = null,
                    originalImage = null,
                    imageTiles = emptyList(),
                )
            }
        }

        private fun toggleAutoSolve() {
            if (uiState.value.isAutoSolving) {
                stopAutoSolve()
            } else {
                startAutoSolve()
            }
        }

        private fun startAutoSolve() {
            autoSolveJob?.cancel()
            autoSolveJob =
                viewModelScope.launch {
                    updateState { copy(isAutoSolving = true, solutionMoves = emptyList()) }
                    val solution = puzzleManager.autoSolve()
                    if (solution != null) {
                        for (move in solution) {
                            if (!isActive) break
                            moveTile(move, isAutoMove = true)
                            delay(AUTO_SOLVE_INTERVAL_MILLIS)
                        }
                    }
                    updateState { copy(isAutoSolving = false) }
                }
        }

        private fun stopAutoSolve() {
            autoSolveJob?.cancel()
            updateState { copy(isAutoSolving = false) }
        }

        private fun moveTile(move: Move, isAutoMove: Boolean = false) {
            executeAction {
                if (puzzleManager.moveTile(move)) {
                    if (!isAutoMove) {
                        stopAutoSolve()
                    }
                    updateState {
                        val recommendedMove = solutionMoves.firstOrNull()
                        val followsRecommendation =
                            recommendedMove != null &&
                                recommendedMove.fromPosition == move.fromPosition &&
                                recommendedMove.toPosition == move.toPosition

                        copy(
                            isHintActive = false,
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

        private fun shuffle() {
            updateState {
                copy(
                    isWon = false,
                    showVictoryDialog = false,
                    isHintActive = false,
                    solutionMoves = emptyList(),
                )
            }
            executeAction {
                puzzleManager.shuffle()
            }
        }

        private fun undo() {
            executeAction {
                if (puzzleManager.undo()) {
                    updateState {
                        copy(
                            isHintActive = false,
                            solutionMoves = emptyList(),
                            showVictoryDialog = false,
                        )
                    }
                }
            }
        }

        private fun reset() {
            updateState {
                copy(
                    isWon = false,
                    showVictoryDialog = false,
                    isHintActive = false,
                    solutionMoves = emptyList(),
                )
            }
            executeAction {
                puzzleManager.reset()
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
                        showVictoryDialog = false,
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

        override fun onCleared() {
            super.onCleared()
            viewModelScope.launch {
                puzzleManager.clearAll()
            }
        }

        private fun requestSolution() {
            executeAction {
                val solution = puzzleManager.autoSolve().orEmpty()
                updateState {
                    copy(
                        isHintActive = true,
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
            const val AUTO_SOLVE_INTERVAL_MILLIS = 500L
        }
    }
