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
package ke.don.slideslide.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.ui.theme.SlideSlideTheme

class PuzzleBoardScreenshotTest {
    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun PuzzleBoardEasyPreview() {
        val difficulty = Difficulty.EASY
        val tiles =
            List(difficulty.totalTiles) { index ->
                Tile(
                    id = index,
                    value = index,
                    currentPosition = index,
                    correctPosition = index,
                    isBlank = index == difficulty.totalTiles - 1,
                )
            }
        SlideSlideTheme {
            PuzzleBoard(
                state =
                    PuzzleBoardState(
                        tiles = tiles,
                        difficulty = difficulty,
                        gameId = 0L,
                    ),
                onTileClick = {},
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun PuzzleBoardMediumPreview() {
        val difficulty = Difficulty.MEDIUM
        val tiles =
            List(difficulty.totalTiles) { index ->
                Tile(
                    id = index,
                    value = index,
                    currentPosition = index,
                    correctPosition = index,
                    isBlank = index == difficulty.totalTiles - 1,
                )
            }
        SlideSlideTheme {
            PuzzleBoard(
                state =
                    PuzzleBoardState(
                        tiles = tiles,
                        difficulty = difficulty,
                        gameId = 0L,
                    ),
                onTileClick = {},
            )
        }
    }
}
