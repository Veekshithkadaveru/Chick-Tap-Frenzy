package app.krafted.chicktapfrenzy.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.krafted.chicktapfrenzy.viewmodel.GameViewModel

private object ChickTapRoutes {
    const val Splash = "splash"
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
        startDestination = ChickTapRoutes.Splash,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A0F04))
    ) {
        composable(
            route = ChickTapRoutes.Splash,
            exitTransition = { fadeOut(androidx.compose.animation.core.tween(400)) }
        ) {
            SplashScreen(
                onFinished = {
                    navController.navigate(ChickTapRoutes.Home) {
                        popUpTo(ChickTapRoutes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = ChickTapRoutes.Home,
            enterTransition = {
                fadeIn(androidx.compose.animation.core.tween(500)) +
                        scaleIn(
                            initialScale = 0.96f,
                            animationSpec = androidx.compose.animation.core.tween(500)
                        )
            }
        ) {
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
                },
                onStartNextRound = {
                    gameViewModel.startNextRound()
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
