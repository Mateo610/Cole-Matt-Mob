package com.example.tictactoe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Provides the Material 3 theme configuration for the Tic-Tac-Toe application.
 */
object TicTacToeThemeConfig {

    /**
     * Light color scheme used by the application.
     */
    val lightColors = lightColorScheme(
        primary = TicTacToeColors.TopBarPurple,
        onPrimary = TicTacToeColors.OnPrimary,
        secondary = TicTacToeColors.NavyPrimary,
        tertiary = TicTacToeColors.TealTertiary,
        background = TicTacToeColors.GameBoardBackground,
        surface = TicTacToeColors.GameBoardBackground,
        onSurface = TicTacToeColors.OnSurface
    )
}

/**
 * Root theme composable that applies the Tic-Tac-Toe color scheme and typography.
 *
 * @param content Composable content that will be wrapped by the theme.
 */
@Composable
fun TicTacToeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TicTacToeThemeConfig.lightColors,
        typography = TicTacToeTypography.typography,
        content = content
    )
}

