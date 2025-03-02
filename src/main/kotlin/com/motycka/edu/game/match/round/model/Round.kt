package com.motycka.edu.game.match.round.model

data class Round(
    val id: Int? = null,
    val matchId: Int,
    val round: Int,
    val characterId: Int,
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int,
)
