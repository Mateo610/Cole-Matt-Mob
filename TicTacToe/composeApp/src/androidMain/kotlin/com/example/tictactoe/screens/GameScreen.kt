package com.example.tictactoe.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.example.tictactoe.R
import com.example.tictactoe.logic.chooseAiMove
import com.example.tictactoe.model.Board
import com.example.tictactoe.model.Move
import com.example.tictactoe.model.PlayerConfig
import com.example.tictactoe.model.PlayerPiece
import com.example.tictactoe.model.PlayerType
import com.example.tictactoe.model.RoundOutcome
import com.example.tictactoe.navigation.AppRoutes
import com.example.tictactoe.ui.theme.TicTacToeColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private const val AI_MOVE_DELAY_MS: Long = 650L

/**
 * Main play screen: board, turn indicator, human moves, and AI moves with delay.
 *
 * @param player1Config First player (always [PlayerPiece.X]).
 * @param player2Config Second player (always [PlayerPiece.O]).
 * @param snackbarHostState Host for error snackbars.
 * @param navBackStackEntry Entry for this destination; used to read [AppRoutes.SAVED_STATE_NEW_ROUND].
 * @param onGameOver Invoked when the round ends with outcome and running score totals.
 * @param modifier Optional modifier for the root of this screen.
 */
