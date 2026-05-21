package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Let's keep a single active profile for simplicity, or we can make it auto-increment if we support multiple children. Let's make it fixed id so we can easily query/update the single active profile!
    val name: String = "Cute Explorer",
    val avatar: String = "bear", // "bear", "rabbit", "frog", "lion", "monkey"
    val stars: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val additionLevel: Int = 1,
    val subtractionLevel: Int = 1,
    val multiplicationLevel: Int = 1,
    val divisionLevel: Int = 1
)

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val operation: String, // "ADDITION", "SUBTRACTION", "MULTIPLICATION", "DIVISION"
    val level: Int,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timestamp: Long = System.currentTimeMillis()
)
