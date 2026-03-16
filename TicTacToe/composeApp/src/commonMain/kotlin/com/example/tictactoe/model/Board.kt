package com.example.tictactoe.model

/**
 * Immutable representation of a Tic-Tac-Toe board.
 *
 * The board is always three rows by three columns and stores which player
 * has claimed each cell, if any.
 *
 * @property cells The flattened list of nine cells stored row by row.
 */
data class Board(
    val cells: List<PlayerPiece?>
) {

    init {
        require(cells.size == BOARD_SIZE * BOARD_SIZE) {
            "Board must contain exactly ${BOARD_SIZE * BOARD_SIZE} cells."
        }
    }

    /**
     * Returns the piece at the given row and column, or null when the cell is empty.
     *
     * @param row Zero-based row index.
     * @param col Zero-based column index.
     * @return The piece at the specified position or null for an empty cell.
     */
    fun getCell(row: Int, col: Int): PlayerPiece? {
        require(row in 0 until BOARD_SIZE && col in 0 until BOARD_SIZE) {
            "Row and column must be between 0 and ${BOARD_SIZE - 1}."
        }
        val index = row * BOARD_SIZE + col
        return cells[index]
    }

    /**
     * Applies the given move for the provided piece and returns a new board.
     *
     * @param move The move to apply.
     * @param piece The piece to place on the board.
     * @return A new board instance with the move applied.
     */
    fun applyMove(move: Move, piece: PlayerPiece): Board {
        val index = move.row * BOARD_SIZE + move.col
        require(index in cells.indices) {
            "Move position is outside of the board."
        }
        require(cells[index] == null) {
            "Cannot apply move to an occupied cell."
        }
        val updatedCells = cells.toMutableList()
        updatedCells[index] = piece
        return copy(cells = updatedCells.toList())
    }

    /**
     * Checks the board for a winning line and returns the winning piece when found.
     *
     * @return The winning [PlayerPiece] or null when there is no winner.
     */
    fun checkWinner(): PlayerPiece? {
        val lines: List<List<Int>> = listOf(
            // Rows
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            // Columns
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            // Diagonals
            listOf(0, 4, 8),
            listOf(2, 4, 6)
        )

        for (line in lines) {
            val a = cells[line[0]]
            val b = cells[line[1]]
            val c = cells[line[2]]
            if (a != null && a == b && b == c) {
                return a
            }
        }
        return null
    }

    /**
     * Determines whether the board is completely full.
     *
     * @return True if there are no empty cells, false otherwise.
     */
    fun isFull(): Boolean = cells.none { it == null }

    /**
     * Serializes the board into a nine-character string using the characters
     * 'X', 'O', and '-' for X pieces, O pieces, and empty cells respectively.
     *
     * @return A serialized representation of the board.
     */
    fun serialize(): String {
        val builder = StringBuilder(BOARD_SIZE * BOARD_SIZE)
        for (cell in cells) {
            val char = when (cell) {
                PlayerPiece.X -> 'X'
                PlayerPiece.O -> 'O'
                null -> '-'
            }
            builder.append(char)
        }
        return builder.toString()
    }

    companion object {

        /**
         * Creates an empty board with all cells initially unclaimed.
         *
         * @return A new empty [Board] instance.
         */
        fun empty(): Board = Board(List(BOARD_SIZE * BOARD_SIZE) { null })

        /**
         * Deserializes a board from the provided nine-character string.
         *
         * @param serialized Serialized board string containing exactly nine characters.
         * @return A [Board] instance that matches the serialized state.
         */
        fun deserialize(serialized: String): Board {
            require(serialized.length == BOARD_SIZE * BOARD_SIZE) {
                "Serialized board must contain exactly ${BOARD_SIZE * BOARD_SIZE} characters."
            }
            val cells: List<PlayerPiece?> = serialized.map { char ->
                when (char) {
                    'X' -> PlayerPiece.X
                    'O' -> PlayerPiece.O
                    '-' -> null
                    else -> error("Unexpected character '$char' in serialized board.")
                }
            }
            return Board(cells)
        }

        private const val BOARD_SIZE: Int = 3
    }
}

