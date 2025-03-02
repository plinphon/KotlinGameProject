package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
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