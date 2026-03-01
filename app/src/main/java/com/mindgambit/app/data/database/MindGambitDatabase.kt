package com.mindgambit.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindgambit.app.data.database.dao.*
import com.mindgambit.app.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ============================================================
// MindGambit — Room Database
// Version 1 — includes all tables
// ============================================================

@Database(
    entities = [
        GameEntity::class,
        PuzzleEntity::class,
        EloHistoryEntity::class,
        OpeningProgressEntity::class,
    ],
    version  = 1,
    exportSchema = false
)
abstract class MindGambitDatabase : RoomDatabase() {

    abstract fun gameDao():            GameDao
    abstract fun puzzleDao():          PuzzleDao
    abstract fun eloDao():             EloDao
    abstract fun openingProgressDao(): OpeningProgressDao

    companion object {
        const val DATABASE_NAME = "mindgambit.db"

        @Volatile private var INSTANCE: MindGambitDatabase? = null

        fun getInstance(context: Context): MindGambitDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MindGambitDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(SeedCallback())
                    .fallbackToDestructiveMigration()   // safe for early development
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    // Pre-populate DB with starter puzzles and Elo ratings on first launch
    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedStartingElo(database.eloDao())
                    seedOpeningProgress(database.openingProgressDao())
                    // Puzzles are seeded from assets in PuzzleRepository
                }
            }
        }

        private suspend fun seedStartingElo(dao: EloDao) {
            val startRating = 800
            listOf("RAPID", "BLITZ", "TACTICAL", "STRATEGIC").forEach { mode ->
                dao.insertRating(
                    EloHistoryEntity(
                        mode      = mode,
                        rating    = startRating,
                        delta     = 0,
                        recordedAt= System.currentTimeMillis()
                    )
                )
            }
        }

        private suspend fun seedOpeningProgress(dao: OpeningProgressDao) {
            listOf(
                OpeningProgressEntity("LONDON",       totalLessons = 8),
                OpeningProgressEntity("JOBAVA_LONDON", totalLessons = 6),
                OpeningProgressEntity("PIRC",          totalLessons = 7),
            ).forEach { dao.upsertProgress(it) }
        }
    }
}
