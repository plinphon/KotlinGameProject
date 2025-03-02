package com.motycka.edu.game.match.model

import com.motycka.edu.game.match.round.model.Round

data class GameMatchResult(
    val id: Int = 0,
    val challengerId: Int,
    val opponentId: Int,
    val matchOutcome: MatchOutcome,
    val challengerXp: Int,
    val opponentXp: Int,
)

data class MatchCharacterData(
    val id: Int,
    val name: String,
    val characterClass: String,
    val experienceGained: Int
)

data class EnhancedMatchResult(
    val id: Int,
    val challenger: MatchCharacterData,
    val opponent: MatchCharacterData,
    val matchOutcome: String,
    val rounds: List<Round> = emptyList()
)
