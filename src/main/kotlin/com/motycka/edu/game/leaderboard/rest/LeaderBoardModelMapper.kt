package com.motycka.edu.game.leaderboard.rest

import com.motycka.edu.game.character.model.GameCharacter
import com.motycka.edu.game.leaderboard.model.LeaderBoard

fun LeaderBoard.toLeaderBoardResponse(position: Int, character: GameCharacter?) = LeaderBoardResponse (
    position = position,
    character = character,
    wins = this.wins,
    losses = this.losses,
    draws = this.draws,
)