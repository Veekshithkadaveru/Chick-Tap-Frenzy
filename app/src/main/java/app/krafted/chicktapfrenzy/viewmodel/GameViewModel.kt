package app.krafted.chicktapfrenzy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.chicktapfrenzy.data.db.AppDatabase
import app.krafted.chicktapfrenzy.data.db.ScoreEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).scoreDao()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeHighScore().collect { max ->
                _uiState.update { it.copy(highScore = max ?: 0) }
            }
        }
    }

    fun onHoleTapped(holeIndex: Int) {
        if (holeIndex < 0) return
        // TODO B2: score/lives deltas, golden bonus, fox penalty
    }

    fun onCharacterMissed(holeIndex: Int) {
        if (holeIndex < 0) return
        // TODO B2: missed-chick life penalty
    }

    fun startGame() {
        _uiState.update { current ->
            current.copy(
                score = 0,
                lives = 3,
                round = 1,
                roundTimeRemaining = 30f,
                isGameOver = false,
                isRoundComplete = false,
                isNewHighScore = false
            )
        }
        // TODO B1: kick GameThread
    }

    fun endGame() {
        val finalScore = uiState.value.score
        val currentHighScore = uiState.value.highScore

        viewModelScope.launch {
            val previousHighScore = dao.getHighScoreOnce() ?: 0
            dao.insertScore(ScoreEntity(score = finalScore))
            val isNewHighScore = finalScore > previousHighScore

            _uiState.update { current ->
                current.copy(
                    isGameOver = true,
                    highScore = maxOf(currentHighScore, finalScore),
                    isNewHighScore = isNewHighScore
                )
            }
        }
    }
}
