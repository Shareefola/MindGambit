package com.mindgambit.app.data.repository

import android.content.Context
import com.mindgambit.app.data.database.dao.PuzzleDao
import com.mindgambit.app.data.database.entities.toEntity
import com.mindgambit.app.domain.model.Puzzle
import com.mindgambit.app.domain.model.PuzzleMotif
import com.mindgambit.app.domain.repository.PuzzleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// ============================================================
// MindGambit — PuzzleRepositoryImpl
// Includes spaced repetition: puzzles reviewed less recently
// surface first. Intervals grow on repeated success.
// ============================================================

class PuzzleRepositoryImpl @Inject constructor(
    private val puzzleDao: PuzzleDao,
    @ApplicationContext private val context: Context
) : PuzzleRepository {

    override suspend fun getNextPuzzle(): Puzzle? {
        // Seed puzzles if DB is empty
        if (puzzleDao.getTotalCount() == 0) {
            seedPuzzles()
        }
        return puzzleDao.getNextPuzzle()?.toDomain()
    }

    override suspend fun getPuzzleById(id: Long): Puzzle? =
        puzzleDao.getPuzzleById(id)?.toDomain()

    override suspend fun getRandomPuzzle(): Puzzle? =
        puzzleDao.getRandomPuzzle()?.toDomain()

    override suspend fun getRandomPuzzleByMotif(motif: PuzzleMotif): Puzzle? =
        puzzleDao.getRandomPuzzleByMotif(motif.name)?.toDomain()

    override suspend fun markSolved(puzzleId: Long) {
        val entity = puzzleDao.getPuzzleById(puzzleId) ?: return
        val newSolvedCount = entity.solvedCount + 1

        // Spaced repetition: interval grows with each successful solve
        // 1st solve → review in 1 day
        // 2nd solve → review in 3 days
        // 3rd solve → review in 7 days
        // 4th+ → review in 14 days
        val intervalDays = when (newSolvedCount) {
            1    -> 1L
            2    -> 3L
            3    -> 7L
            else -> 14L
        }
        val nextReview = Instant.now().plus(intervalDays, ChronoUnit.DAYS).toEpochMilli()

        puzzleDao.updatePuzzle(
            entity.copy(
                solvedCount  = newSolvedCount,
                lastSeenAt   = System.currentTimeMillis(),
                nextReviewAt = nextReview
            )
        )
    }

    override suspend fun markFailed(puzzleId: Long) {
        val entity = puzzleDao.getPuzzleById(puzzleId) ?: return
        // Failed: review again soon (1 hour)
        val nextReview = Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
        puzzleDao.updatePuzzle(
            entity.copy(
                failedCount  = entity.failedCount + 1,
                lastSeenAt   = System.currentTimeMillis(),
                nextReviewAt = nextReview
            )
        )
    }

    override fun getSolvedCount(): Flow<Int> =
        puzzleDao.getSolvedCount()

    // ── Seed starter puzzle set ───────────────────────────────

    private suspend fun seedPuzzles() {
        val puzzles = buildStarterPuzzles()
        puzzleDao.insertPuzzles(puzzles.map { it.toEntity() })
    }

    private fun buildStarterPuzzles(): List<Puzzle> = listOf(

        // ── Forks ─────────────────────────────────────────────
        Puzzle(
            id            = 1,
            fen           = "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4",
            solutionMoves = listOf("f3g5"),
            motif         = PuzzleMotif.FORK,
            difficulty    = 1,
            eloRating     = 900,
            themes        = listOf("fork", "knight")
        ),
        Puzzle(
            id            = 2,
            fen           = "5rk1/pp3ppp/2p5/8/3Pn3/1B3N2/PPP2PPP/R4RK1 w - - 0 14",
            solutionMoves = listOf("f3e5", "f8f1", "e5g6"),
            motif         = PuzzleMotif.FORK,
            difficulty    = 3,
            eloRating     = 1300,
            themes        = listOf("fork", "knight", "sacrifice")
        ),
        Puzzle(
            id            = 3,
            fen           = "r1b1kb1r/pp3ppp/2p1pn2/q7/3P4/2N1BN2/PPP2PPP/R2QK2R w KQkq - 0 9",
            solutionMoves = listOf("d1d3", "a5a2", "d3b5"),
            motif         = PuzzleMotif.FORK,
            difficulty    = 2,
            eloRating     = 1100,
            themes        = listOf("fork", "queen")
        ),

        // ── Pins ──────────────────────────────────────────────
        Puzzle(
            id            = 4,
            fen           = "r2qk2r/ppp2ppp/2nb1n2/3pp3/2B1P1b1/2NP1N2/PPP2PPP/R1BQK2R w KQkq - 0 7",
            solutionMoves = listOf("f3e5", "g4d1", "c4f7"),
            motif         = PuzzleMotif.PIN,
            difficulty    = 3,
            eloRating     = 1400,
            themes        = listOf("pin", "bishop", "discovered")
        ),
        Puzzle(
            id            = 5,
            fen           = "r1bqk2r/pppp1ppp/2n2n2/2b5/2B1P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 5 5",
            solutionMoves = listOf("c5f2"),
            motif         = PuzzleMotif.PIN,
            difficulty    = 2,
            eloRating     = 1000,
            themes        = listOf("pin", "bishop_sacrifice")
        ),

        // ── Skewers ───────────────────────────────────────────
        Puzzle(
            id            = 6,
            fen           = "4k3/8/8/3q4/8/8/8/R3K3 b Q - 0 1",
            solutionMoves = listOf("d5a5"),
            motif         = PuzzleMotif.SKEWER,
            difficulty    = 2,
            eloRating     = 1050,
            themes        = listOf("skewer", "queen", "endgame")
        ),
        Puzzle(
            id            = 7,
            fen           = "8/8/3k4/8/8/3K4/8/R6r w - - 0 1",
            solutionMoves = listOf("a1h1"),
            motif         = PuzzleMotif.SKEWER,
            difficulty    = 1,
            eloRating     = 850,
            themes        = listOf("skewer", "rook", "endgame")
        ),

        // ── Back Rank Mates ───────────────────────────────────
        Puzzle(
            id            = 8,
            fen           = "6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1",
            solutionMoves = listOf("d1d8"),
            motif         = PuzzleMotif.BACK_RANK,
            difficulty    = 1,
            eloRating     = 800,
            themes        = listOf("back_rank", "rook", "mate_in_1")
        ),
        Puzzle(
            id            = 9,
            fen           = "3r2k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1",
            solutionMoves = listOf("d1d8", "d8d8"),
            motif         = PuzzleMotif.BACK_RANK,
            difficulty    = 2,
            eloRating     = 1000,
            themes        = listOf("back_rank", "rook_trade")
        ),

        // ── Discovered Attacks ────────────────────────────────
        Puzzle(
            id            = 10,
            fen           = "r1b1k2r/ppp2ppp/2n1pn2/3p4/1bBP4/2N1PN2/PPP2PPP/R1BQK2R w KQkq d6 0 7",
            solutionMoves = listOf("e3d5"),
            motif         = PuzzleMotif.DISCOVERED,
            difficulty    = 2,
            eloRating     = 1150,
            themes        = listOf("discovered_attack", "knight")
        ),

        // ── Smothered Mate ────────────────────────────────────
        Puzzle(
            id            = 11,
            fen           = "6rk/6pp/8/8/8/8/8/5NRK w - - 0 1",
            solutionMoves = listOf("f1h2", "g8g1", "h2f1", "g1h1", "f1g3"),
            motif         = PuzzleMotif.SMOTHERED_MATE,
            difficulty    = 4,
            eloRating     = 1600,
            themes        = listOf("smothered_mate", "knight", "sacrifice")
        ),

        // ── Deflection ────────────────────────────────────────
        Puzzle(
            id            = 12,
            fen           = "r2q1rk1/pp2ppbp/2np1np1/2p5/4PP2/2NP1NP1/PPP3BP/R1BQ1RK1 b - - 0 9",
            solutionMoves = listOf("d6e4", "f3e5", "e4f2"),
            motif         = PuzzleMotif.DEFLECTION,
            difficulty    = 3,
            eloRating     = 1350,
            themes        = listOf("deflection", "sacrifice")
        ),

        // ── Endgame ───────────────────────────────────────────
        Puzzle(
            id            = 13,
            fen           = "8/5kp1/5p2/5P2/4K3/8/8/8 w - - 0 1",
            solutionMoves = listOf("e4d5", "f7e7", "d5e5"),
            motif         = PuzzleMotif.ENDGAME,
            difficulty    = 2,
            eloRating     = 1100,
            themes        = listOf("king_opposition", "endgame", "pawn")
        ),
        Puzzle(
            id            = 14,
            fen           = "8/8/8/3k4/8/8/3K4/3Q4 w - - 0 1",
            solutionMoves = listOf("d1d5"),
            motif         = PuzzleMotif.ENDGAME,
            difficulty    = 1,
            eloRating     = 750,
            themes        = listOf("queen_endgame", "check")
        ),

        // ── Opening Traps ─────────────────────────────────────
        Puzzle(
            id            = 15,
            fen           = "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/2N2N2/PPPP1PPP/R1BQK2R b KQkq - 5 4",
            solutionMoves = listOf("f6e4", "c3e4", "d7d5"),
            motif         = PuzzleMotif.OPENING,
            difficulty    = 2,
            eloRating     = 1050,
            themes        = listOf("italian_game", "trap", "center")
        ),

        // ── Double Check ──────────────────────────────────────
        Puzzle(
            id            = 16,
            fen           = "r1b1kb1r/pppp1ppp/2n5/4p3/2BPP3/5N2/PPP2PPP/RNBQK2R b KQkq d3 0 5",
            solutionMoves = listOf("c6d4", "f3d4", "d8h4"),
            motif         = PuzzleMotif.DOUBLE_CHECK,
            difficulty    = 3,
            eloRating     = 1250,
            themes        = listOf("double_check", "legal_trap")
        ),

        // ── General / Mixed ───────────────────────────────────
        Puzzle(
            id            = 17,
            fen           = "r3k2r/ppp2ppp/2n1bn2/3qp3/3P4/2N1BN2/PPP2PPP/R2QK2R w KQkq - 0 9",
            solutionMoves = listOf("d4e5", "d5d1", "a1d1", "c6e5"),
            motif         = PuzzleMotif.GENERAL,
            difficulty    = 3,
            eloRating     = 1300,
            themes        = listOf("combination", "exchange")
        ),
        Puzzle(
            id            = 18,
            fen           = "2r3k1/p4ppp/1p6/3Pp3/8/1BP5/P4PPP/R5K1 w - - 0 22",
            solutionMoves = listOf("d5d6", "c8c3", "b3c4"),
            motif         = PuzzleMotif.GENERAL,
            difficulty    = 4,
            eloRating     = 1500,
            themes        = listOf("passed_pawn", "activity")
        ),
        Puzzle(
            id            = 19,
            fen           = "4r1k1/1p3ppp/p1p5/3pP3/P2P1P2/2PB4/6PP/5RK1 b - - 0 25",
            solutionMoves = listOf("e8e5", "f4e5", "d5d4"),
            motif         = PuzzleMotif.DEFLECTION,
            difficulty    = 4,
            eloRating     = 1550,
            themes        = listOf("deflection", "rook_sacrifice")
        ),
        Puzzle(
            id            = 20,
            fen           = "r4rk1/ppp2ppp/2n1b3/3qp3/3P4/2PB1N2/PP3PPP/R2Q1RK1 w - - 0 13",
            solutionMoves = listOf("d3h7", "g8h7", "f3g5", "h7g8", "d1h5"),
            motif         = PuzzleMotif.GENERAL,
            difficulty    = 5,
            eloRating     = 1750,
            themes        = listOf("bishop_sacrifice", "attack", "brilliancy")
        )
    )
}
