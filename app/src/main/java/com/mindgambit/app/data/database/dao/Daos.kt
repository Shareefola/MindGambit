package com.mindgambit.app.data.database.dao

import androidx.room.*
import com.mindgambit.app.data.database.entities.*
import kotlinx.coroutines.flow.Flow

// ============================================================
// MindGambit — Room DAOs
// ============================================================

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getGameById(id: Long): GameEntity?

    @Query("SELECT * FROM games ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentGames(limit: Int = 10): List<GameEntity>

    @Query("SELECT COUNT(*) FROM games")
    fun getGameCount(): Flow<Int>

    @Query("SELECT AVG(accuracy) FROM games WHERE result != 'IN_PROGRESS'")
    fun getAverageAccuracy(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: Long)
}

// ────────────────────────────────────────────────────────────

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles WHERE id = :id")
    suspend fun getPuzzleById(id: Long): PuzzleEntity?

    // Spaced repetition: get puzzles due for review (nextReviewAt < now OR never seen)
    @Query("""
        SELECT * FROM puzzles 
        WHERE nextReviewAt IS NULL OR nextReviewAt <= :now
        ORDER BY 
            CASE WHEN lastSeenAt IS NULL THEN 0 ELSE 1 END,
            nextReviewAt ASC
        LIMIT 1
    """)
    suspend fun getNextPuzzle(now: Long = System.currentTimeMillis()): PuzzleEntity?

    @Query("SELECT * FROM puzzles WHERE motif = :motif ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPuzzleByMotif(motif: String): PuzzleEntity?

    @Query("SELECT COUNT(*) FROM puzzles WHERE solvedCount > 0")
    fun getSolvedCount(): Flow<Int>

    @Query("SELECT * FROM puzzles ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPuzzle(): PuzzleEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPuzzles(puzzles: List<PuzzleEntity>)

    @Update
    suspend fun updatePuzzle(puzzle: PuzzleEntity)

    @Query("SELECT COUNT(*) FROM puzzles")
    suspend fun getTotalCount(): Int
}

// ────────────────────────────────────────────────────────────

@Dao
interface EloDao {
    @Query("SELECT * FROM elo_history WHERE mode = :mode ORDER BY recordedAt DESC")
    fun getHistoryForMode(mode: String): Flow<List<EloHistoryEntity>>

    @Query("SELECT * FROM elo_history WHERE mode = :mode ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestForMode(mode: String): EloHistoryEntity?

    @Query("SELECT * FROM elo_history ORDER BY recordedAt DESC LIMIT 30")
    fun getRecentHistory(): Flow<List<EloHistoryEntity>>

    @Insert
    suspend fun insertRating(entity: EloHistoryEntity)

    @Query("DELETE FROM elo_history WHERE mode = :mode")
    suspend fun clearHistoryForMode(mode: String)
}

// ────────────────────────────────────────────────────────────

@Dao
interface OpeningProgressDao {
    @Query("SELECT * FROM opening_progress")
    fun getAllProgress(): Flow<List<OpeningProgressEntity>>

    @Query("SELECT * FROM opening_progress WHERE openingId = :openingId")
    suspend fun getProgress(openingId: String): OpeningProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: OpeningProgressEntity)
}
