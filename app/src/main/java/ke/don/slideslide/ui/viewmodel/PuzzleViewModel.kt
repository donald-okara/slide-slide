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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.don.slideslide.domain.manager.PuzzleManager
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.ui.state.PuzzleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PuzzleViewModel
    @Inject
    constructor(
        private val puzzleManager: PuzzleManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PuzzleUiState())

        val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

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
    }
