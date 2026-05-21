package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameHistory
import com.example.data.MathDatabase
import com.example.data.MathRepository
import com.example.data.UserStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class Question(
    val num1: Int,
    val num2: Int,
    val operation: String, // "+", "-", "×", "÷"
    val correctAnswer: Int,
    val options: List<Int>,
    val explanation: String
)

class MathViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MathRepository

    val userStats: StateFlow<UserStats?>
    val gameHistory: StateFlow<List<GameHistory>>

    init {
        val database = MathDatabase.getDatabase(application)
        repository = MathRepository(database.mathDao())
        userStats = repository.userStatsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        gameHistory = repository.gameHistoryFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize user stats in database
        viewModelScope.launch {
            repository.getOrCreateUserStats()
            checkAndUpdateStreak()
        }
    }

    // Single profiles update/creation helpers
    fun updateProfile(name: String, avatar: String) {
        viewModelScope.launch {
            val current = repository.getOrCreateUserStats()
            repository.updateUserStats(current.copy(name = name, avatar = avatar))
        }
    }

    private suspend fun checkAndUpdateStreak() {
        val current = repository.getOrCreateUserStats()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDateStr = current.lastActiveDate

        if (lastDateStr == todayStr) {
            // Already active today, do nothing to streak
            return
        }

        val newStreak = if (lastDateStr.isEmpty()) {
            1
        } else {
            try {
                val lastDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastDateStr)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayStr)
                val diffMills = today.time - lastDate.time
                val diffDays = diffMills / (1000 * 60 * 60 * 24)

                if (diffDays == 1L) {
                    current.streak + 1
                } else if (diffDays > 1L) {
                    1 // Streak broken, reset to 1
                } else {
                    current.streak // Negative diff (not expected, do nothing)
                }
            } catch (e: Exception) {
                1
            }
        }

        repository.updateUserStats(current.copy(streak = newStreak, lastActiveDate = todayStr))
    }

    // --- GAME ENGINE STATE ---
    var activeGameMode by mutableStateOf<String?>(null) // "ADDITION", "SUBTRACTION", "MULTIPLICATION", "DIVISION"
    var activeGameLevel by mutableStateOf(1)
    
    var currentQuestionIndex by mutableStateOf(0)
    val totalQuestionsCount = 5
    var gameScore by mutableStateOf(0)
    var correctAnswersCount by mutableStateOf(0)
    var currentQuestion by mutableStateOf<Question?>(null)
    
    var selectedAnswer by mutableStateOf<Int?>(null)
    var isAnswerChecked by mutableStateOf(false)
    var isAnswerCorrect by mutableStateOf(false)
    
    var isGameFinished by mutableStateOf(false)
    var starsEarnedInGame by mutableStateOf(0)

    // Start a new game
    fun startGame(operation: String, level: Int) {
        activeGameMode = operation
        activeGameLevel = level
        currentQuestionIndex = 0
        gameScore = 0
        correctAnswersCount = 0
        isGameFinished = false
        starsEarnedInGame = 0
        setupNextQuestion()
    }

    private fun setupNextQuestion() {
        selectedAnswer = null
        isAnswerChecked = false
        isAnswerCorrect = false
        currentQuestion = generateQuestion(activeGameMode ?: "ADDITION", activeGameLevel)
    }

    fun submitAnswer(answer: Int) {
        if (isAnswerChecked) return
        selectedAnswer = answer
        val correct = (answer == currentQuestion?.correctAnswer)
        isAnswerCorrect = correct
        isAnswerChecked = true
        
        if (correct) {
            correctAnswersCount++
            gameScore += 20 * activeGameLevel // Scoring scale with level
        }
    }

    fun nextQuestion() {
        if (currentQuestionIndex + 1 < totalQuestionsCount) {
            currentQuestionIndex++
            setupNextQuestion()
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        isGameFinished = true
        // Calculate stars based on correctness
        starsEarnedInGame = when (correctAnswersCount) {
            5 -> 3
            4 -> 2
            3 -> 1
            else -> 0
        }

        viewModelScope.launch {
            val currentStats = repository.getOrCreateUserStats()
            
            // Unlocked level progress update
            var newAdd = currentStats.additionLevel
            var newSub = currentStats.subtractionLevel
            var newMul = currentStats.multiplicationLevel
            var newDiv = currentStats.divisionLevel

            // If player gets 3 or 4/5, they unlock the next level up to Level 5 limit!
            val didPass = correctAnswersCount >= 3
            if (didPass) {
                when (activeGameMode) {
                    "ADDITION" -> if (activeGameLevel == currentStats.additionLevel && activeGameLevel < 5) newAdd = activeGameLevel + 1
                    "SUBTRACTION" -> if (activeGameLevel == currentStats.subtractionLevel && activeGameLevel < 5) newSub = activeGameLevel + 1
                    "MULTIPLICATION" -> if (activeGameLevel == currentStats.multiplicationLevel && activeGameLevel < 5) newMul = activeGameLevel + 1
                    "DIVISION" -> if (activeGameLevel == currentStats.divisionLevel && activeGameLevel < 5) newDiv = activeGameLevel + 1
                }
            }

            // Update stats
            val updatedStats = currentStats.copy(
                stars = currentStats.stars + starsEarnedInGame,
                additionLevel = newAdd,
                subtractionLevel = newSub,
                multiplicationLevel = newMul,
                divisionLevel = newDiv
            )
            repository.updateUserStats(updatedStats)

            // Save history
            val history = GameHistory(
                operation = activeGameMode ?: "ADDITION",
                level = activeGameLevel,
                score = gameScore,
                totalQuestions = totalQuestionsCount,
                correctAnswers = correctAnswersCount
            )
            repository.addGameHistory(history)
        }
    }

    fun exitGame() {
        activeGameMode = null
        currentQuestion = null
        isGameFinished = false
    }

    // --- SANDBOX STATE ---
    var sandboxNum1 by mutableStateOf(3)
    var sandboxNum2 by mutableStateOf(2)
    var sandboxOp by mutableStateOf("+") // "+", "-", "×", "÷"

    fun updateSandbox(num1: Int, num2: Int, op: String) {
        sandboxNum1 = num1.coerceIn(1, 10)
        sandboxNum2 = num2.coerceIn(1, 10)
        sandboxOp = op
    }

    // Clear history helper
    fun clearGameHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // --- MATH QUESTION GENERATION ENGINE ---
    private fun generateQuestion(operation: String, level: Int): Question {
        var num1 = 0
        var num2 = 0
        var correctAnswer = 0
        var explanation = ""
        val charOp = when (operation) {
            "ADDITION" -> "+"
            "SUBTRACTION" -> "-"
            "MULTIPLICATION" -> "×"
            else -> "÷"
        }

        when (operation) {
            "ADDITION" -> {
                when (level) {
                    1 -> { // 1-5
                        num1 = Random.nextInt(1, 6)
                        num2 = Random.nextInt(1, 6)
                    }
                    2 -> { // 1-10
                        num1 = Random.nextInt(1, 11)
                        num2 = Random.nextInt(1, 11)
                    }
                    3 -> { // 5-15
                        num1 = Random.nextInt(5, 16)
                        num2 = Random.nextInt(5, 16)
                    }
                    4 -> { // 10-30
                        num1 = Random.nextInt(10, 31)
                        num2 = Random.nextInt(10, 31)
                    }
                    else -> { // 20-50
                        num1 = Random.nextInt(20, 51)
                        num2 = Random.nextInt(20, 51)
                    }
                }
                correctAnswer = num1 + num2
                explanation = "Put $num1 and $num2 together to make $correctAnswer!"
            }
            "SUBTRACTION" -> {
                when (level) {
                    1 -> { // 1-5, result >= 0
                        num1 = Random.nextInt(1, 6)
                        num2 = Random.nextInt(1, num1 + 1)
                    }
                    2 -> { // 1-10
                        num1 = Random.nextInt(1, 11)
                        num2 = Random.nextInt(1, num1 + 1)
                    }
                    3 -> { // 5-20
                        num1 = Random.nextInt(5, 21)
                        num2 = Random.nextInt(1, num1 + 1)
                    }
                    4 -> { // 10-50
                        num1 = Random.nextInt(10, 51)
                        num2 = Random.nextInt(5, num1 + 1)
                    }
                    else -> { // 20-100
                        num1 = Random.nextInt(20, 101)
                        num2 = Random.nextInt(10, num1 + 1)
                    }
                }
                correctAnswer = num1 - num2
                explanation = "Take away $num2 from $num1. You are left with $correctAnswer!"
            }
            "MULTIPLICATION" -> {
                when (level) {
                    1 -> { // 1, 2, 5 tables up to 5
                        val factors = listOf(1, 2, 5)
                        num1 = factors.random()
                        num2 = Random.nextInt(1, 6)
                    }
                    2 -> { // 2, 3, 4 tables up to 5
                        num1 = Random.nextInt(2, 6)
                        num2 = Random.nextInt(1, 6)
                    }
                    3 -> { // Tables up to 10
                        num1 = Random.nextInt(2, 10)
                        num2 = Random.nextInt(1, 10)
                    }
                    4 -> { // Tables up to 12
                        num1 = Random.nextInt(3, 13)
                        num2 = Random.nextInt(2, 11)
                    }
                    else -> { // Mix of tables and double digits
                        num1 = Random.nextInt(5, 15)
                        num2 = Random.nextInt(5, 11)
                    }
                }
                correctAnswer = num1 * num2
                explanation = "$num1 groups of $num2 equals $correctAnswer!"
            }
            "DIVISION" -> {
                // Generates dividend and divisor strictly yielding integers
                when (level) {
                    1 -> { // Quotient up to 5, divisor 1-3
                        num2 = Random.nextInt(1, 4)
                        val quotient = Random.nextInt(1, 6)
                        num1 = quotient * num2
                    }
                    2 -> { // Divisor 1-5, quotient 1-10
                        num2 = Random.nextInt(2, 6)
                        val quotient = Random.nextInt(1, 11)
                        num1 = quotient * num2
                    }
                    3 -> { // Divisor 2-8, quotient 2-10
                        num2 = Random.nextInt(2, 9)
                        val quotient = Random.nextInt(2, 11)
                        num1 = quotient * num2
                    }
                    4 -> { // Divisor 3-10, quotient 3-12
                        num2 = Random.nextInt(3, 11)
                        val quotient = Random.nextInt(3, 13)
                        num1 = quotient * num2
                    }
                    else -> { // Challenging: larger division tables
                        num2 = Random.nextInt(5, 13)
                        val quotient = Random.nextInt(5, 13)
                        num1 = quotient * num2
                    }
                }
                correctAnswer = num1 / num2
                explanation = "Divide $num1 cookies into $num2 groups. Each group gets $correctAnswer!"
            }
        }

        // Generate multiple choice options
        val options = mutableSetOf<Int>()
        options.add(correctAnswer)

        // Generate distractors close to correct answer
        var attempts = 0
        while (options.size < 4 && attempts < 50) {
            val offset = Random.nextInt(-4, 5)
            val option = correctAnswer + offset
            if (option >= 0 && option != correctAnswer) {
                options.add(option)
            }
            attempts++
        }

        // Add pure random distractors if needed
        while (options.size < 4) {
            val option = correctAnswer + Random.nextInt(1, 15)
            options.add(option)
        }

        return Question(
            num1 = num1,
            num2 = num2,
            operation = charOp,
            correctAnswer = correctAnswer,
            options = options.toList().shuffled(),
            explanation = explanation
        )
    }
}
