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

/**
 * Interface for persisting the puzzle image to disk.
 */
interface PersistentImageCache {
    /**
     * Saves the [bitmap] to the cache.
     */
    suspend fun saveImage(bitmap: Bitmap)

    /**
     * Loads the saved image from the cache.
     * @return The cached [Bitmap], or null if not found.
     */
    suspend fun loadImage(): Bitmap?

    /**
     * Clears the cached image.
     */
    suspend fun clear()
}
