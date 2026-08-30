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
package ke.don.slideslide

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import ke.don.slideslide.ui.navigation.PuzzleRoute
import ke.don.slideslide.ui.navigation.rememberPuzzleNavigator
import ke.don.slideslide.ui.screen.PuzzleScreen
import ke.don.slideslide.ui.screen.SetupScreen
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PuzzleViewModel = hiltViewModel()
            val navigator = rememberPuzzleNavigator(
                onIntent = viewModel::onIntent,
                finishActivity = { finish() }
            )

            NavDisplay(
                backStack = navigator.state.backStack,
                onBack = { navigator.navigateBack() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<PuzzleRoute.Setup> {
                        SetupScreen(viewModel) { navigator.navigateToGame() }
                    }
                    entry<PuzzleRoute.Game> {
                        PuzzleScreen(viewModel) { navigator.navigateBack() }
                    }
                }
            )
        }
    }
}
