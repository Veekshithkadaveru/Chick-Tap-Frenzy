package app.krafted.chicktapfrenzy.viewmodel

data class GameUiState(
    val score: Int = 0,
    val lives: Int = 3,
    val round: Int = 1,
    val roundTimeRemaining: Float = 30f,
    val isGameOver: Boolean = false,
    val isRoundComplete: Boolean = false,
    val highScore: Int = 0,
    val isNewHighScore: Boolean = false
)
