package com.example.tictactoe.model

/**
 * Configuration data for a player in the Tic-Tac-Toe game.
 *
 * @property name The display name for the player.
 * @property type The type of player, such as human or one of the AI levels.
 */
data class PlayerConfig(
    val name: String,
    val type: PlayerType
)

