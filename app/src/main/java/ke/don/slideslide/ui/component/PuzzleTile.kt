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
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.don.slideslide.domain.model.Tile
import ke.don.slideslide.ui.theme.OnAccentPurple
import ke.don.slideslide.ui.utils.SlidePreview
import ke.don.slideslide.ui.utils.SlidePreviewContent

@Composable
fun PuzzleTile(
    tile: Tile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bitmap: Bitmap? = null,
    isHighlighted: Boolean = false,
) {
    if (tile.isBlank) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = Color(0xFF49454F),
                            style = stroke,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                        )
                    }
        )
        return
    }

    val highlightColor = MaterialTheme.colorScheme.primary
    val borderModifier = if (isHighlighted) {
        Modifier.border(3.dp, highlightColor, RoundedCornerShape(12.dp))
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
        shape = RoundedCornerShape(12.dp),
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
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = tile.value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnAccentPurple,
                )
            }
        }
    }
}

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
