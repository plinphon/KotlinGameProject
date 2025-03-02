package com.motycka.edu.game.match

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.match.model.EnhancedMatchResult
import com.motycka.edu.game.match.rest.MatchCreateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/matches")
class MatchController(
    private val accountService: AccountService,
    private val matchService: MatchService
) {

    @GetMapping
    fun getMatches(): List<EnhancedMatchResult> {
        val accountId = accountService.getCurrentAccountId()
        return matchService.getEnhancedMatchesByAccountId(accountId)
    }

    @PostMapping
    fun postMatch(
        @RequestBody matchCreateRequest: MatchCreateRequest
    ): ResponseEntity<EnhancedMatchResult> {
        val challengerId = matchCreateRequest.challengerId
        val opponentId = matchCreateRequest.opponentId

        val createdMatch = matchService.createMatchResult(challengerId, opponentId)
        val enhancedMatch = matchService.enhanceMatchResult(createdMatch)

        return ResponseEntity.status(HttpStatus.CREATED).body(enhancedMatch)
    }
}