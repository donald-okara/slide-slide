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
package ke.don.slideslide.di

import android.content.Context
import androidx.room.Room
import ke.don.slideslide.data.dao.PuzzleDao
import ke.don.slideslide.data.database.AppDatabase

/**
 * Simple manual DI for database-related components.
 */
object DatabaseModule {
    private var database: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase =
        database ?: synchronized(this) {
            val instance =
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "slideslide.db",
                    ).build()
            database = instance
            instance
        }

    fun providePuzzleDao(context: Context): PuzzleDao = provideDatabase(context).puzzleDao()
}
