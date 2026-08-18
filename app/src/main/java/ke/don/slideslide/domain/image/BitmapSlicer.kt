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
package ke.don.slideslide.domain.image

import android.graphics.Bitmap
import ke.don.slideslide.domain.model.Difficulty
import javax.inject.Inject
import kotlin.math.min

interface BitmapSlicer {
    /**
     * Splits [bitmap] into equally sized, square tiles for [difficulty].
     *
     * Non-square images are center-cropped before slicing. If the cropped
     * dimension is not divisible by the grid size, the unused remainder is
     * removed from the bottom and right edges.
     */
    fun slice(
        bitmap: Bitmap,
        difficulty: Difficulty,
    ): List<Bitmap>
}

class BitmapSlicerImpl
    @Inject
    constructor() : BitmapSlicer {
        override fun slice(
            bitmap: Bitmap,
            difficulty: Difficulty,
        ): List<Bitmap> {
            require(!bitmap.isRecycled) { "Cannot slice a recycled bitmap" }

            val gridSize = difficulty.size
            val sourceSize = min(bitmap.width, bitmap.height)
            val tileSize = sourceSize / gridSize
            require(tileSize > 0) { "Bitmap is too small for the selected difficulty" }

            val croppedSize = tileSize * gridSize
            val cropLeft = (bitmap.width - croppedSize) / 2
            val cropTop = (bitmap.height - croppedSize) / 2

            return buildList(difficulty.totalTiles) {
                repeat(difficulty.totalTiles) { index ->
                    val row = index / gridSize
                    val column = index % gridSize
                    add(
                        Bitmap.createBitmap(
                            bitmap,
                            cropLeft + column * tileSize,
                            cropTop + row * tileSize,
                            tileSize,
                            tileSize,
                        ),
                    )
                }
            }
        }
    }
