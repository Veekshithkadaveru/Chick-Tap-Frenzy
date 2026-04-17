package app.krafted.chicktapfrenzy.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.krafted.chicktapfrenzy.viewmodel.GameViewModel

private object ChickTapRoutes {
    const val Home = "home"
    const val Game = "game"
    const val GameOver = "game_over"
}

@Composable
fun ChickTapApp(gameViewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by gameViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = ChickTapRoutes.Home
    ) {
        composable(ChickTapRoutes.Home) {
            HomeScreen(
                highScore = uiState.highScore,
                onPlay = {
                    gameViewModel.startGame()
                    navController.navigate(ChickTapRoutes.Game) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(ChickTapRoutes.Game) {
            GameScreen(
                uiState = uiState,
                onTick = { delta -> gameViewModel.tickGame(delta) },
                onHoleTapped = { holeIndex -> gameViewModel.onHoleTapped(holeIndex) },
                onEndGame = {
                    gameViewModel.endGame()
                    navController.navigate(ChickTapRoutes.GameOver) {
                        launchSingleTop = true
                        popUpTo(ChickTapRoutes.Game) { inclusive = true }
                    }
                },
                onExitToHome = {
                    navController.navigate(ChickTapRoutes.Home) {
                        launchSingleTop = true
                        popUpTo(ChickTapRoutes.Home) { inclusive = true }
                    }
                }
            )
        }

        composable(ChickTapRoutes.GameOver) {
            GameOverScreen(
                uiState = uiState,
                onPlayAgain = {
                    gameViewModel.startGame()
                    navController.navigate(ChickTapRoutes.Game) {
                        launchSingleTop = true
                        popUpTo(ChickTapRoutes.Home)
                    }
                },
                onBackToHome = {
                    navController.navigate(ChickTapRoutes.Home) {
                        launchSingleTop = true
                        popUpTo(ChickTapRoutes.Home) { inclusive = true }
                    }
                }
            )
        }
    }
}
