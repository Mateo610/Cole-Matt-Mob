package com.example.tictactoe.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tictactoe.model.PlayerConfig
import com.example.tictactoe.model.PlayerType
import com.example.tictactoe.model.RoundOutcome
import com.example.tictactoe.screens.GameOverScreen
import com.example.tictactoe.screens.GameScreen
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
     * [androidx.lifecycle.SavedStateHandle] key: when set to true, [GameScreen] starts a new round.
     */
    const val SAVED_STATE_NEW_ROUND: String = "newRound"

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
     * Navigation argument key for [RoundOutcome] (serialized enum name).
     */
    const val ARG_OUTCOME: String = "outcome"

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
 * @param navController Controller used to manage navigation between destinations.
 * @param snackbarHostState Shared snackbar host state provided by the scaffold.
 * @param modifier Modifier applied to the [NavHost] (e.g. scaffold padding).
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.ROUTE_WELCOME,
        modifier = modifier.fillMaxSize()
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
                navArgument(AppRoutes.ARG_P1_NAME) { type = NavType.StringType },
                navArgument(AppRoutes.ARG_P2_NAME) { type = NavType.StringType },
                navArgument(AppRoutes.ARG_P1_TYPE) { type = NavType.StringType },
                navArgument(AppRoutes.ARG_P2_TYPE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val p1Name = args?.getString(AppRoutes.ARG_P1_NAME)?.let { Uri.decode(it) }.orEmpty()
            val p2Name = args?.getString(AppRoutes.ARG_P2_NAME)?.let { Uri.decode(it) }.orEmpty()
            val p1TypeName = args?.getString(AppRoutes.ARG_P1_TYPE).orEmpty()
            val p2TypeName = args?.getString(AppRoutes.ARG_P2_TYPE).orEmpty()
            val p1Type = runCatching { PlayerType.valueOf(p1TypeName) }.getOrDefault(PlayerType.HUMAN)
            val p2Type = runCatching { PlayerType.valueOf(p2TypeName) }.getOrDefault(PlayerType.HUMAN)
            val p1Config = PlayerConfig(name = p1Name, type = p1Type)
            val p2Config = PlayerConfig(name = p2Name, type = p2Type)

            GameScreen(
                player1Config = p1Config,
                player2Config = p2Config,
                snackbarHostState = snackbarHostState,
                navBackStackEntry = backStackEntry,
                onGameOver = { outcome, p1Wins, p2Wins, ties, finalBoard ->
                    navController.navigate(
                        buildGameOverRoute(
                            outcome = outcome,
                            player1Name = p1Name,
                            player2Name = p2Name,
                            p1Wins = p1Wins,
                            p2Wins = p2Wins,
                            ties = ties,
                            finalBoard = finalBoard.serialize()
                        )
                    )
                }
            )
        }

        composable(
            route = "${AppRoutes.ROUTE_GAME_OVER}?" +
                "${AppRoutes.ARG_OUTCOME}={${AppRoutes.ARG_OUTCOME}}&" +
                "${AppRoutes.ARG_P1_NAME}={${AppRoutes.ARG_P1_NAME}}&" +
                "${AppRoutes.ARG_P2_NAME}={${AppRoutes.ARG_P2_NAME}}&" +
                "${AppRoutes.ARG_P1_WINS}={${AppRoutes.ARG_P1_WINS}}&" +
                "${AppRoutes.ARG_P2_WINS}={${AppRoutes.ARG_P2_WINS}}&" +
                "${AppRoutes.ARG_TIES}={${AppRoutes.ARG_TIES}}&" +
                "${AppRoutes.ARG_FINAL_BOARD}={${AppRoutes.ARG_FINAL_BOARD}}",
            arguments = listOf(
                navArgument(AppRoutes.ARG_OUTCOME) {
                    type = NavType.StringType
                    defaultValue = "TIE"
                },
                navArgument(AppRoutes.ARG_P1_NAME) { type = NavType.StringType },
                navArgument(AppRoutes.ARG_P2_NAME) { type = NavType.StringType },
                navArgument(AppRoutes.ARG_P1_WINS) { type = NavType.IntType },
                navArgument(AppRoutes.ARG_P2_WINS) { type = NavType.IntType },
                navArgument(AppRoutes.ARG_TIES) { type = NavType.IntType },
                navArgument(AppRoutes.ARG_FINAL_BOARD) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val rawOutcome = args?.getString(AppRoutes.ARG_OUTCOME).orEmpty()
            val outcome = runCatching { RoundOutcome.valueOf(rawOutcome) }.getOrDefault(RoundOutcome.TIE)
            val p1Name = args?.getString(AppRoutes.ARG_P1_NAME)?.let { Uri.decode(it) }.orEmpty()
            val p2Name = args?.getString(AppRoutes.ARG_P2_NAME)?.let { Uri.decode(it) }.orEmpty()
            val p1Wins = args?.getInt(AppRoutes.ARG_P1_WINS) ?: 0
            val p2Wins = args?.getInt(AppRoutes.ARG_P2_WINS) ?: 0
            val ties = args?.getInt(AppRoutes.ARG_TIES) ?: 0
            val startNewRound: () -> Unit = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(AppRoutes.SAVED_STATE_NEW_ROUND, true)
                navController.popBackStack()
            }

            GameOverScreen(
                outcome = outcome,
                player1Name = p1Name,
                player2Name = p2Name,
                player1Wins = p1Wins,
                player2Wins = p2Wins,
                ties = ties,
                onStartNewGame = startNewRound
            )
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

private fun buildGameOverRoute(
    outcome: RoundOutcome,
    player1Name: String,
    player2Name: String,
    p1Wins: Int,
    p2Wins: Int,
    ties: Int,
    finalBoard: String
): String {
    val o = Uri.encode(outcome.name)
    val p1 = Uri.encode(player1Name)
    val p2 = Uri.encode(player2Name)
    val b = Uri.encode(finalBoard)
    return "${AppRoutes.ROUTE_GAME_OVER}?" +
        "${AppRoutes.ARG_OUTCOME}=$o&" +
        "${AppRoutes.ARG_P1_NAME}=$p1&" +
        "${AppRoutes.ARG_P2_NAME}=$p2&" +
        "${AppRoutes.ARG_P1_WINS}=$p1Wins&" +
        "${AppRoutes.ARG_P2_WINS}=$p2Wins&" +
        "${AppRoutes.ARG_TIES}=$ties&" +
        "${AppRoutes.ARG_FINAL_BOARD}=$b"
}
