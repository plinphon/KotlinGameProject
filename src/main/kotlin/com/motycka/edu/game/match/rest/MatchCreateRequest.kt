package com.motycka.edu.game.match.rest

data class MatchCreateRequest(
    val challengerId: Int,
    val opponentId: Int,
)