@Composable
fun GameScreen(
    player1Config: PlayerConfig,
    player2Config: PlayerConfig,
    snackbarHostState: SnackbarHostState,
    navBackStackEntry: NavBackStackEntry,
    onGameOver: (
        outcome: RoundOutcome,
        p1Wins: Int,
        p2Wins: Int,
        ties: Int,
        finalBoard: Board
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var board by remember { mutableStateOf(Board.empty()) }
    var currentPlayerIsP1 by remember { mutableStateOf(true) }
    var p1Wins by rememberSaveable { mutableStateOf(0) }
    var p2Wins by rememberSaveable { mutableStateOf(0) }
    var ties by rememberSaveable { mutableStateOf(0) }
    var isAiThinking by remember { mutableStateOf(false) }

    val savedStateHandle = navBackStackEntry.savedStateHandle
    val scope = rememberCoroutineScope()
    val errorAiThinking = stringResource(id = R.string.error_ai_thinking)
    val errorCellOccupied = stringResource(id = R.string.error_cell_occupied)

    LaunchedEffect(savedStateHandle) {
        snapshotFlow { savedStateHandle.get<Boolean>(AppRoutes.SAVED_STATE_NEW_ROUND) == true }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                board = Board.empty()
                currentPlayerIsP1 = true
                isAiThinking = false
                savedStateHandle[AppRoutes.SAVED_STATE_NEW_ROUND] = false
            }
    }

    fun currentPlayerType(): PlayerType =
        if (currentPlayerIsP1) player1Config.type else player2Config.type

    fun currentPiece(): PlayerPiece =
        if (currentPlayerIsP1) PlayerPiece.X else PlayerPiece.O

    fun currentPlayerName(): String =
        if (currentPlayerIsP1) player1Config.name else player2Config.name

    fun showSnackbar(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun applyTurn(newBoard: Board) {
        board = newBoard
        val winner = newBoard.checkWinner()
        when {
            winner != null -> {
                if (winner == PlayerPiece.X) p1Wins++ else p2Wins++
                val outcome = if (winner == PlayerPiece.X) {
                    RoundOutcome.PLAYER1_WIN
                } else {
                    RoundOutcome.PLAYER2_WIN
                }
                onGameOver(outcome, p1Wins, p2Wins, ties, newBoard)
            }
            newBoard.isFull() -> {
                ties++
                onGameOver(RoundOutcome.TIE, p1Wins, p2Wins, ties, newBoard)
            }
            else -> {
                currentPlayerIsP1 = !currentPlayerIsP1
            }
        }
    }

    fun onCellClicked(row: Int, col: Int) {
        if (currentPlayerType() != PlayerType.HUMAN) {
            showSnackbar(message = errorAiThinking)
            return
        }
        if (isAiThinking) {
            showSnackbar(message = errorAiThinking)
            return
        }
        if (board.getCell(row, col) != null) {
            showSnackbar(message = errorCellOccupied)
            return
        }
        val move = Move(row, col)
        val newBoard = board.applyMove(move, currentPiece())
        applyTurn(newBoard)
    }

    LaunchedEffect(board.serialize(), currentPlayerIsP1, player1Config.type, player2Config.type) {
        val type = currentPlayerType()
        if (type == PlayerType.HUMAN) return@LaunchedEffect
        if (board.checkWinner() != null || board.isFull()) return@LaunchedEffect

        isAiThinking = true
        try {
            delay(AI_MOVE_DELAY_MS)
            val piece = currentPiece()
            val move = chooseAiMove(type, board, piece)
            if (board.getCell(move.row, move.col) != null) return@LaunchedEffect
            val newBoard = board.applyMove(move, piece)
            applyTurn(newBoard)
        } finally {
            isAiThinking = false
        }
    }

    val lavender = TicTacToeColors.GameBoardBackground
    val lineColor = TicTacToeColors.GridLinePurple
    val filledCell = TicTacToeColors.FilledCellPurple

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(lavender)
    ) {
        val landscape = maxWidth > maxHeight
        val turnText = if (currentPiece() == PlayerPiece.X) {
            stringResource(id = R.string.game_turn_play_x, currentPlayerName())
        } else {
            stringResource(id = R.string.game_turn_play_o, currentPlayerName())
        }

        if (landscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TurnBanner(
                    text = turnText,
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                )
                BoardPanel(
                    board = board,
                    lineColor = lineColor,
                    lavender = lavender,
                    filledCell = filledCell,
                    onCellClick = ::onCellClicked,
                    modifier = Modifier
                        .weight(0.65f)
                        .widthIn(max = maxHeight - 32.dp)
                        .aspectRatio(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TurnBanner(
                    text = turnText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
                BoardPanel(
                    board = board,
                    lineColor = lineColor,
                    lavender = lavender,
                    filledCell = filledCell,
                    onCellClick = ::onCellClicked,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

/**
 * Shows whose turn it is using the current player name and piece (X or O).
 */
@Composable
private fun TurnBanner(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = TicTacToeColors.OnGameBoardText,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

/**
 * Renders the 3×3 grid with line-only dividers and tappable cells.
 */
@Composable
private fun BoardPanel(
    board: Board,
    lineColor: Color,
    lavender: Color,
    filledCell: Color,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (col in 0 until 3) {
                        val piece = board.getCell(row, col)
                        BoardCellView(
                            piece = piece,
                            lavender = lavender,
                            filledCell = filledCell,
                            onClick = { onCellClick(row, col) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val thirdW = w / 3f
            val thirdH = h / 3f
            val stroke = 6.dp.toPx()
            fun line(a: Offset, b: Offset) {
                drawLine(
                    color = lineColor,
                    start = a,
                    end = b,
                    strokeWidth = stroke
                )
            }
            line(Offset(thirdW, 0f), Offset(thirdW, h))
            line(Offset(2 * thirdW, 0f), Offset(2 * thirdW, h))
            line(Offset(0f, thirdH), Offset(w, thirdH))
            line(Offset(0f, 2 * thirdH), Offset(w, 2 * thirdH))
        }
    }
}

/**
 * One cell: empty blends with [lavender]; occupied uses [filledCell] and a white mark.
 */
@Composable
private fun BoardCellView(
    piece: PlayerPiece?,
    lavender: Color,
    filledCell: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (piece == null) lavender else filledCell
    Box(
        modifier = modifier
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (piece != null) {
            Text(
                text = if (piece == PlayerPiece.X) "X" else "O",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
