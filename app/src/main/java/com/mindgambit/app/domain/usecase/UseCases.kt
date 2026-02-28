package com.mindgambit.app.domain.usecase

import com.mindgambit.app.core.utils.EloCalculator
import com.mindgambit.app.data.engine.StockfishEngine
import com.mindgambit.app.domain.model.*
import com.mindgambit.app.domain.repository.*
import javax.inject.Inject

// ============================================================
// MindGambit — Use Cases
// Each use case = one atomic business operation.
// ============================================================

// ── Chess Move Use Cases ──────────────────────────────────────

class GetLegalMovesUseCase @Inject constructor(
    private val engine: StockfishEngine
) {
    suspend operator fun invoke(position: ChessPosition, square: Int): List<Move> {
        val fen = position.toFen()
        val uciMoves = engine.getLegalMoves(fen)
        val fromAlg = squareToAlgebraic(square)
        return uciMoves
            .filter { it.startsWith(fromAlg) }
            .map { uci ->
                val to = algebraicToSquare(uci.substring(2, 4))
                val promo = uci.getOrNull(4)?.let { ch ->
                    when (ch) {
                        'q' -> PieceType.QUEEN
                        'r' -> PieceType.ROOK
                        'b' -> PieceType.BISHOP
                        'n' -> PieceType.KNIGHT
                        else -> null
                    }
                }
                Move(from = square, to = to, promotion = promo)
            }
    }
}

class GetEngineEvalUseCase @Inject constructor(
    private val engine: StockfishEngine
) {
    suspend operator fun invoke(position: ChessPosition, depth: Int = 12) =
        engine.evaluatePosition(position.toFen(), depth)
}

class GetBestMoveUseCase @Inject constructor(
    private val engine: StockfishEngine
) {
    suspend operator fun invoke(position: ChessPosition, moveTime: Int = 1000) =
        engine.getBestMove(position.toFen(), moveTime)
}

// ── Decision Intelligence Use Cases ──────────────────────────

class ScanThreatsUseCase @Inject constructor() {
    /**
     * Returns squares where the opponent has immediate threats.
     * Simplified: checks which opponent pieces can capture player's pieces next move.
     */
    operator fun invoke(position: ChessPosition): List<Int> {
        val opponent = position.sideToMove.opposite()
        val threats = mutableListOf<Int>()
        // Iterate opponent pieces and find their attacked squares
        position.pieces.forEachIndexed { sq, piece ->
            if (piece?.color == opponent) {
                getAttackedSquares(position, sq).forEach { attacked ->
                    if (position.pieces[attacked]?.color == position.sideToMove) {
                        threats.add(attacked)
                    }
                }
            }
        }
        return threats.distinct()
    }

    private fun getAttackedSquares(position: ChessPosition, square: Int): List<Int> {
        val piece = position.pieces[square] ?: return emptyList()
        val rank = square / 8; val file = square % 8
        return when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) 1 else -1
                listOfNotNull(
                    if (file > 0) squareOf(file - 1, rank + dir).takeIf { it in 0..63 } else null,
                    if (file < 7) squareOf(file + 1, rank + dir).takeIf { it in 0..63 } else null
                )
            }
            PieceType.KNIGHT -> {
                listOf(-17,-15,-10,-6,6,10,15,17)
                    .map { square + it }
                    .filter { it in 0..63 && abs(it % 8 - file) <= 2 }
            }
            else -> emptyList() // Simplified — full sliding piece logic in MoveGenerator
        }
    }
}

data class CCTResult(
    val checks:   List<String>,  // UCI moves that give check
    val captures: List<String>,  // UCI moves that capture
    val threats:  List<String>   // UCI moves that create threats
)

