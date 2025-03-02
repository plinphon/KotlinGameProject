package com.motycka.edu.game.match.rest

import com.motycka.edu.game.match.model.MatchOutcome

data class MatchResponse(
    val challengerId: Int,
    val opponentId: Int,
    val matchOutcome: MatchOutcome,
    val challengerXp: Int,
    val opponentXp: Int,
)