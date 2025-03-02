package com.motycka.edu.game.match.rest

import com.motycka.edu.game.character.model.GameCharacter

data class MatchCreateRequest(
    val challengerId: Int,
    val opponentId: Int,
)
