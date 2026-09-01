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
package ke.don.slideslide.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.don.slideslide.di.IoDispatcher
import ke.don.slideslide.domain.image.PersistentImageCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistentImageCacheImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : PersistentImageCache {
        private val cacheFile by lazy { File(context.filesDir, CACHE_FILE_NAME) }

        override suspend fun saveImage(bitmap: Bitmap) =
            withContext(ioDispatcher) {
                runCatching {
                    FileOutputStream(cacheFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }.getOrThrow()
            }

        override suspend fun loadImage(): Bitmap? =
            withContext(ioDispatcher) {
                if (cacheFile.exists()) {
                    BitmapFactory.decodeFile(cacheFile.absolutePath)
                } else {
                    null
                }
            }

        override suspend fun clear() {
            withContext(ioDispatcher) {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
            }
        }

        private companion object {
            const val CACHE_FILE_NAME = "puzzle_image.png"
        }
    }
