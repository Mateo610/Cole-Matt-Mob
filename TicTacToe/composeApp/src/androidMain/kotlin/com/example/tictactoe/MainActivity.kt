package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.tictactoe.navigation.AppNavigation
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
            onBackClick = { navController.popBackStack() }
        ) { padding ->
            AppNavigation(
                navController = navController,
                snackbarHostState = snackbarHostState
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
