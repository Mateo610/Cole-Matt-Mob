package com.example.tictactoe.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

private const val AI_MOVE_DELAY_MS: Long = 650L

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
                val outcome = if (winner == PlayerPiece.X) RoundOutcome.PLAYER1_WIN else RoundOutcome.PLAYER2_WIN
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
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        val maxBoardSize = (maxHeight - 96.dp).coerceAtLeast(0.dp)
        val boardSize = minOf(maxWidth * 0.92f, maxBoardSize)

        val turnText = if (currentPiece() == PlayerPiece.X) {
            stringResource(id = R.string.game_turn_play_x, currentPlayerName())
        } else {
            stringResource(id = R.string.game_turn_play_o, currentPlayerName())
        }

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
                    .padding(bottom = 12.dp)
            )

            BoardPanel(
                board = board,
                lineColor = lineColor,
                lavender = lavender,
                filledCell = filledCell,
                onCellClick = ::onCellClicked,
                modifier = Modifier.size(boardSize)
            )
        }
    }
}

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
                drawLine(color = lineColor, start = a, end = b, strokeWidth = stroke)
            }
            line(Offset(thirdW, 0f), Offset(thirdW, h))
            line(Offset(2 * thirdW, 0f), Offset(2 * thirdW, h))
            line(Offset(0f, thirdH), Offset(w, thirdH))
            line(Offset(0f, 2 * thirdH), Offset(w, 2 * thirdH))
        }
    }
}

@Composable
private fun BoardCellView(
    piece: PlayerPiece?,
    lavender: Color,
    filledCell: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (piece == null) lavender else filledCell

    // Animates from 0 → 1 when a piece is placed, driving sparkle spread and fade
    val sparkleProgress = remember { Animatable(0f) }

    LaunchedEffect(piece) {
        if (piece != null) {
            sparkleProgress.snapTo(0f)
            sparkleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = EaseOut)
            )
        }
    }

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

            // Sparkle overlay: radiating rays that spread out and fade
            val progress = sparkleProgress.value
            if (progress < 1f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxRadius = size.minDimension * 0.45f
                    val alpha = (1f - progress).coerceIn(0f, 1f)
                    val numRays = 8

                    // Primary rays
                    for (i in 0 until numRays) {
                        val angle = (2.0 * PI * i / numRays).toFloat()
                        val innerR = maxRadius * 0.15f + maxRadius * 0.2f * progress
                        val outerR = maxRadius * 0.3f + maxRadius * 0.5f * progress

                        drawLine(
                            color = Color.White.copy(alpha = alpha),
                            start = Offset(cx + innerR * cos(angle), cy + innerR * sin(angle)),
                            end = Offset(cx + outerR * cos(angle), cy + outerR * sin(angle)),
                            strokeWidth = 3.dp.toPx()
                        )

                        // Dot at tip of each primary ray
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.8f),
                            radius = 3.dp.toPx(),
                            center = Offset(cx + outerR * cos(angle), cy + outerR * sin(angle))
                        )
                    }

                    // Secondary shorter rays between primary rays
                    for (i in 0 until numRays) {
                        val angle = (2.0 * PI * i / numRays + PI / numRays).toFloat()
                        val outerR = maxRadius * 0.15f + maxRadius * 0.25f * progress

                        drawLine(
                            color = Color.White.copy(alpha = alpha * 0.6f),
                            start = Offset(cx + maxRadius * 0.1f * cos(angle), cy + maxRadius * 0.1f * sin(angle)),
                            end = Offset(cx + outerR * cos(angle), cy + outerR * sin(angle)),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}