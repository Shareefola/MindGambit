package com.mindgambit.app.domain.model

import java.time.Instant

// ============================================================
// MindGambit — Game, Puzzle, Opening Domain Models
// ============================================================

// ── Game ──────────────────────────────────────────────────────

enum class GameResult { WHITE_WIN, BLACK_WIN, DRAW, IN_PROGRESS, ABANDONED }
enum class GameMode   { RAPID, BLITZ, ANALYSIS, PUZZLE }

data class Game(
    val id:           Long         = 0,
    val mode:         GameMode,
    val timeControl:  TimeControl,
    val position:     ChessPosition,
    val moveHistory:  List<Move>   = emptyList(),
    val pgn:          String       = "",
    val result:       GameResult   = GameResult.IN_PROGRESS,
    val playerColor:  PieceColor   = PieceColor.WHITE,
    val playerRating: Int          = 1200,
    val opponentRating: Int        = 1200,
    val eloDelta:     Int          = 0,
    val accuracy:     Float        = 0f,
    val createdAt:    Instant      = Instant.now()
)

data class TimeControl(
    val initialSeconds: Int,
    val incrementSeconds: Int = 0
) {
    val displayName: String get() = when {
        initialSeconds < 180  -> "Bullet"
        initialSeconds < 600  -> "Blitz"
        initialSeconds < 1800 -> "Rapid"
        else                  -> "Classical"
    }

    override fun toString() = "${initialSeconds / 60}+$incrementSeconds"

    companion object {
        val RAPID_10  = TimeControl(600)
        val BLITZ_5   = TimeControl(300)
        val BLITZ_3_2 = TimeControl(180, 2)
    }
}

// ── Puzzle ─────────────────────────────────────────────────────

enum class PuzzleMotif(val displayName: String) {
    FORK          ("Fork"),
    PIN           ("Pin"),
    SKEWER        ("Skewer"),
    DISCOVERED    ("Discovered Attack"),
    DOUBLE_CHECK  ("Double Check"),
    BACK_RANK     ("Back Rank Mate"),
    SMOTHERED_MATE("Smothered Mate"),
    DEFLECTION    ("Deflection"),
    DECOY         ("Decoy"),
    ZUGZWANG      ("Zugzwang"),
    ENDGAME       ("Endgame"),
    OPENING       ("Opening Trap"),
    GENERAL       ("General Tactics"),
}

data class Puzzle(
    val id:             Long,
    val fen:            String,
    val solutionMoves:  List<String>,   // UCI move strings
    val motif:          PuzzleMotif,
    val difficulty:     Int,            // 1–5 stars
    val eloRating:      Int,            // puzzle difficulty rating
    val themes:         List<String>    = emptyList(),
    val solvedCount:    Int             = 0,
    val failedCount:    Int             = 0,
    val lastSeenAt:     Instant?        = null,
    val nextReviewAt:   Instant?        = null  // spaced repetition
) {
    val position: ChessPosition get() = FenParser.fromFen(fen)
    val successRate: Float get() =
        if (solvedCount + failedCount == 0) 0f
        else solvedCount.toFloat() / (solvedCount + failedCount)
}

// ── Opening ────────────────────────────────────────────────────

enum class OpeningId { LONDON, JOBAVA_LONDON, PIRC }

data class Opening(
    val id:          OpeningId,
    val name:        String,
    val tag:         String,
    val description: String,
    val eco:         String,           // ECO code e.g. "D02"
    val moves:       String,           // PGN move sequence
    val lessons:     List<OpeningLesson>
)

data class OpeningLesson(
    val id:          String,
    val title:       String,
    val description: String,
    val fen:         String,
    val moves:       List<String>,    // UCI moves to demonstrate
    val isUnlocked:  Boolean = false,
    val isCompleted: Boolean = false
)

data class OpeningProgress(
    val openingId:        OpeningId,
    val completedLessons: Set<String> = emptySet(),
    val totalLessons:     Int
) {
    val progressFraction: Float get() =
        if (totalLessons == 0) 0f
        else completedLessons.size.toFloat() / totalLessons

    val progressPercent: Int get() = (progressFraction * 100).toInt()
}
