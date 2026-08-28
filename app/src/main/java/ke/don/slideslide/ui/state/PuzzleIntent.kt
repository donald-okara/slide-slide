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
package ke.don.slideslide.ui.state

import android.graphics.Bitmap
import android.net.Uri
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Move

sealed interface PuzzleIntent {
    sealed interface GameAction : PuzzleIntent

    sealed interface ImageAction : PuzzleIntent

    sealed interface UiAction : PuzzleIntent

    sealed interface SettingsAction : PuzzleIntent

    data class ChangeDifficulty(
        val difficulty: Difficulty,
    ) : GameAction

    data class MoveTile(
        val move: Move,
    ) : GameAction

    data object Shuffle : GameAction

    data object Undo : GameAction

    data object Reset : GameAction

    data object RequestHint : GameAction

    data object ToggleAutoSolve : GameAction

    data object PlayAgain : GameAction

    data object ClearAll : GameAction

    data class SelectImage(
        val uri: Uri,
    ) : ImageAction

    data class ProcessImage(
        val bitmap: Bitmap,
        val difficulty: Difficulty,
    ) : ImageAction

    data class ConfirmCrop(
        val bitmap: Bitmap,
    ) : ImageAction

    data object CancelCrop : ImageAction

    data object ClearImage : ImageAction

    data object ShowImagePreview : UiAction

    data object DismissImagePreview : UiAction

    data object DismissVictoryDialog : UiAction

    data object ToggleSound : SettingsAction

    data object ToggleVibration : SettingsAction
}
