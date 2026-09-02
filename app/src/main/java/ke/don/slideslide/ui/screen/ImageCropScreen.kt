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
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.don.slideslide.ui.state.PuzzleIntent
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel
import kotlin.math.max

@Suppress("MagicNumber")
private const val OVERLAY_ALPHA = 0.7f

@Suppress("MagicNumber")
private const val VIEWPORT_SCREEN_RATIO = 0.8f

@Suppress("MagicNumber")
private const val OUTPUT_IMAGE_SIZE = 1024

@Immutable
private data class CropParams(
    val scale: Float,
    val offset: Offset,
    val viewportRect: Rect,
    val screenWidth: Float,
    val screenHeight: Float,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageCropScreen(viewModel: PuzzleViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmap = uiState.croppingImage ?: return

    BackHandler {
        viewModel.onIntent(PuzzleIntent.CancelCrop)
    }

    CropContent(
        bitmap = bitmap,
        onCancel = { viewModel.onIntent(PuzzleIntent.CancelCrop) },
        onConfirm = { croppedBitmap ->
            viewModel.onIntent(PuzzleIntent.ConfirmCrop(croppedBitmap))
        },
    )
}

@Composable
fun CropContent(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onConfirm: (Bitmap) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim),
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        BoxWithConstraints(
            modifier =
                Modifier
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
            val viewportSize = minOf(screenWidth, screenHeight) * VIEWPORT_SCREEN_RATIO

            val viewportRect =
                Rect(
                    offset =
                        Offset(
                            (screenWidth - viewportSize) / 2,
                            (screenHeight - viewportSize) / 2,
                        ),
                    size = Size(viewportSize, viewportSize),
                )

            CropImageLayer(bitmap, scale, offset)

            CropOverlay(viewportRect)

            CropControls(
                onCancel = onCancel,
                onConfirm = {
                    val croppedBitmap =
                        cropBitmap(
                            bitmap,
                            CropParams(scale, offset, viewportRect, screenWidth, screenHeight),
                        )
                    onConfirm(croppedBitmap)
                },
            )
        }
    }
}

@Composable
private fun CropImageLayer(
    bitmap: Bitmap,
    scale: Float,
    offset: Offset,
) {
    androidx.compose.foundation.Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun CropOverlay(viewportRect: Rect) {
    val scrimColor = MaterialTheme.colorScheme.scrim
    val outlineColor = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path =
            Path().apply {
                addRect(viewportRect)
            }
        clipPath(path, clipOp = ClipOp.Difference) {
            drawRect(scrimColor.copy(alpha = OVERLAY_ALPHA))
        }
        // Viewport border
        drawRect(
            color = outlineColor,
            topLeft = viewportRect.topLeft,
            size = viewportRect.size,
            style =
                androidx.compose.ui.graphics.drawscope
                    .Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun CropControls(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCancel) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                "Crop Image",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        IconButton(onClick = onConfirm) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Confirm",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun cropBitmap(
    source: Bitmap,
    params: CropParams,
): Bitmap {
    val sourceAspect = source.width.toFloat() / source.height.toFloat()
    val screenAspect = params.screenWidth / params.screenHeight

    val dispWidth: Float
    val dispHeight: Float

    if (sourceAspect > screenAspect) {
        dispWidth = params.screenWidth
        dispHeight = params.screenWidth / sourceAspect
    } else {
        dispHeight = params.screenHeight
        dispWidth = params.screenHeight * sourceAspect
    }

    val left = (params.screenWidth - dispWidth) / 2
    val top = (params.screenHeight - dispHeight) / 2

    val curRect =
        Rect(
            offset =
                Offset(
                    (left * params.scale + params.offset.x - (dispWidth * (params.scale - 1) / 2)),
                    (top * params.scale + params.offset.y - (dispHeight * (params.scale - 1) / 2)),
                ),
            size = Size(dispWidth * params.scale, dispHeight * params.scale),
        )

    val factor = source.width.toFloat() / curRect.width
    val srcL = (params.viewportRect.left - curRect.left) * factor
    val srcT = (params.viewportRect.top - curRect.top) * factor
    val srcS = params.viewportRect.width * factor

    val finalBmp =
        Bitmap.createBitmap(
            source,
            max(0, srcL.toInt()),
            max(0, srcT.toInt()),
            minOf(srcS.toInt(), source.width - max(0, srcL.toInt())),
            minOf(srcS.toInt(), source.height - max(0, srcT.toInt())),
        )

    return finalBmp.scale(OUTPUT_IMAGE_SIZE, OUTPUT_IMAGE_SIZE)
}
