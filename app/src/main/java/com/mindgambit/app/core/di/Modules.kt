package com.mindgambit.app.core.di

import android.content.Context
import com.mindgambit.app.data.database.MindGambitDatabase
import com.mindgambit.app.data.database.dao.*
import com.mindgambit.app.data.engine.StockfishEngine
import com.mindgambit.app.data.repository.*
import com.mindgambit.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ============================================================
// MindGambit — Hilt DI Modules
// ============================================================

// ── Database Module ──────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MindGambitDatabase =
        MindGambitDatabase.getInstance(context)

    @Provides
    fun provideGameDao(db: MindGambitDatabase): GameDao = db.gameDao()

    @Provides
    fun providePuzzleDao(db: MindGambitDatabase): PuzzleDao = db.puzzleDao()

    @Provides
    fun provideEloDao(db: MindGambitDatabase): EloDao = db.eloDao()

    @Provides
    fun provideOpeningProgressDao(db: MindGambitDatabase): OpeningProgressDao =
        db.openingProgressDao()
}

// ── Engine Module ─────────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideStockfishEngine(): StockfishEngine = StockfishEngine()
}

// ── Repository Module ─────────────────────────────────────────

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindPuzzleRepository(impl: PuzzleRepositoryImpl): PuzzleRepository

    @Binds
    @Singleton
    abstract fun bindEloRepository(impl: EloRepositoryImpl): EloRepository

    @Binds
    @Singleton
    abstract fun bindOpeningRepository(impl: OpeningRepositoryImpl): OpeningRepository
}
