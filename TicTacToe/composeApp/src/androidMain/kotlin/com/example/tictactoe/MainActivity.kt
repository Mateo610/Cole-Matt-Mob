package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.tictactoe.navigation.AppNavigation
import com.example.tictactoe.navigation.AppRoutes
import com.example.tictactoe.scaffold.AppScaffold
import com.example.tictactoe.ui.theme.TicTacToeTheme

/**
 * Main activity that hosts the Tic-Tac-Toe application.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContent {
            TicTacToeApp()
        }
    }
}

/**
 * Root composable entry point for the Android application.
 */
@Composable
fun TicTacToeApp() {
    TicTacToeTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }

        AppScaffold(
            navController = navController,
            snackbarHostState = snackbarHostState,
            onBackClick = {
                when (navController.currentBackStackEntry?.destination?.route?.substringBefore('?')) {
                    AppRoutes.ROUTE_GAME -> {
                        navController.popBackStack()
                    }
                    AppRoutes.ROUTE_GAME_OVER -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(AppRoutes.SAVED_STATE_NEW_ROUND, true)
                        navController.popBackStack()
                    }
                }
            }
        ) { padding ->
            AppNavigation(
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

/**
 * Preview of the root Android application composable.
 */
@Preview
@Composable
fun TicTacToeAppPreview() {
    TicTacToeApp()
}
