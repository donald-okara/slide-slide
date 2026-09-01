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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import ke.don.slideslide.ui.component.SlideTopAppBar
import ke.don.slideslide.ui.component.SlideTopAppBarActions
import ke.don.slideslide.ui.navigation.PuzzleRoute
import ke.don.slideslide.ui.navigation.rememberPuzzleNavigator
import ke.don.slideslide.ui.screen.PuzzleScreen
import ke.don.slideslide.ui.screen.SetupScreen
import ke.don.slideslide.ui.state.PuzzleIntent
import ke.don.slideslide.ui.theme.SlideSlideTheme
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlideSlideTheme {
                SlideApp(finishActivity = { finish() })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SlideApp(
    viewModel: PuzzleViewModel = hiltViewModel(),
    finishActivity: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator =
        rememberPuzzleNavigator(
            onIntent = viewModel::onIntent,
            finishActivity = finishActivity,
        )

    Scaffold(
        topBar = {
            SlideAppTopBar(
                navigator = navigator,
                uiState = uiState,
                onIntent = viewModel::onIntent,
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                backStack = navigator.state.backStack,
                onBack = { navigator.navigateBack() },
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider =
                    entryProvider {
                        entry<PuzzleRoute.Setup> {
                            SetupScreen(
                                viewModel = viewModel,
                            ) { navigator.navigateToGame() }
                        }
                        entry<PuzzleRoute.Game> {
                            PuzzleScreen(
                                viewModel = viewModel,
                            ) { navigator.navigateBack() }
                        }
                    },
            )
        }
    }
}

@Composable
fun SlideAppTopBar(
    navigator: ke.don.slideslide.ui.navigation.PuzzleNavigator,
    uiState: ke.don.slideslide.ui.state.PuzzleUiState,
    onIntent: (PuzzleIntent) -> Unit,
) {
    val currentRoute = navigator.state.backStack.lastOrNull()
    val title =
        when (currentRoute) {
            is PuzzleRoute.Setup -> "Slide Slide"
            is PuzzleRoute.Game -> "Game"
            else -> "Slide Slide"
        }

    SlideTopAppBar(
        title = title,
        actions =
            SlideTopAppBarActions(
                isSoundEnabled = uiState.isSoundEnabled,
                isVibrationEnabled = uiState.isVibrationEnabled,
                onToggleSound = { onIntent(PuzzleIntent.ToggleSound) },
                onToggleVibration = { onIntent(PuzzleIntent.ToggleVibration) },
            ),
        navigationIcon =
            if (navigator.state.backStack.size > 1) {
                {
                    IconButton(onClick = { navigator.navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            } else {
                null
            },
    )
}
