package com.motycka.edu.game.leaderboard.model

data class LeaderBoard(
    val characterId: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
)
