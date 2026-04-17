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
fun GameScreen(
    uiState: GameUiState,
    onEndGame: () -> Unit,
    onExitToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game in Progress",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Score: ${uiState.score}",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Lives: ${uiState.lives}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Round: ${uiState.round}",
            style = MaterialTheme.typography.titleMedium
        )
        Button(
            onClick = onEndGame,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text("End Game")
        }
        Button(
            onClick = onExitToHome,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Back To Home")
        }
    }
}
