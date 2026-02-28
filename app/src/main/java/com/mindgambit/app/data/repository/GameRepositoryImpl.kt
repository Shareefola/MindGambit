package com.mindgambit.app.data.repository

import com.mindgambit.app.data.database.dao.GameDao
import com.mindgambit.app.data.database.entities.toEntity
import com.mindgambit.app.domain.model.Game
import com.mindgambit.app.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ============================================================
// MindGambit — GameRepositoryImpl
// ============================================================

class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao
) : GameRepository {

    override fun getAllGames(): Flow<List<Game>> =
        gameDao.getAllGames().map { list -> list.map { it.toDomain() } }

    override suspend fun getGameById(id: Long): Game? =
        gameDao.getGameById(id)?.toDomain()

    override suspend fun getRecentGames(limit: Int): List<Game> =
        gameDao.getRecentGames(limit).map { it.toDomain() }

    override suspend fun saveGame(game: Game): Long =
        gameDao.insertGame(game.toEntity())

    override suspend fun updateGame(game: Game) =
        gameDao.updateGame(game.toEntity())

    override suspend fun deleteGame(id: Long) =
        gameDao.deleteGameById(id)

    override fun getGameCount(): Flow<Int> =
        gameDao.getGameCount()

    override fun getAverageAccuracy(): Flow<Float?> =
        gameDao.getAverageAccuracy()
}
