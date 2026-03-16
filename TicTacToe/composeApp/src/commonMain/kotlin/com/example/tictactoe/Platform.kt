package com.example.tictactoe

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform