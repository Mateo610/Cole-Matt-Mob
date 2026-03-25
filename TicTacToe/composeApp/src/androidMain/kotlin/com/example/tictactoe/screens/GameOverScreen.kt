package com.example.tictactoe.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.R
import com.example.tictactoe.model.RoundOutcome
import com.example.tictactoe.ui.theme.TicTacToeColors

/**
 * Shown when a round ends: outcome, running scores, and control to start another round.
 *
 * @param outcome Whether player 1 won, player 2 won, or the round was a tie.
 * @param player1Name First player display name (for the congrats line when they win).
 * @param player2Name Second player display name.
 * @param player1Wins Running win count for player 1.
 * @param player2Wins Running win count for player 2.
 * @param ties Running tie count.
 * @param onStartNewGame Resets the board for a new round (same as the system back gesture here).
 * @param modifier Optional modifier for the root of this screen.
 */
@Composable
fun GameOverScreen(
    outcome: RoundOutcome,
    player1Name: String,
    player2Name: String,
    player1Wins: Int,
    player2Wins: Int,
    ties: Int,
    onStartNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onStartNewGame)

    val headline = when (outcome) {
        RoundOutcome.TIE -> stringResource(id = R.string.result_headline_tie)
        RoundOutcome.PLAYER1_WIN -> stringResource(id = R.string.result_headline_p1_won)
        RoundOutcome.PLAYER2_WIN -> stringResource(id = R.string.result_headline_p2_won)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TicTacToeColors.GameBoardBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = headline,
            modifier = Modifier.fillMaxWidth(),
            color = TicTacToeColors.OnGameBoardText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (outcome) {
            RoundOutcome.PLAYER1_WIN -> {
                SubtitleCongrats(winnerName = player1Name)
                Spacer(modifier = Modifier.height(24.dp))
            }
            RoundOutcome.PLAYER2_WIN -> {
                SubtitleCongrats(winnerName = player2Name)
                Spacer(modifier = Modifier.height(24.dp))
            }
            RoundOutcome.TIE -> {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        ScoreLines(
            player1Wins = player1Wins,
            player2Wins = player2Wins,
            ties = ties,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStartNewGame,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(id = R.string.btn_start_new_game),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Personalized line under the headline when there is a winner.
 */
@Composable
private fun SubtitleCongrats(winnerName: String) {
    Text(
        text = stringResource(id = R.string.result_congrats_player, winnerName),
        modifier = Modifier.fillMaxWidth(),
        color = TicTacToeColors.OnGameBoardText,
        fontSize = 18.sp,
        textAlign = TextAlign.Center
    )
}

/**
 * Running session statistics for both players and ties.
 */
@Composable
private fun ScoreLines(
    player1Wins: Int,
    player2Wins: Int,
    ties: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.score_p1_wins, player1Wins),
            color = TicTacToeColors.OnGameBoardText,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(id = R.string.score_p2_wins, player2Wins),
            color = TicTacToeColors.OnGameBoardText,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(id = R.string.score_ties_count, ties),
            color = TicTacToeColors.OnGameBoardText,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
