package com.motycka.edu.game.leaderboard.model

import com.motycka.edu.game.match.model.MatchOutcome

data class LeaderBoardUpdate(
    val characterId: Int,
    val matchOutcome: MatchOutcome,
)
