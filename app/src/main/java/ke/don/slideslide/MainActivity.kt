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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import ke.don.slideslide.ui.screen.PuzzleScreen
import ke.don.slideslide.ui.screen.SetupScreen
import ke.don.slideslide.ui.theme.SlideSlideTheme
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed interface PuzzleRoute : NavKey {
    @Serializable data object Setup : PuzzleRoute
    @Serializable data object Game : PuzzleRoute
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlideSlideTheme {
                val backStack = remember { mutableStateListOf<NavKey>(PuzzleRoute.Setup) }
                val viewModel: PuzzleViewModel = viewModel()

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            PuzzleRoute.Setup -> NavEntry(key) {
                                SetupScreen(
                                    viewModel = viewModel,
                                    onStartGame = { backStack.add(PuzzleRoute.Game) }
                                )
                            }
                            PuzzleRoute.Game -> NavEntry(key) {
                                PuzzleScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { backStack.removeLastOrNull() }
                                )
                            }
                            else -> error("Unknown route: $key")
                        }
                    }
                )
            }
        }
    }
}
