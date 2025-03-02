package com.motycka.edu.game.leaderboard.rest

import com.motycka.edu.game.character.model.GameCharacter

data class LeaderBoardResponse(
    val position: Int,
    val character: GameCharacter? = null,
    val wins: Int,
    val losses: Int,
    val draws: Int,
)
