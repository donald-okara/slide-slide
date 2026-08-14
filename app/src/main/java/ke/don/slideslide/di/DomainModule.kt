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

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ke.don.slideslide.domain.manager.PuzzleManager
import ke.don.slideslide.domain.manager.PuzzleManagerImpl
import ke.don.slideslide.domain.usecase.IsGameOverUseCase
import ke.don.slideslide.domain.usecase.IsGameOverUseCaseImpl
import ke.don.slideslide.domain.usecase.ShuffleUseCase
import ke.don.slideslide.domain.usecase.ShuffleUseCaseImpl
import ke.don.slideslide.domain.usecase.SolveUseCase
import ke.don.slideslide.domain.usecase.SolveUseCaseImpl
import ke.don.slideslide.domain.usecase.ValidateMoveUseCase
import ke.don.slideslide.domain.usecase.ValidateMoveUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds
    @Singleton
    abstract fun bindIsGameOverUseCase(impl: IsGameOverUseCaseImpl): IsGameOverUseCase

    @Binds
    @Singleton
    abstract fun bindShuffleUseCase(impl: ShuffleUseCaseImpl): ShuffleUseCase

    @Binds
    @Singleton
    abstract fun bindSolveUseCase(impl: SolveUseCaseImpl): SolveUseCase

    @Binds
    @Singleton
    abstract fun bindValidateMoveUseCase(impl: ValidateMoveUseCaseImpl): ValidateMoveUseCase

    @Binds
    @Singleton
    abstract fun bindPuzzleManager(impl: PuzzleManagerImpl): PuzzleManager
}
