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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.ui.component.SlideTopAppBar
import ke.don.slideslide.ui.state.PuzzleUiState
import ke.don.slideslide.ui.theme.SlideSlideTheme

class ScreenScreenshotTest {

    @PreviewTest
    @Preview(showBackground = true, name = "Setup Screen")
    @Composable
    fun SetupScreenScreenshot() {
        TestScreenWrapper(title = "Sliding Puzzle") {
            SetupContent(
                uiState = PuzzleUiState(difficulty = Difficulty.EASY),
                actions = SetupActions(
                    onIntent = {},
                    onPickImage = {},
                    onStartGame = {},
                    onInteraction = {}
                )
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true, name = "Puzzle Screen")
    @Composable
    fun PuzzleScreenScreenshot() {
        val difficulty = Difficulty.EASY
        val tiles = List(difficulty.totalTiles) { index ->
            Tile(
                id = index,
                value = index,
                currentPosition = index,
                correctPosition = index,
                isBlank = index == difficulty.totalTiles - 1
            )
        }
        TestScreenWrapper(title = "Game") {
            PuzzleContent(
                uiState = PuzzleUiState(
                    difficulty = difficulty,
                    tiles = tiles,
                    moveCount = 5,
                    timerSeconds = 42
                ),
                onIntent = {},
                onBackClick = {}
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true, name = "Image Crop Screen")
    @Composable
    fun ImageCropScreenScreenshot() {
        val bitmap = createTestBitmap()
        SlideSlideTheme {
            CropContent(
                bitmap = bitmap,
                onCancel = {},
                onConfirm = {}
            )
        }
    }

    @Composable
    private fun TestScreenWrapper(
        title: String,
        content: @Composable () -> Unit
    ) {
        SlideSlideTheme {
            Scaffold(
                topBar = {
                    SlideTopAppBar(
                        title = title,
                        isSoundEnabled = true,
                        isVibrationEnabled = true,
                        onToggleSound = {},
                        onToggleVibration = {}
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    content()
                }
            }
        }
    }

    private fun createTestBitmap(): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        paint.color = Color.BLUE
        canvas.drawCircle(size / 2f, size / 2f, size / 4f, paint)
        return bitmap
    }
}