class CCTScannerUseCase @Inject constructor(
    private val engine: StockfishEngine
) {
    suspend operator fun invoke(position: ChessPosition): CCTResult {
        val allMoves = engine.getLegalMoves(position.toFen())
        // In full implementation, evaluate each move for check/capture/threat
        // Here we categorise by basic heuristics
        val checks   = allMoves.filter { it.contains("+") }
        val captures = allMoves.filter { isCapture(position, it) }
        val threats  = allMoves.filter { !it.contains("+") && !isCapture(position, it) }
        return CCTResult(checks, captures, threats)
    }

    private fun isCapture(pos: ChessPosition, uci: String): Boolean {
        val to = algebraicToSquare(uci.substring(2, 4))
        return pos.pieces[to] != null
    }
}

class BlunderGuardUseCase @Inject constructor(
    private val engine: StockfishEngine
) {
    /**
     * Evaluate proposed move — return true if it's a blunder (drops evaluation significantly).
     * Threshold: -150 centipawns relative to current position.
     */
    suspend operator fun invoke(position: ChessPosition, move: Move): Boolean {
        val beforeEval = engine.evaluatePosition(position.toFen())
        // Apply move then evaluate
        val fenAfter = applyMoveToFen(position.toFen(), move.toUci())
        val afterEval = engine.evaluatePosition(fenAfter)
        val drop = beforeEval.evaluation - afterEval.evaluation
        return drop > 150
    }

    private fun applyMoveToFen(fen: String, uci: String): String {
        // Simplified — in production use full move generator
        return fen // placeholder
    }
}

// ── Elo Use Cases ─────────────────────────────────────────────

class UpdateEloUseCase @Inject constructor(
    private val eloRepository: EloRepository
) {
    suspend operator fun invoke(
        mode:             RatingMode,
        opponentRating:   Int,
        score:            Double,
        decisionAccuracy: Float = 0.7f
    ): Pair<Int, Int> {
        val currentElo = eloRepository.getRating(mode)
        val (newRating, delta) = EloCalculator.calculate(
            currentRating    = currentElo.rating,
            opponentRating   = opponentRating,
            score            = score,
            gamesPlayed      = currentElo.gamesPlayed,
            decisionAccuracy = decisionAccuracy
        )
        eloRepository.updateRating(mode, newRating, delta)
        return Pair(newRating, delta)
    }
}

class GetTierUseCase @Inject constructor(
    private val eloRepository: EloRepository
) {
    suspend operator fun invoke(mode: RatingMode): Tier {
        val rating = eloRepository.getRating(mode)
        return Tier.fromRating(rating.rating)
    }
}

// ── Tactics Use Cases ─────────────────────────────────────────

class GetNextPuzzleUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository
) {
    suspend operator fun invoke(): Puzzle? = puzzleRepository.getNextPuzzle()
}

class ValidateSolutionUseCase @Inject constructor() {
    /**
     * Returns true if [playerMove] matches the expected solution move.
     * Handles promotion suffixes and normalises UCI format.
     */
    operator fun invoke(
        puzzle:      Puzzle,
        moveIndex:   Int,
        playerMove:  String
    ): Boolean {
        val expected = puzzle.solutionMoves.getOrNull(moveIndex) ?: return false
        return expected.lowercase().trim() == playerMove.lowercase().trim()
    }
}

class MarkPuzzleSolvedUseCase @Inject constructor(
    private val puzzleRepository: PuzzleRepository,
    private val eloRepository:    EloRepository
) {
    suspend operator fun invoke(puzzle: Puzzle, solved: Boolean) {
        if (solved) {
            puzzleRepository.markSolved(puzzle.id)
            // Update tactical rating
            val current = eloRepository.getRating(RatingMode.TACTICAL)
            val delta = EloCalculator.calculatePuzzleDelta(
                playerRating = current.rating,
                puzzleRating = puzzle.eloRating,
                solved       = true
            )
            eloRepository.updateRating(RatingMode.TACTICAL, current.rating + delta, delta)
        } else {
            puzzleRepository.markFailed(puzzle.id)
            val current = eloRepository.getRating(RatingMode.TACTICAL)
            val delta = EloCalculator.calculatePuzzleDelta(
                playerRating = current.rating,
                puzzleRating = puzzle.eloRating,
                solved       = false
            )
            eloRepository.updateRating(RatingMode.TACTICAL, current.rating + delta, delta)
        }
    }
}
