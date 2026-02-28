package com.mindgambit.app.core.utils

import org.junit.Assert.*
import org.junit.Test

// ============================================================
// MindGambit — EloCalculator Unit Tests
// ============================================================

class EloCalculatorTest {

    // ── expectedScore ─────────────────────────────────────────

    @Test
    fun `expectedScore returns 0_5 for equal ratings`() {
        val score = EloCalculator.expectedScore(1200, 1200)
        assertEquals(0.5, score, 0.001)
    }

    @Test
    fun `expectedScore is higher when player rating is higher`() {
        val score = EloCalculator.expectedScore(1400, 1200)
        assertTrue("Expected score should be > 0.5 for higher rated player", score > 0.5)
    }

    @Test
    fun `expectedScore is lower when player rating is lower`() {
        val score = EloCalculator.expectedScore(1000, 1200)
        assertTrue("Expected score should be < 0.5 for lower rated player", score < 0.5)
    }

    @Test
    fun `expectedScore is between 0 and 1`() {
        listOf(
            Pair(400, 2000),
            Pair(2000, 400),
            Pair(1200, 1200),
            Pair(800, 1600)
        ).forEach { (a, b) ->
            val score = EloCalculator.expectedScore(a, b)
            assertTrue("Score should be > 0", score > 0.0)
            assertTrue("Score should be < 1", score < 1.0)
        }
    }

    // ── kFactor ───────────────────────────────────────────────

    @Test
    fun `kFactor is 32 for new player`() {
        assertEquals(32.0, EloCalculator.kFactor(0), 0.001)
        assertEquals(32.0, EloCalculator.kFactor(10), 0.001)
        assertEquals(32.0, EloCalculator.kFactor(29), 0.001)
    }

    @Test
    fun `kFactor is 16 for experienced player`() {
        assertEquals(16.0, EloCalculator.kFactor(30), 0.001)
        assertEquals(16.0, EloCalculator.kFactor(100), 0.001)
    }

    // ── calculate ─────────────────────────────────────────────

    @Test
    fun `win against equal opponent increases rating`() {
        val (newRating, delta) = EloCalculator.calculate(
            currentRating    = 1200,
            opponentRating   = 1200,
            score            = 1.0,
            gamesPlayed      = 50,
            decisionAccuracy = 0.7f
        )
        assertTrue("Rating should increase after win", newRating > 1200)
        assertTrue("Delta should be positive", delta > 0)
    }

    @Test
    fun `loss against equal opponent decreases rating`() {
        val (newRating, delta) = EloCalculator.calculate(
            currentRating    = 1200,
            opponentRating   = 1200,
            score            = 0.0,
            gamesPlayed      = 50,
            decisionAccuracy = 0.7f
        )
        assertTrue("Rating should decrease after loss", newRating < 1200)
        assertTrue("Delta should be negative", delta < 0)
    }

    @Test
    fun `draw against equal opponent keeps rating roughly stable`() {
        val (newRating, _) = EloCalculator.calculate(
            currentRating    = 1200,
            opponentRating   = 1200,
            score            = 0.5,
            gamesPlayed      = 50,
            decisionAccuracy = 0.7f
        )
        assertEquals(1200, newRating)
    }

    @Test
    fun `win against stronger opponent increases rating more`() {
        val (_, deltaVsStrong) = EloCalculator.calculate(
            currentRating  = 1200,
            opponentRating = 1600,
            score          = 1.0,
            gamesPlayed    = 50
        )
        val (_, deltaVsEqual) = EloCalculator.calculate(
            currentRating  = 1200,
            opponentRating = 1200,
            score          = 1.0,
            gamesPlayed    = 50
        )
        assertTrue("Beating a stronger player should give more points", deltaVsStrong > deltaVsEqual)
    }

    @Test
    fun `high decision accuracy gives bonus points on win`() {
        val (_, deltaHigh) = EloCalculator.calculate(
            currentRating    = 1200,
            opponentRating   = 1200,
            score            = 1.0,
            gamesPlayed      = 50,
            decisionAccuracy = 1.0f
        )
        val (_, deltaLow) = EloCalculator.calculate(
            currentRating    = 1200,
            opponentRating   = 1200,
            score            = 1.0,
            gamesPlayed      = 50,
            decisionAccuracy = 0.0f
        )
        assertTrue("High accuracy should yield more points than low accuracy", deltaHigh > deltaLow)
    }

    @Test
    fun `rating stays within bounds`() {
        val (newRating, _) = EloCalculator.calculate(
            currentRating  = 100,
            opponentRating = 3000,
            score          = 0.0,
            gamesPlayed    = 200
        )
        assertTrue("Rating should not drop below 100", newRating >= 100)
    }

    // ── calculatePuzzleDelta ──────────────────────────────────

    @Test
    fun `solving a puzzle increases tactical rating`() {
        val delta = EloCalculator.calculatePuzzleDelta(
            playerRating = 1200,
            puzzleRating = 1200,
            solved       = true
        )
        assertTrue("Solving should give positive delta", delta > 0)
    }

    @Test
    fun `failing a puzzle decreases tactical rating`() {
        val delta = EloCalculator.calculatePuzzleDelta(
            playerRating = 1200,
            puzzleRating = 1200,
            solved       = false
        )
        assertTrue("Failing should give negative delta", delta < 0)
    }

    @Test
    fun `solving easy puzzle gives small gain`() {
        val deltaEasy = EloCalculator.calculatePuzzleDelta(
            playerRating = 1500,
            puzzleRating = 800,
            solved       = true
        )
        val deltaHard = EloCalculator.calculatePuzzleDelta(
            playerRating = 1500,
            puzzleRating = 2000,
            solved       = true
        )
        assertTrue("Solving a hard puzzle should give more points than an easy one", deltaHard > deltaEasy)
    }

    // ── performanceRating ─────────────────────────────────────

    @Test
    fun `performance rating with all wins is high`() {
        val perf = EloCalculator.performanceRating(
            listOf(Pair(1200, 1.0), Pair(1200, 1.0), Pair(1200, 1.0))
        )
        assertTrue("Perfect score should yield high performance rating", perf > 1200)
    }

    @Test
    fun `performance rating with all losses is low`() {
        val perf = EloCalculator.performanceRating(
            listOf(Pair(1200, 0.0), Pair(1200, 0.0), Pair(1200, 0.0))
        )
        assertTrue("Zero score should yield low performance rating", perf < 1200)
    }

    @Test
    fun `empty results returns default 1200`() {
        val perf = EloCalculator.performanceRating(emptyList())
        assertEquals(1200, perf)
    }
}
