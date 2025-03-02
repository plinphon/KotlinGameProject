package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.model.GameCharacter
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import com.motycka.edu.game.match.MatchService
import com.motycka.edu.game.match.model.EnhancedMatchResult
import com.motycka.edu.game.match.rest.MatchCreateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/leaderboards")
class LeaderBoardController(
    private val leaderBoardService: LeaderBoardService
) {
    @GetMapping
    fun getLeaderboard(
        @RequestParam(required = false) characterClass: String?
    ): List<LeaderBoardResponse> {
        return leaderBoardService.getLeaderBoards(characterClass)
    }
}