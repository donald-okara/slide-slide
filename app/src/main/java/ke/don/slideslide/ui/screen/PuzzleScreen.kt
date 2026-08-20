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
package ke.don.slideslide.ui.screen

import androidx.annotation.RequiresApi
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.ui.component.GameControls
import ke.don.slideslide.ui.component.GameStats
import ke.don.slideslide.ui.component.ImagePreviewDialog
import ke.don.slideslide.ui.component.PuzzleBoard
import ke.don.slideslide.ui.component.VictoryDialog
import ke.don.slideslide.ui.state.PuzzleIntent
import ke.don.slideslide.ui.state.PuzzleUiState
import ke.don.slideslide.ui.utils.SlidePreviewContent
import ke.don.slideslide.ui.utils.SlideScreenPreview
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    PuzzleContent(
        uiState = uiState,
        onIntent = { viewModel.onIntent(it) },
        onBackClick = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleContent(
    uiState: PuzzleUiState,
    onIntent: (PuzzleIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GameStats(
                moveCount = uiState.moveCount,
                timerSeconds = uiState.timerSeconds,
                imageBitmap = uiState.originalImage,
                onImageClick = { onIntent(PuzzleIntent.ShowImagePreview) }
            )

            Spacer(modifier = Modifier.weight(1f))

            PuzzleBoard(
                tiles = uiState.tiles,
                difficulty = uiState.difficulty,
                gameId = uiState.gameId,
                onTileClick = { onIntent(PuzzleIntent.MoveTile(it)) },
                imageTiles = uiState.imageTiles,
                highlightedPosition = if (uiState.isHintActive) uiState.solutionMoves.firstOrNull()?.fromPosition else null
            )

            Spacer(modifier = Modifier.weight(1f))

            GameControls(
                onBack = onBackClick,
                onShuffle = { onIntent(PuzzleIntent.Shuffle) },
                onHint = { onIntent(PuzzleIntent.RequestHint) },
                onAutoSolve = { onIntent(PuzzleIntent.ToggleAutoSolve) },
                isAutoSolving = uiState.isAutoSolving
            )
        }
    }

    if (uiState.showImagePreview && uiState.originalImage != null) {
        ImagePreviewDialog(
            bitmap = uiState.originalImage,
            onDismiss = { onIntent(PuzzleIntent.DismissImagePreview) }
        )
    }

    if (uiState.showVictoryDialog) {
        VictoryDialog(
            moveCount = uiState.moveCount,
            timerSeconds = uiState.timerSeconds,
            onDismiss = { onIntent(PuzzleIntent.DismissVictoryDialog) },
            onPlayAgain = { onIntent(PuzzleIntent.PlayAgain) },
            onChooseAnotherImage = {
                onIntent(PuzzleIntent.DismissVictoryDialog)
                onBackClick()
            }
        )
    }
}

@SlideScreenPreview
@Composable
private fun PuzzleContentPreview() {
    SlidePreviewContent(withPadding = false) {
        PuzzleContent(
            uiState =
                PuzzleUiState(
                    difficulty = Difficulty.EASY,
                    moveCount = 10,
                    timerSeconds = 120,
                ),
            onIntent = {},
            onBackClick = {},
        )
    }
}
