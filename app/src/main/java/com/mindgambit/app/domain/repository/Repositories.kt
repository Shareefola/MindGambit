package com.mindgambit.app.domain.repository

import com.mindgambit.app.domain.model.*
import kotlinx.coroutines.flow.Flow

// ============================================================
// MindGambit — Repository Interfaces (Domain layer)
// Implementations live in data layer.
// Domain interfaces must NOT reference data layer types.
// ============================================================

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>
    suspend fun getGameById(id: Long): Game?
    suspend fun getRecentGames(limit: Int = 10): List<Game>
    suspend fun saveGame(game: Game): Long
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(id: Long)
    fun getGameCount(): Flow<Int>
    fun getAverageAccuracy(): Flow<Float?>
}

interface PuzzleRepository {
    suspend fun getNextPuzzle(): Puzzle?
    suspend fun getPuzzleById(id: Long): Puzzle?
    suspend fun getRandomPuzzle(): Puzzle?
    suspend fun getRandomPuzzleByMotif(motif: PuzzleMotif): Puzzle?
    suspend fun markSolved(puzzleId: Long)
    suspend fun markFailed(puzzleId: Long)
    fun getSolvedCount(): Flow<Int>
}

// Domain-safe rating history entry (no Room annotations)
data class RatingHistoryEntry(
    val mode:       RatingMode,
    val rating:     Int,
    val delta:      Int,
    val recordedAt: Long = System.currentTimeMillis()
)

interface EloRepository {
    suspend fun getRating(mode: RatingMode): EloRating
    fun getHistory(mode: RatingMode): Flow<List<RatingHistoryEntry>>
    fun getRecentHistory(): Flow<List<RatingHistoryEntry>>
    suspend fun updateRating(mode: RatingMode, newRating: Int, delta: Int)
    suspend fun getAllRatings(): Map<RatingMode, EloRating>
}

interface OpeningRepository {
    fun getAllOpenings(): List<Opening>
    fun getProgress(openingId: OpeningId): Flow<OpeningProgress>
    suspend fun markLessonComplete(openingId: OpeningId, lessonId: String)
    suspend fun getOpeningById(id: OpeningId): Opening?
}
