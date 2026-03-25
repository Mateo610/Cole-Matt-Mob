package com.example.tictactoe.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tictactoe.R
import com.example.tictactoe.navigation.AppRoutes
import com.example.tictactoe.ui.theme.TicTacToeColors

/**
 * Application-level scaffold that hosts the top app bar and snackbar host.
 *
 * The scaffold is created once at the activity level and wraps the navigation host.
 *
 * @param navController Controller used to query the current route.
 * @param snackbarHostState Shared snackbar state used throughout the app.
 * @param onBackClick Callback when the back arrow is pressed on game or game-over routes
 *   (welcome has no top bar). The host should pop the stack or set the game entry new-round flag.
 * @param content Screen content that receives the scaffold padding values.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppScaffold(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onBackClick: (() -> Unit)?,
    content: @Composable (PaddingValues) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String? = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            when (currentRoute?.substringBefore('?')) {
                AppRoutes.ROUTE_GAME -> {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = TicTacToeColors.TopBarPurple,
                            titleContentColor = TicTacToeColors.OnPrimary,
                            navigationIconContentColor = TicTacToeColors.OnPrimary
                        ),
                        title = { Text(text = stringResource(id = R.string.title_game)) },
                        navigationIcon = {
                            IconButton(onClick = { onBackClick?.invoke() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.cd_back_button)
                                )
                            }
                        }
                    )
                }

                AppRoutes.ROUTE_GAME_OVER -> {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = TicTacToeColors.TopBarPurple,
                            titleContentColor = TicTacToeColors.OnPrimary,
                            navigationIconContentColor = TicTacToeColors.OnPrimary
                        ),
                        title = { Text(text = stringResource(id = R.string.title_game_over)) },
                        navigationIcon = {
                            IconButton(onClick = { onBackClick?.invoke() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.cd_back_button)
                                )
                            }
                        }
                    )
                }

                else -> {
                    // No top app bar on the welcome route by design.
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = content
    )
}

