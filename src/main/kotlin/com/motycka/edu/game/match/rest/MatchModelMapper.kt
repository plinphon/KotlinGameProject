package com.motycka.edu.game.match.rest

import com.motycka.edu.game.match.model.GameMatchResult
import com.motycka.edu.game.match.model.MatchOutcome


fun MatchCreateRequest.toMatch(matchOutcome: MatchOutcome, challengerXp: Int, opponentXp : Int) = GameMatchResult(
    challengerId = this.challengerId,
    opponentId = this.opponentId,
    matchOutcome = matchOutcome,
    challengerXp = challengerXp,
    opponentXp = opponentXp
)

fun GameMatchResult.toMatchResponse() = MatchResponse(
    challengerId = this.challengerId,
    opponentId = this.opponentId,
    matchOutcome = this.matchOutcome,
    challengerXp = this.challengerXp,
    opponentXp = this.opponentXp
)