package com.example.tictactoe.model

/**
 * Represents a move on the Tic-Tac-Toe board.
 *
 * @property row The zero-based row index for the move.
 * @property col The zero-based column index for the move.
 */
data class Move(
    val row: Int,
    val col: Int
)

