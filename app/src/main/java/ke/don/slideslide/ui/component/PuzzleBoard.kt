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

import android.graphics.Bitmap
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.domain.model.Move
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.ui.theme.SurfaceGrey
import ke.don.slideslide.ui.utils.SlidePreview
import ke.don.slideslide.ui.utils.SlidePreviewContent
import kotlin.math.abs

private const val TILE_ANIMATION_DURATION = 300
private val BOARD_PADDING = 8.dp

@Immutable
data class PuzzleBoardState(
    val tiles: List<Tile>,
    val difficulty: Difficulty,
    val gameId: Long,
    val imageTiles: List<Bitmap> = emptyList(),
    val highlightedPosition: Int? = null,
)

@Composable
fun PuzzleBoard(
    state: PuzzleBoardState,
    onTileClick: (Move) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .aspectRatio(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceGrey)
                .padding(BOARD_PADDING),
    ) {
        val gridSize = state.difficulty.size
        val tileSizeDp = maxWidth / gridSize
        val blankTile = state.tiles.find { it.isBlank }

        state.tiles.forEach { tile ->
            AnimatedTile(
                tile = tile,
                gridSize = gridSize,
                tileSizeDp = tileSizeDp,
                blankTile = blankTile,
                gameId = state.gameId,
                onTileClick = onTileClick,
                imageTiles = state.imageTiles,
                highlightedPosition = state.highlightedPosition,
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun AnimatedTile(
    tile: Tile,
    gridSize: Int,
    tileSizeDp: Dp,
    blankTile: Tile?,
    gameId: Long,
    onTileClick: (Move) -> Unit,
    imageTiles: List<Bitmap>,
    highlightedPosition: Int?,
) {
    val row = tile.currentPosition / gridSize
    val col = tile.currentPosition % gridSize

    val animatedOffsetX by animateDpAsState(
        targetValue = tileSizeDp * col,
        animationSpec = tween(durationMillis = TILE_ANIMATION_DURATION),
        label = "TileX_${tile.id}",
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = tileSizeDp * row,
        animationSpec = tween(durationMillis = TILE_ANIMATION_DURATION),
        label = "TileY_${tile.id}",
    )

    Box(
        modifier =
            Modifier
                .size(tileSizeDp)
                .offset(x = animatedOffsetX, y = animatedOffsetY),
    ) {
        PuzzleTile(
            tile = tile,
            onClick = {
                if ((blankTile != null) &&
                    isAdjacent(tile.currentPosition, blankTile.currentPosition, gridSize)
                ) {
                    onTileClick(
                        Move(
                            gameId = gameId,
                            fromPosition = tile.currentPosition,
                            toPosition = blankTile.currentPosition,
                        ),
                    )
                }
            },
            bitmap =
                if (imageTiles.isNotEmpty() && !tile.isBlank) {
                    imageTiles.getOrNull(tile.value)
                } else {
                    null
                },
            isHighlighted = highlightedPosition == tile.currentPosition,
        )
    }
}

private fun isAdjacent(
    pos1: Int,
    pos2: Int,
    size: Int,
): Boolean {
    val row1 = pos1 / size
    val col1 = pos1 % size
    val row2 = pos2 / size
    val col2 = pos2 % size
    return (abs(row1 - row2) == 1 && col1 == col2) || (abs(col1 - col2) == 1 && row1 == row2)
}

@Suppress("UnusedPrivateMember")
@SlidePreview
@Composable
private fun PuzzleBoardPreview() {
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
    SlidePreviewContent {
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
