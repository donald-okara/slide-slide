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
import ke.don.slideslide.domain.image.BitmapSlicer
import ke.don.slideslide.domain.manager.FeedbackManager
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
        private val feedbackManager: FeedbackManager,
        private val clock: Clock,
        private val bitmapSlicer: BitmapSlicer,
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
            feedbackManager.playClickFeedback()
            when (intent) {
                is PuzzleIntent.GameAction -> handleGameAction(intent)
                is PuzzleIntent.ImageAction -> handleImageAction(intent)
                is PuzzleIntent.SettingsAction -> handleSettings(intent)
                is PuzzleIntent.UiAction -> handleUi(intent)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun handleGameAction(action: PuzzleIntent.GameAction) =
            when (action) {
                is PuzzleIntent.ChangeDifficulty -> createGame(action.difficulty)
                is PuzzleIntent.MoveTile -> moveTile(action.move)
                PuzzleIntent.Shuffle ->
                    performAction {
                        if (uiState.value.tiles.isEmpty()) {
                            puzzleManager.createGame(uiState.value.difficulty)
                        } else {
                            puzzleManager.shuffle()
                        }
                    }
                PuzzleIntent.Undo ->
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
                PuzzleIntent.Reset -> performAction { puzzleManager.reset() }
                PuzzleIntent.RequestHint ->
                    executeAction {
                        val sol = puzzleManager.autoSolve().orEmpty()
                        if (sol.isNotEmpty()) feedbackManager.playHintFeedback()
                        updateState { copy(isHintActive = true, solutionMoves = sol) }
                    }
                PuzzleIntent.ToggleAutoSolve -> toggleAutoSolve()
                PuzzleIntent.PlayAgain -> {
                    updateState { copy(showVictoryDialog = false) }
                    onIntent(PuzzleIntent.Shuffle)
                }
                PuzzleIntent.ClearAll ->
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

        private fun handleImageAction(action: PuzzleIntent.ImageAction) =
            when (action) {
                is PuzzleIntent.SelectImage ->
                    updateState {
                        copy(
                            selectedImageUri = action.uri,
                            isCropping = true,
                            originalImage = null,
                            error = null,
                        )
                    }
                is PuzzleIntent.ProcessImage ->
                    updateState {
                        copy(croppingImage = action.bitmap, difficulty = action.difficulty)
                    }
                is PuzzleIntent.ConfirmCrop ->
                    executeAction {
                        val diff = uiState.value.difficulty
                        val tiles = bitmapSlicer.slice(action.bitmap, diff)
                        puzzleManager.createGame(diff)
                        updateState {
                            copy(
                                originalImage = action.bitmap,
                                imageTiles = tiles,
                                isCropping = false,
                            )
                        }
                    }
                is PuzzleIntent.CancelCrop ->
                    updateState {
                        copy(isCropping = false, selectedImageUri = null)
                    }
                is PuzzleIntent.ClearImage ->
                    updateState {
                        copy(selectedImageUri = null, originalImage = null)
                    }
            }

        private fun handleSettings(action: PuzzleIntent.SettingsAction) =
            when (action) {
                PuzzleIntent.ToggleSound ->
                    updateState {
                        val newValue = !isSoundEnabled
                        feedbackManager.setEnabled(newValue, isVibrationEnabled)
                        copy(isSoundEnabled = newValue)
                    }
                PuzzleIntent.ToggleVibration ->
                    updateState {
                        val newValue = !isVibrationEnabled
                        feedbackManager.setEnabled(isSoundEnabled, newValue)
                        copy(isVibrationEnabled = newValue)
                    }
            }

        private fun handleUi(action: PuzzleIntent.UiAction) =
            when (action) {
                PuzzleIntent.ShowImagePreview -> updateState { copy(showImagePreview = true) }
                PuzzleIntent.DismissImagePreview -> updateState { copy(showImagePreview = false) }
                PuzzleIntent.DismissVictoryDialog -> updateState { copy(showVictoryDialog = false) }
            }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun observeGame() {
            viewModelScope.launch {
                var isFirst = true
                puzzleManager.observeGame().collect { game ->
                    updateState {
                        val isNewlyWon = !isFirst && (game?.isWon == true) && !isWon
                        isFirst = false
                        if (isNewlyWon) feedbackManager.playVictoryFeedback()
                        copy(
                            gameId = game?.id ?: 0L,
                            moveCount = game?.moveCount ?: 0,
                            tiles = game?.tiles?.sortedBy { it.id }.orEmpty(),
                            isWon = game?.isWon ?: false,
                            showVictoryDialog = if (isNewlyWon) true else showVictoryDialog,
                            difficulty = game?.difficulty ?: difficulty,
                            gameStartTime = game?.startTime,
                            gameEndTime = game?.endTime,
                            timerSeconds = calculateElapsedSeconds(game?.startTime, game?.endTime, clock),
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
                            copy(timerSeconds = calculateElapsedSeconds(gameStartTime, gameEndTime, clock))
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
                    val original = uiState.value.originalImage
                    val newTiles = original?.let { bitmapSlicer.slice(it, difficulty) } ?: emptyList()
                    copy(difficulty = difficulty, imageTiles = newTiles)
                }
            }
        }

        private fun toggleAutoSolve() {
            if (uiState.value.isAutoSolving) {
                autoSolveJob?.cancel()
                updateState { copy(isAutoSolving = false) }
            } else {
                autoSolveJob?.cancel()
                autoSolveJob =
                    viewModelScope.launch {
                        updateState { copy(isAutoSolving = true, solutionMoves = emptyList()) }
                        puzzleManager.autoSolve()?.forEach { move ->
                            if (!isActive) return@forEach
                            moveTile(move, isAutoMove = true)
                            delay(AUTO_SOLVE_INTERVAL_MILLIS.milliseconds)
                        }
                        updateState { copy(isAutoSolving = false) }
                    }
            }
        }

        private fun moveTile(
            move: Move,
            isAutoMove: Boolean = false,
        ) = executeAction {
            if (puzzleManager.moveTile(move)) {
                if (!isAutoMove) {
                    autoSolveJob?.cancel()
                    updateState { copy(isAutoSolving = false) }
                }
                feedbackManager.playMoveFeedback()
                updateState {
                    val followsRec =
                        solutionMoves.firstOrNull()?.let {
                            it.fromPosition == move.fromPosition && it.toPosition == move.toPosition
                        } ?: false
                    copy(
                        isHintActive = false,
                        solutionMoves = if (followsRec) solutionMoves.drop(1) else emptyList(),
                    )
                }
            }
        }

        private fun performAction(action: suspend () -> Unit) {
            updateState {
                copy(
                    isWon = false,
                    showVictoryDialog = false,
                    isHintActive = false,
                    solutionMoves = emptyList(),
                )
            }
            executeAction(action)
        }

        override fun onCleared() {
            feedbackManager.release()
            viewModelScope.launch { puzzleManager.clearAll() }
        }

        private fun updateState(reducer: PuzzleUiState.() -> PuzzleUiState) = _uiState.update(reducer)

        private fun executeAction(action: suspend () -> Unit) {
            viewModelScope.launch {
                updateState { copy(isLoading = true, error = null) }
                runCatching { action() }.onFailure { t ->
                    updateState {
                        copy(
                            isLoading = false,
                            error = t.message ?: "Something went wrong",
                        )
                    }
                }
                updateState { copy(isLoading = false) }
            }
        }

        fun playClickFeedback() = feedbackManager.playClickFeedback()

        private companion object {
            const val TIMER_UPDATE_INTERVAL_MILLIS = 1_000L
            const val AUTO_SOLVE_INTERVAL_MILLIS = 500L
        }
    }
