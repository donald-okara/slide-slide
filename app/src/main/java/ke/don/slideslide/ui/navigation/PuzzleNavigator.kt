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
package ke.don.slideslide.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ke.don.slideslide.ui.state.PuzzleIntent

class PuzzleNavigator(
    val state: PuzzleNavState,
    private val onIntent: (PuzzleIntent) -> Unit,
    private val finishActivity: () -> Unit,
) {
    fun navigateToGame() {
        state.backStack.add(PuzzleRoute.Game)
    }

    fun navigateBack() {
        onIntent(PuzzleIntent.ClearAll)
        if (state.backStack.size > 1) {
            state.backStack.removeAt(state.backStack.size - 1)
        } else {
            finishActivity()
        }
    }
}

@Composable
fun rememberPuzzleNavigator(
    state: PuzzleNavState = rememberPuzzleNavState(),
    onIntent: (PuzzleIntent) -> Unit,
    finishActivity: () -> Unit,
): PuzzleNavigator =
    remember(state, onIntent, finishActivity) {
        PuzzleNavigator(state, onIntent, finishActivity)
    }
