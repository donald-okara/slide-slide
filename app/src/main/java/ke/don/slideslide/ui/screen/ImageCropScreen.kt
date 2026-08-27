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
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ke.don.slideslide.ui.state.PuzzleIntent
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel
import kotlin.math.max
import androidx.core.graphics.scale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageCropScreen(
    viewModel: PuzzleViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val bitmap = uiState.croppingImage ?: return

    BackHandler {
        viewModel.onIntent(PuzzleIntent.CancelCrop)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = max(1f, scale * zoom)
                        offset += pan
                    }
                },
        ) {
            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()
            val viewportSize = minOf(screenWidth, screenHeight) * 0.8f
            
            val viewportRect = Rect(
                offset = Offset(
                    (screenWidth - viewportSize) / 2,
                    (screenHeight - viewportSize) / 2,
                ),
                size = Size(viewportSize, viewportSize),
            )

            // Image
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
                contentScale = ContentScale.Fit,
            )

            // Viewport Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    addRect(viewportRect)
                }
                clipPath(path, clipOp = ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.7f))
                }
                // Viewport border
                drawRect(
                    color = Color.White,
                    topLeft = viewportRect.topLeft,
                    size = viewportRect.size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                )
            }

            // Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { viewModel.onIntent(PuzzleIntent.CancelCrop) },
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                }
                
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Crop Image", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }

                IconButton(
                    onClick = {
                        val croppedBitmap = cropBitmap(
                            bitmap,
                            scale,
                            offset,
                            viewportRect,
                            screenWidth,
                            screenHeight,
                        )
                        viewModel.onIntent(PuzzleIntent.ConfirmCrop(croppedBitmap))
                    },
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirm", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun cropBitmap(
    source: Bitmap,
    scale: Float,
    offset: Offset,
    viewportRect: Rect,
    screenWidth: Float,
    screenHeight: Float,
): Bitmap {
    val sourceAspect = source.width.toFloat() / source.height.toFloat()
    val screenAspect = screenWidth / screenHeight
    
    val displayedWidth: Float
    val displayedHeight: Float
    
    if (sourceAspect > screenAspect) {
        displayedWidth = screenWidth
        displayedHeight = screenWidth / sourceAspect
    } else {
        displayedHeight = screenHeight
        displayedWidth = screenHeight * sourceAspect
    }
    
    val left = (screenWidth - displayedWidth) / 2
    val top = (screenHeight - displayedHeight) / 2
    
    val currentImageRect = Rect(
        offset = Offset(
            left * scale + offset.x - (displayedWidth * (scale - 1) / 2),
            top * scale + offset.y - (displayedHeight * (scale - 1) / 2),
        ),
        size = Size(displayedWidth * scale, displayedHeight * scale),
    )
    
    val scaleFactor = source.width.toFloat() / currentImageRect.width
    
    val srcLeft = (viewportRect.left - currentImageRect.left) * scaleFactor
    val srcTop = (viewportRect.top - currentImageRect.top) * scaleFactor
    val srcSize = viewportRect.width * scaleFactor
    
    val finalBitmap = Bitmap.createBitmap(
        source,
        max(0, srcLeft.toInt()),
        max(0, srcTop.toInt()),
        minOf(srcSize.toInt(), source.width - max(0, srcLeft.toInt())),
        minOf(srcSize.toInt(), source.height - max(0, srcTop.toInt())),
    )
    
    return finalBitmap.scale(1024, 1024)
}
