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
import android.net.Uri
import android.util.LruCache
import ke.don.slideslide.domain.model.Difficulty
import javax.inject.Inject
import javax.inject.Singleton

interface BitmapCache {
    fun get(
        imageUri: Uri,
        difficulty: Difficulty,
    ): List<Bitmap>?

    fun put(
        imageUri: Uri,
        difficulty: Difficulty,
        tiles: List<Bitmap>,
    )

    fun remove(imageUri: Uri)

    fun clear()
}

@Singleton
class BitmapCacheImpl
    @Inject
    constructor() : BitmapCache {
        private val cache =
            object : LruCache<String, List<Bitmap>>(MAX_CACHE_BYTES) {
                override fun sizeOf(
                    key: String,
                    value: List<Bitmap>,
                ): Int = value.sumOf { it.byteCount }
            }

        override fun get(
            imageUri: Uri,
            difficulty: Difficulty,
        ): List<Bitmap>? = synchronized(cache) { cache.get(cacheKey(imageUri, difficulty)) }

        override fun put(
            imageUri: Uri,
            difficulty: Difficulty,
            tiles: List<Bitmap>,
        ) {
            synchronized(cache) {
                cache.put(cacheKey(imageUri, difficulty), tiles)
            }
        }

        override fun remove(imageUri: Uri) {
            synchronized(cache) {
                Difficulty.entries.forEach { difficulty ->
                    cache.remove(cacheKey(imageUri, difficulty))
                }
            }
        }

        override fun clear() {
            synchronized(cache) {
                cache.evictAll()
            }
        }

        private fun cacheKey(
            imageUri: Uri,
            difficulty: Difficulty,
        ): String = "$imageUri:${difficulty.name}"

        private companion object {
            const val MAX_CACHE_BYTES = 16 * 1024 * 1024
        }
    }
