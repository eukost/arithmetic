package com.example.data

import kotlinx.coroutines.flow.Flow

class MathRepository(private val mathDao: MathDao) {
    val userStatsFlow: Flow<UserStats?> = mathDao.getUserStatsFlow()
    val gameHistoryFlow: Flow<List<GameHistory>> = mathDao.getGameHistory()

    suspend fun getOrCreateUserStats(): UserStats {
        val existing = mathDao.getUserStatsDirect()
        if (existing != null) {
            return existing
        }
        val defaultStats = UserStats()
        mathDao.insertOrUpdateUserStats(defaultStats)
        return defaultStats
    }

    suspend fun updateUserStats(stats: UserStats) {
        mathDao.insertOrUpdateUserStats(stats)
    }

    suspend fun addGameHistory(history: GameHistory) {
        mathDao.insertGameHistory(history)
    }

    suspend fun clearHistory() {
        mathDao.clearHistory()
    }
}
