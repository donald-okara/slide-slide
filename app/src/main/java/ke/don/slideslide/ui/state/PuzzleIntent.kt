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
    data class ChangeDifficulty(val difficulty: Difficulty) : PuzzleIntent
    data class MoveTile(val move: Move) : PuzzleIntent
    data object Shuffle : PuzzleIntent
    data object Undo : PuzzleIntent
    data object Reset : PuzzleIntent
    data object RequestHint : PuzzleIntent
    data class SelectImage(val uri: Uri) : PuzzleIntent
    data class ProcessImage(val bitmap: Bitmap, val difficulty: Difficulty) : PuzzleIntent
    data object ClearImage : PuzzleIntent
    data class ConfirmCrop(val bitmap: Bitmap) : PuzzleIntent
    data object CancelCrop : PuzzleIntent
    data object ShowImagePreview : PuzzleIntent
    data object DismissImagePreview : PuzzleIntent
    data object ToggleAutoSolve : PuzzleIntent
    data object ToggleSound : PuzzleIntent
    data object ToggleVibration : PuzzleIntent
    data object DismissVictoryDialog : PuzzleIntent
    data object PlayAgain : PuzzleIntent
    data object ClearAll : PuzzleIntent
}
