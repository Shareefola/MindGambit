package com.mindgambit.app.data.repository

import com.mindgambit.app.data.database.dao.EloDao
import com.mindgambit.app.data.database.entities.EloHistoryEntity
import com.mindgambit.app.domain.model.EloRating
import com.mindgambit.app.domain.model.RatingMode
import com.mindgambit.app.domain.model.Tier
import com.mindgambit.app.domain.repository.EloRepository
import com.mindgambit.app.domain.repository.RatingHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ============================================================
// MindGambit — EloRepositoryImpl
// Maps Room entities → domain RatingHistoryEntry (clean arch).
// ============================================================

class EloRepositoryImpl @Inject constructor(
    private val eloDao: EloDao
) : EloRepository {

    override suspend fun getRating(mode: RatingMode): EloRating {
        val entity = eloDao.getLatestForMode(mode.name)
        val rating = entity?.rating ?: 800
        return EloRating(
            mode        = mode,
            rating      = rating,
            confidence  = entity?.confidence ?: 1.0,
            tier        = Tier.fromRating(rating),
            gamesPlayed = 0
        )
    }

    override fun getHistory(mode: RatingMode): Flow<List<RatingHistoryEntry>> =
        eloDao.getHistoryForMode(mode.name).map { list -> list.map { it.toDomain() } }

    override fun getRecentHistory(): Flow<List<RatingHistoryEntry>> =
        eloDao.getRecentHistory().map { list -> list.map { it.toDomain() } }

    override suspend fun updateRating(mode: RatingMode, newRating: Int, delta: Int) {
        eloDao.insertRating(
            EloHistoryEntity(
                mode       = mode.name,
                rating     = newRating,
                delta      = delta,
                confidence = 1.0
            )
        )
    }

    override suspend fun getAllRatings(): Map<RatingMode, EloRating> =
        RatingMode.entries.associateWith { mode -> getRating(mode) }

    private fun EloHistoryEntity.toDomain() = RatingHistoryEntry(
        mode       = RatingMode.valueOf(mode),
        rating     = rating,
        delta      = delta,
        recordedAt = recordedAt
    )
}
