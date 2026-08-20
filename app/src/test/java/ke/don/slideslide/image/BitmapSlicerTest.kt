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
package ke.don.slideslide.image

import android.graphics.Bitmap
import ke.don.slideslide.domain.image.BitmapSlicer
import ke.don.slideslide.domain.image.BitmapSlicerImpl
import ke.don.slideslide.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BitmapSlicerTest {
    private val slicer: BitmapSlicer = BitmapSlicerImpl()

    @Test
    fun `slice creates one tile per puzzle position`() {
        val bitmap = Bitmap.createBitmap(900, 600, Bitmap.Config.ARGB_8888)

        val tiles = slicer.slice(bitmap, Difficulty.EASY)

        assertEquals(9, tiles.size)
        tiles.forEach { tile ->
            assertEquals(200, tile.width)
            assertEquals(200, tile.height)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `slice rejects a bitmap smaller than one tile`() {
        slicer.slice(
            Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888),
            Difficulty.HARD,
        )
    }
}
