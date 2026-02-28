package com.mindgambit.app.domain.model

import androidx.compose.ui.graphics.Color
import com.mindgambit.app.presentation.theme.*

// ============================================================
// MindGambit — Tier & Elo Rating Models
// ============================================================

enum class Tier(
    val displayName:  String,
    val minRating:    Int,
    val maxRating:    Int,
    val badge:        String,
    val color:        Color,
    val description:  String
) {
    NOVICE(
        "Novice",       400,  799,  "🥉", TierNovice,
        "Learning the fundamentals"
    ),
    BEGINNER(
        "Beginner",     800,  1099, "🥈", TierBeginner,
        "Building basic patterns"
    ),
    IMPROVING(
        "Improving",    1100, 1399, "⭐", TierImproving,
        "Developing tactical vision"
    ),
    INTERMEDIATE(
        "Intermediate", 1400, 1699, "🔷", TierIntermediate,
        "Mastering positional play"
    ),
    ADVANCED(
        "Advanced Club",1700, 1999, "💎", TierAdvanced,
        "Strategic depth unlocked"
    ),
    ELITE(
        "Elite Track",  2000, 2400, "👑", TierElite,
        "Peak competitive chess"
    );

    fun progressFraction(rating: Int): Float {
        val range = (maxRating - minRating).toFloat()
        val progress = (rating - minRating).coerceIn(0, maxRating - minRating).toFloat()
        return progress / range
    }

    companion object {
        fun fromRating(rating: Int): Tier =
            entries.lastOrNull { rating >= it.minRating } ?: NOVICE
    }
}

enum class RatingMode(val displayName: String, val icon: String) {
    RAPID    ("Rapid",    "⏱"),
    BLITZ    ("Blitz",    "⚡"),
    TACTICAL ("Tactical", "🧩"),
    STRATEGIC("Strategic","♟"),
}

data class EloRating(
    val mode:       RatingMode,
    val rating:     Int,
    val confidence: Double  = 1.0,   // Glicko-2 RD (rating deviation) normalized
    val tier:       Tier    = Tier.fromRating(rating),
    val gamesPlayed:Int     = 0,
    val winCount:   Int     = 0,
    val lossCount:  Int     = 0,
    val drawCount:  Int     = 0
) {
    val winRate: Float get() =
        if (gamesPlayed == 0) 0f else winCount.toFloat() / gamesPlayed

    val nextTier: Tier? get() {
        val next = tier.ordinal + 1
        return if (next < Tier.entries.size) Tier.entries[next] else null
    }

    val pointsToNextTier: Int get() =
        nextTier?.let { it.minRating - rating }?.coerceAtLeast(0) ?: 0
}
