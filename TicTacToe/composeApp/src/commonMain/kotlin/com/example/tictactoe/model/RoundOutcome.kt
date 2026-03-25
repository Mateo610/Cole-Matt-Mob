package com.example.tictactoe.model

/**
 * Result of a finished round, used for navigation and the game-over screen.
 */
enum class RoundOutcome {
    /**
     * Player 1 (X) won the round.
     */
    PLAYER1_WIN,

    /**
     * Player 2 (O) won the round.
     */
    PLAYER2_WIN,

    /**
     * The board filled with no winner.
     */
    TIE
}
