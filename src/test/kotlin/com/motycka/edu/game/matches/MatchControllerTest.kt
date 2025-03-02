package com.motycka.edu.game.matches

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.match.MatchController
import com.motycka.edu.game.match.MatchService
import com.motycka.edu.game.match.model.EnhancedMatchResult
import com.motycka.edu.game.match.model.GameMatchResult
import com.motycka.edu.game.match.model.MatchCharacterData
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.rest.MatchCreateRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class MatchControllerTest {

    private val accountService: AccountService = mockk()

    private val matchService: MatchService = mockk()

    private lateinit var matchController: MatchController

    @BeforeEach
    fun setUp() {
        matchController = MatchController(accountService, matchService)
    }

    @Test
    fun `getMatches should return enhanced matches for current account`() {
        // Arrange
        val accountId = 1L
        val expectedMatches = listOf(
            EnhancedMatchResult(
                id = 1,
                challenger = MatchCharacterData(1, "Challenger", "Warrior", 20),
                opponent = MatchCharacterData(2, "Opponent", "Mage", 5),
                matchOutcome = "CHALLENGER_WON"
            )
        )

        every { accountService.getCurrentAccountId() } returns accountId
        every { matchService.getEnhancedMatchesByAccountId(accountId) } returns expectedMatches

        // Act
        val result = matchController.getMatches()

        // Assert
        assertEquals(expectedMatches, result)
        verify { accountService.getCurrentAccountId() }
        verify { matchService.getEnhancedMatchesByAccountId(accountId) }
    }

    @Test
    fun `postMatch should create and return enhanced match result`() {
        // Arrange
        val matchCreateRequest = MatchCreateRequest(
            challengerId = 1,
            opponentId = 2
        )

        val createdMatch = GameMatchResult(
            id = 1,
            challengerId = 1,
            opponentId = 2,
            matchOutcome = MatchOutcome.WIN,
            challengerXp = 20,
            opponentXp = 5
        )

        val enhancedMatchResult = EnhancedMatchResult(
            id = 1,
            challenger = MatchCharacterData(1, "Challenger", "Warrior", 20),
            opponent = MatchCharacterData(2, "Opponent", "Mage", 5),
            matchOutcome = "CHALLENGER_WON"
        )

        every { matchService.createMatchResult(1, 2) } returns createdMatch
        every { matchService.enhanceMatchResult(createdMatch) } returns enhancedMatchResult

        // Act
        val response = matchController.postMatch(matchCreateRequest)

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(enhancedMatchResult, response.body)
        verify { matchService.createMatchResult(1, 2) }
        verify { matchService.enhanceMatchResult(createdMatch) }
    }
}