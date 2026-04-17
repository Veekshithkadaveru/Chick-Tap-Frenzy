package app.krafted.chicktapfrenzy.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.krafted.chicktapfrenzy.viewmodel.GameUiState

@Composable
fun GameOverScreen(
    uiState: GameUiState,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Score: ${uiState.score}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "High Score: ${uiState.highScore}",
            style = MaterialTheme.typography.titleMedium
        )
        if (uiState.isNewHighScore) {
            Text(
                text = "NEW HIGH SCORE!",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text("Play Again")
        }
        Button(
            onClick = onBackToHome,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Home")
        }
    }
}
