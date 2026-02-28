package com.mindgambit.app.core.utils

import kotlin.math.*

// ============================================================
// MindGambit — Elo / Glicko-2 Calculator
//
// Uses a simplified Glicko-style formula with:
//  • Base K-factor adjusted by games played (higher early on)
//  • Decision quality multiplier (rewards good thinking process)
//  • Confidence weighting (rating deviation equivalent)
// ============================================================

object EloCalculator {

    // ── Constants ─────────────────────────────────────────────

    private const val K_BASE       = 32.0   // max rating change per game (new players)
    private const val K_EXPERIENCED = 16.0  // K-factor after 30+ games
    private const val K_THRESHOLD  = 30     // games before K reduction
    private const val DECISION_BONUS_MAX = 0.25  // up to 25% bonus for great thinking

    // ── Main calculation ──────────────────────────────────────

    /**
     * Calculate new rating after a game.
     *
     * @param currentRating     Player's current Elo
     * @param opponentRating    Opponent's Elo
     * @param score             1.0 = win, 0.5 = draw, 0.0 = loss
     * @param gamesPlayed       Total games played (affects K-factor)
     * @param decisionAccuracy  0.0–1.0 quality of thinking process
     * @return Pair of (newRating, delta)
     */
    fun calculate(
        currentRating:    Int,
        opponentRating:   Int,
        score:            Double,   // 1.0 win, 0.5 draw, 0.0 loss
        gamesPlayed:      Int,
        decisionAccuracy: Float = 0.7f
    ): Pair<Int, Int> {
        val k = kFactor(gamesPlayed)
        val expected = expectedScore(currentRating, opponentRating)
        val base = k * (score - expected)

        // Decision quality multiplier: great thinking = bonus points, poor = penalty
        val decisionMult = 1.0 + (decisionAccuracy - 0.5) * DECISION_BONUS_MAX * 2
        val delta = (base * decisionMult).roundToInt()

        val newRating = (currentRating + delta).coerceIn(100, 3000)
        return Pair(newRating, delta)
    }

    /**
     * Expected score using standard Elo formula.
     */
    fun expectedScore(ratingA: Int, ratingB: Int): Double {
        return 1.0 / (1.0 + 10.0.pow((ratingB - ratingA) / 400.0))
    }

    /**
     * K-factor: higher for new players, stabilises over time.
     */
    fun kFactor(gamesPlayed: Int): Double {
        return if (gamesPlayed < K_THRESHOLD) K_BASE else K_EXPERIENCED
    }

    /**
     * Estimate rating change for display before confirming a game result.
     */
    fun estimateDelta(
        currentRating:  Int,
        opponentRating: Int,
        won:            Boolean,
        gamesPlayed:    Int
    ): Int {
        val score = if (won) 1.0 else 0.0
        return calculate(currentRating, opponentRating, score, gamesPlayed).second
    }

    /**
     * Puzzle rating update: simpler formula based on success/failure.
     * Puzzle has its own rating; player gains/loses vs puzzle rating.
     */
    fun calculatePuzzleDelta(
        playerRating:  Int,
        puzzleRating:  Int,
        solved:        Boolean,
        attemptCount:  Int = 1
    ): Int {
        val k = if (attemptCount == 1) 20.0 else 10.0
        val score = if (solved) 1.0 else 0.0
        val expected = expectedScore(playerRating, puzzleRating)
        return (k * (score - expected)).roundToInt()
    }

    /**
     * Performance rating: what Elo would a player be if they always scored
     * this way against these opponents? Used for session summaries.
     */
    fun performanceRating(results: List<Pair<Int, Double>>): Int {
        if (results.isEmpty()) return 1200
        val avgOpponent = results.map { it.first }.average()
        val score = results.sumOf { it.second } / results.size
        val dp = when {
            score >= 1.0  ->  800.0
            score <= 0.0  -> -800.0
            else          -> -400.0 * log10(1.0 / score - 1.0)
        }
        return (avgOpponent + dp).roundToInt().coerceIn(100, 3000)
    }

    private fun Double.roundToInt() = kotlin.math.roundToInt(this)
}

private fun kotlin.math.roundToInt(d: Double): Int = Math.round(d).toInt()
