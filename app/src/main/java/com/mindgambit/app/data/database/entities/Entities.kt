package com.mindgambit.app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mindgambit.app.domain.model.*

// ============================================================
// MindGambit — Room Database Entities
// ============================================================

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id:              Long   = 0,
    val mode:            String,
    val initialSeconds:  Int,
    val incrementSeconds:Int,
    val pgn:             String,
    val result:          String,
    val playerColor:     String,
    val playerRating:    Int,
    val opponentRating:  Int,
    val eloDelta:        Int,
    val accuracy:        Float,
    val createdAt:       Long   = System.currentTimeMillis()
) {
    fun toDomain(): Game = Game(
        id             = id,
        mode           = GameMode.valueOf(mode),
        timeControl    = TimeControl(initialSeconds, incrementSeconds),
        position       = ChessPosition.STARTING,
        pgn            = pgn,
        result         = GameResult.valueOf(result),
        playerColor    = PieceColor.valueOf(playerColor),
        playerRating   = playerRating,
        opponentRating = opponentRating,
        eloDelta       = eloDelta,
        accuracy       = accuracy,
    )
}

fun Game.toEntity() = GameEntity(
    id             = id,
    mode           = mode.name,
    initialSeconds = timeControl.initialSeconds,
    incrementSeconds = timeControl.incrementSeconds,
    pgn            = pgn,
    result         = result.name,
    playerColor    = playerColor.name,
    playerRating   = playerRating,
    opponentRating = opponentRating,
    eloDelta       = eloDelta,
    accuracy       = accuracy
)

// ────────────────────────────────────────────────────────────

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val id:        Long,
    val fen:                   String,
    val solutionMoves:         String,   // comma-separated UCI moves
    val motif:                 String,
    val difficulty:            Int,
    val eloRating:             Int,
    val themes:                String,   // comma-separated
    val solvedCount:           Int       = 0,
    val failedCount:           Int       = 0,
    val lastSeenAt:            Long?     = null,
    val nextReviewAt:          Long?     = null
) {
    fun toDomain() = Puzzle(
        id            = id,
        fen           = fen,
        solutionMoves = solutionMoves.split(",").filter { it.isNotBlank() },
        motif         = PuzzleMotif.valueOf(motif),
        difficulty    = difficulty,
        eloRating     = eloRating,
        themes        = themes.split(",").filter { it.isNotBlank() },
        solvedCount   = solvedCount,
        failedCount   = failedCount,
    )
}

fun Puzzle.toEntity() = PuzzleEntity(
    id            = id,
    fen           = fen,
    solutionMoves = solutionMoves.joinToString(","),
    motif         = motif.name,
    difficulty    = difficulty,
    eloRating     = eloRating,
    themes        = themes.joinToString(","),
    solvedCount   = solvedCount,
    failedCount   = failedCount,
)

// ────────────────────────────────────────────────────────────

@Entity(tableName = "elo_history")
data class EloHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id:        Long   = 0,
    val mode:      String,
    val rating:    Int,
    val delta:     Int,
    val confidence:Double = 1.0,
    val recordedAt:Long   = System.currentTimeMillis()
)

// ────────────────────────────────────────────────────────────

@Entity(tableName = "opening_progress")
data class OpeningProgressEntity(
    @PrimaryKey val openingId:      String,
    val completedLessons:           String = "",   // comma-separated lesson IDs
    val totalLessons:               Int
) {
    fun toDomain(id: OpeningId) = OpeningProgress(
        openingId        = id,
        completedLessons = completedLessons.split(",").filter { it.isNotBlank() }.toSet(),
        totalLessons     = totalLessons
    )
}
