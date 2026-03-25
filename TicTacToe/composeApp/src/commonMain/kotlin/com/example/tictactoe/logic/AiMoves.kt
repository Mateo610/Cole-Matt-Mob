package com.example.tictactoe.logic

import com.example.tictactoe.model.Board
import com.example.tictactoe.model.Move
import com.example.tictactoe.model.PlayerPiece
import com.example.tictactoe.model.PlayerType

/**
 * Selects a legal move for the given AI [PlayerType].
 *
 * Human players do not use this function; the UI supplies their moves.
 *
 * @param type The AI difficulty level.
 * @param board The current board state.
 * @param myPiece The piece this AI is placing ([PlayerPiece.X] or [PlayerPiece.O]).
 * @return A valid empty-cell move.
 */
fun chooseAiMove(type: PlayerType, board: Board, myPiece: PlayerPiece): Move {
    require(type != PlayerType.HUMAN) { "chooseAiMove is only for AI player types." }
    return when (type) {
        PlayerType.HUMAN -> error("Unreachable")
        PlayerType.EASY_AI -> randomMove(board)
        PlayerType.MEDIUM_AI -> mediumMove(board, myPiece)
        PlayerType.HARD_AI -> minimaxBestMove(board, myPiece)
    }
}

private fun randomMove(board: Board): Move {
    val empties = emptyIndices(board)
    require(empties.isNotEmpty()) { "No empty cells." }
    val idx = empties.random()
    return Move(idx / 3, idx % 3)
}

private fun mediumMove(board: Board, myPiece: PlayerPiece): Move {
    val opp = opponent(myPiece)
    winningMove(board, myPiece)?.let { return it }
    winningMove(board, opp)?.let { return it }
    val center = Move(1, 1)
    if (board.getCell(1, 1) == null) return center
    val corners = listOf(Move(0, 0), Move(0, 2), Move(2, 0), Move(2, 2))
    val freeCorners = corners.filter { board.getCell(it.row, it.col) == null }
    if (freeCorners.isNotEmpty()) return freeCorners.random()
    return randomMove(board)
}

private fun winningMove(board: Board, piece: PlayerPiece): Move? {
    for (idx in 0 until 9) {
        val r = idx / 3
        val c = idx % 3
        if (board.getCell(r, c) != null) continue
        val next = board.applyMove(Move(r, c), piece)
        if (next.checkWinner() == piece) return Move(r, c)
    }
    return null
}

private fun minimaxBestMove(board: Board, myPiece: PlayerPiece): Move {
    val empties = emptyIndices(board)
    require(empties.isNotEmpty()) { "No empty cells." }
    var bestScore = Int.MIN_VALUE
    var best: Move? = null
    val opp = opponent(myPiece)
    for (idx in empties) {
        val r = idx / 3
        val c = idx % 3
        val move = Move(r, c)
        val next = board.applyMove(move, myPiece)
        val score = minimax(next, false, myPiece, opp, Int.MIN_VALUE, Int.MAX_VALUE)
        if (score > bestScore) {
            bestScore = score
            best = move
        }
    }
    return best ?: randomMove(board)
}

private fun minimax(
    board: Board,
    isMaximizing: Boolean,
    aiPiece: PlayerPiece,
    humanPiece: PlayerPiece,
    alpha: Int,
    beta: Int
): Int {
    val winner = board.checkWinner()
    if (winner == aiPiece) return 10
    if (winner == humanPiece) return -10
    if (board.isFull()) return 0

    var a = alpha
    var b = beta

    if (isMaximizing) {
        var best = Int.MIN_VALUE
        for (idx in emptyIndices(board)) {
            val r = idx / 3
            val c = idx % 3
            val next = board.applyMove(Move(r, c), aiPiece)
            val s = minimax(next, false, aiPiece, humanPiece, a, b)
            best = maxOf(best, s)
            a = maxOf(a, best)
            if (b <= a) break
        }
        return best
    } else {
        var best = Int.MAX_VALUE
        for (idx in emptyIndices(board)) {
            val r = idx / 3
            val c = idx % 3
            val next = board.applyMove(Move(r, c), humanPiece)
            val s = minimax(next, true, aiPiece, humanPiece, a, b)
            best = minOf(best, s)
            b = minOf(b, best)
            if (b <= a) break
        }
        return best
    }
}

private fun emptyIndices(board: Board): List<Int> {
    val list = ArrayList<Int>(9)
    for (i in 0 until 9) {
        if (board.getCell(i / 3, i % 3) == null) list.add(i)
    }
    return list
}

private fun opponent(piece: PlayerPiece): PlayerPiece =
    if (piece == PlayerPiece.X) PlayerPiece.O else PlayerPiece.X
