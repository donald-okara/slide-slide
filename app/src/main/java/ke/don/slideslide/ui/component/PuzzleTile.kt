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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.ui.utils.SlidePreview
import ke.don.slideslide.ui.utils.SlidePreviewContent

private const val DASH_LENGTH = 10f
private val TILE_CORNER_RADIUS = 12.dp

@Composable
fun PuzzleTile(
    tile: Tile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bitmap: Bitmap? = null,
    isHighlighted: Boolean = false,
) {
    if (tile.isBlank) {
        BlankTilePlaceholder(modifier)
    } else {
        NumberOrImageTile(
            tile = tile,
            onClick = onClick,
            modifier = modifier,
            bitmap = bitmap,
            isHighlighted = isHighlighted,
        )
    }
}

@Composable
private fun BlankTilePlaceholder(modifier: Modifier = Modifier) {
    val borderColor = MaterialTheme.colorScheme.outline
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(4.dp)
                .drawBehind {
                    val stroke =
                        Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_LENGTH, DASH_LENGTH), 0f),
                        )
                    drawRoundRect(
                        color = borderColor,
                        style = stroke,
                        cornerRadius =
                            androidx.compose.ui.geometry
                                .CornerRadius(TILE_CORNER_RADIUS.toPx()),
                    )
                },
    )
}

@Composable
private fun NumberOrImageTile(
    tile: Tile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bitmap: Bitmap? = null,
    isHighlighted: Boolean = false,
) {
    val highlightColor = MaterialTheme.colorScheme.primary
    val borderModifier =
        if (isHighlighted) {
            Modifier.border(3.dp, highlightColor, RoundedCornerShape(TILE_CORNER_RADIUS))
        } else {
            Modifier
        }

    Card(
        modifier =
            modifier
                .fillMaxSize()
                .padding(4.dp)
                .then(borderModifier)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(TILE_CORNER_RADIUS),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                TileImage(bitmap)
            } else {
                TileNumber(tile.value)
            }
        }
    }
}

@Composable
private fun TileImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun TileNumber(value: Int) {
    Text(
        text = value.toString(),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
    )
}

@Suppress("UnusedPrivateMember")
@SlidePreview
@Composable
private fun PuzzleTilePreview() {
    SlidePreviewContent {
        PuzzleTile(
            tile =
                Tile(
                    id = 1,
                    value = 5,
                    currentPosition = 0,
                    correctPosition = 4,
                ),
            onClick = {},
        )
    }
}
