package com.example.tictactoe.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tictactoe.R
import com.example.tictactoe.model.PlayerConfig
import com.example.tictactoe.model.PlayerType
import com.example.tictactoe.screens.WelcomeScreen

/**
 * Provides the navigation graph for the Tic-Tac-Toe application.
 */
object AppRoutes {
    /**
     * Route name for the welcome screen.
     */
    const val ROUTE_WELCOME: String = "welcome"

    /**
     * Route name for the game screen.
     */
    const val ROUTE_GAME: String = "game"

    /**
     * Route name for the game over screen.
     */
    const val ROUTE_GAME_OVER: String = "game_over"

    /**
     * Navigation argument key for the first player's name.
     */
    const val ARG_P1_NAME: String = "p1Name"

    /**
     * Navigation argument key for the second player's name.
     */
    const val ARG_P2_NAME: String = "p2Name"

    /**
     * Navigation argument key for the first player's type.
     */
    const val ARG_P1_TYPE: String = "p1Type"

    /**
     * Navigation argument key for the second player's type.
     */
    const val ARG_P2_TYPE: String = "p2Type"

    /**
     * Navigation argument key for the winner name.
     */
    const val ARG_WINNER_NAME: String = "winnerName"

    /**
     * Navigation argument key for the first player's win count.
     */
    const val ARG_P1_WINS: String = "p1Wins"

    /**
     * Navigation argument key for the second player's win count.
     */
    const val ARG_P2_WINS: String = "p2Wins"

    /**
     * Navigation argument key for the number of tied games.
     */
    const val ARG_TIES: String = "ties"

    /**
     * Navigation argument key for the serialized final board.
     */
    const val ARG_FINAL_BOARD: String = "finalBoard"
}

/**
 * Sets up the navigation graph for the application.
 *
 * Stage 2 wires the welcome screen to a stub game screen using navigation arguments.
 *
 * @param navController Controller used to manage navigation between destinations.
 * @param snackbarHostState Shared snackbar host state provided by the scaffold.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.ROUTE_WELCOME
    ) {
        composable(route = AppRoutes.ROUTE_WELCOME) {
            WelcomeScreen(
                snackbarHostState = snackbarHostState,
                onStartGame = { p1Config, p2Config ->
                    navController.navigate(
                        buildGameRoute(player1Config = p1Config, player2Config = p2Config)
                    )
                }
            )
        }

        composable(
            route = "${AppRoutes.ROUTE_GAME}?" +
                "${AppRoutes.ARG_P1_NAME}={${AppRoutes.ARG_P1_NAME}}&" +
                "${AppRoutes.ARG_P2_NAME}={${AppRoutes.ARG_P2_NAME}}&" +
                "${AppRoutes.ARG_P1_TYPE}={${AppRoutes.ARG_P1_TYPE}}&" +
                "${AppRoutes.ARG_P2_TYPE}={${AppRoutes.ARG_P2_TYPE}}",
            arguments = listOf(
                navArgument(AppRoutes.ARG_P1_NAME) { nullable = false },
                navArgument(AppRoutes.ARG_P2_NAME) { nullable = false },
                navArgument(AppRoutes.ARG_P1_TYPE) { nullable = false },
                navArgument(AppRoutes.ARG_P2_TYPE) { nullable = false }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(id = R.string.stub_game_screen_coming))
            }
        }

        composable(
            route = "${AppRoutes.ROUTE_GAME_OVER}?" +
                "${AppRoutes.ARG_WINNER_NAME}={${AppRoutes.ARG_WINNER_NAME}}&" +
                "${AppRoutes.ARG_P1_NAME}={${AppRoutes.ARG_P1_NAME}}&" +
                "${AppRoutes.ARG_P2_NAME}={${AppRoutes.ARG_P2_NAME}}&" +
                "${AppRoutes.ARG_P1_WINS}={${AppRoutes.ARG_P1_WINS}}&" +
                "${AppRoutes.ARG_P2_WINS}={${AppRoutes.ARG_P2_WINS}}&" +
                "${AppRoutes.ARG_TIES}={${AppRoutes.ARG_TIES}}&" +
                "${AppRoutes.ARG_FINAL_BOARD}={${AppRoutes.ARG_FINAL_BOARD}}",
            arguments = listOf(
                navArgument(AppRoutes.ARG_WINNER_NAME) { nullable = true },
                navArgument(AppRoutes.ARG_P1_NAME) { nullable = false },
                navArgument(AppRoutes.ARG_P2_NAME) { nullable = false },
                navArgument(AppRoutes.ARG_P1_WINS) { nullable = false },
                navArgument(AppRoutes.ARG_P2_WINS) { nullable = false },
                navArgument(AppRoutes.ARG_TIES) { nullable = false },
                navArgument(AppRoutes.ARG_FINAL_BOARD) { nullable = false }
            )
        ) {
            // Stage 2: game over screen comes later.
        }
    }
}

private fun buildGameRoute(player1Config: PlayerConfig, player2Config: PlayerConfig): String {
    val p1Name = Uri.encode(player1Config.name)
    val p2Name = Uri.encode(player2Config.name)
    val p1Type = Uri.encode(player1Config.type.name)
    val p2Type = Uri.encode(player2Config.type.name)
    return "${AppRoutes.ROUTE_GAME}?" +
        "${AppRoutes.ARG_P1_NAME}=$p1Name&" +
        "${AppRoutes.ARG_P2_NAME}=$p2Name&" +
        "${AppRoutes.ARG_P1_TYPE}=$p1Type&" +
        "${AppRoutes.ARG_P2_TYPE}=$p2Type"
}

