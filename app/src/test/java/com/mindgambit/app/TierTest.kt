package com.mindgambit.app.domain.model

import org.junit.Assert.*
import org.junit.Test

// ============================================================
// MindGambit — Tier Unit Tests
// ============================================================

class TierTest {

    @Test
    fun `fromRating returns NOVICE for 400`() {
        assertEquals(Tier.NOVICE, Tier.fromRating(400))
    }

    @Test
    fun `fromRating returns NOVICE for 799`() {
        assertEquals(Tier.NOVICE, Tier.fromRating(799))
    }

    @Test
    fun `fromRating returns BEGINNER for 800`() {
        assertEquals(Tier.BEGINNER, Tier.fromRating(800))
    }

    @Test
    fun `fromRating returns IMPROVING for 1100`() {
        assertEquals(Tier.IMPROVING, Tier.fromRating(1100))
    }

    @Test
    fun `fromRating returns IMPROVING for 1312`() {
        assertEquals(Tier.IMPROVING, Tier.fromRating(1312))
    }

    @Test
    fun `fromRating returns INTERMEDIATE for 1400`() {
        assertEquals(Tier.INTERMEDIATE, Tier.fromRating(1400))
    }

    @Test
    fun `fromRating returns ADVANCED for 1700`() {
        assertEquals(Tier.ADVANCED, Tier.fromRating(1700))
    }

    @Test
    fun `fromRating returns ELITE for 2000`() {
        assertEquals(Tier.ELITE, Tier.fromRating(2000))
    }

    @Test
    fun `fromRating returns ELITE for 2400`() {
        assertEquals(Tier.ELITE, Tier.fromRating(2400))
    }

    @Test
    fun `fromRating returns NOVICE for rating below minimum`() {
        assertEquals(Tier.NOVICE, Tier.fromRating(0))
        assertEquals(Tier.NOVICE, Tier.fromRating(100))
    }

    @Test
    fun `progressFraction is 0 at tier start`() {
        val tier     = Tier.IMPROVING
        val fraction = tier.progressFraction(tier.minRating)
        assertEquals(0f, fraction, 0.001f)
    }

    @Test
    fun `progressFraction is 1 at tier max`() {
        val tier     = Tier.IMPROVING
        val fraction = tier.progressFraction(tier.maxRating)
        assertEquals(1f, fraction, 0.001f)
    }

    @Test
    fun `progressFraction is 0_5 at midpoint`() {
        val tier    = Tier.IMPROVING
        val mid     = (tier.minRating + tier.maxRating) / 2
        val fraction = tier.progressFraction(mid)
        assertEquals(0.5f, fraction, 0.01f)
    }

    @Test
    fun `progressFraction clamps below zero`() {
        val tier     = Tier.IMPROVING
        val fraction = tier.progressFraction(tier.minRating - 100)
        assertEquals(0f, fraction, 0.001f)
    }

    @Test
    fun `progressFraction clamps above one`() {
        val tier     = Tier.IMPROVING
        val fraction = tier.progressFraction(tier.maxRating + 100)
        assertEquals(1f, fraction, 0.001f)
    }

    @Test
    fun `all tier ranges are contiguous`() {
        val tiers = Tier.entries
        for (i in 0 until tiers.size - 1) {
            val current = tiers[i]
            val next    = tiers[i + 1]
            assertEquals(
                "Tier ${current.name} max should be 1 less than ${next.name} min",
                current.maxRating + 1,
                next.minRating
            )
        }
    }

    @Test
    fun `EloRating nextTier is correct`() {
        val elo = EloRating(mode = RatingMode.RAPID, rating = 1200)
        assertEquals(Tier.IMPROVING, elo.tier)
        assertEquals(Tier.INTERMEDIATE, elo.nextTier)
        assertEquals(Tier.INTERMEDIATE.minRating - 1200, elo.pointsToNextTier)
    }

    @Test
    fun `EloRating nextTier is null for ELITE`() {
        val elo = EloRating(mode = RatingMode.RAPID, rating = 2200)
        assertEquals(Tier.ELITE, elo.tier)
        assertNull(elo.nextTier)
        assertEquals(0, elo.pointsToNextTier)
    }
}
