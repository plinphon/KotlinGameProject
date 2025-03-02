package com.motycka.edu.game.matches

import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.character.model.GameCharacter
import com.motycka.edu.game.leaderboard.LeaderBoardService
import com.motycka.edu.game.match.MatchRepository
import com.motycka.edu.game.match.MatchService
import com.motycka.edu.game.match.model.GameMatchResult
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.round.RoundService

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.Test
import kotlin.test.assertTrue


class MatchServiceTest {

    @Mock
    private lateinit var characterService: CharacterService

    @Mock
    private lateinit var matchRepository: MatchRepository

    @Mock
    private lateinit var roundService: RoundService

    @Mock
    private lateinit var leaderBoardService: LeaderBoardService

    private lateinit var matchService: MatchService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        matchService = MatchService(
            characterService,
            matchRepository,
            roundService,
            leaderBoardService
        )
    }

    @Test
    fun `formatMatchResult should correctly format match outcomes`() {
        // Arrange
        val challengerId = 1
        val opponentId = 2

        `when`(characterService.getCharacterById(challengerId)).thenReturn(
            GameCharacter(
                id = 1,
                name = "Challenger",
                health = 100,
                attack = 50,
                accountId = 1L,
                experience = 0,
                characterClass = "Warrior",
                stamina = 100,
                mana = 100
            )
        )
        `when`(characterService.getCharacterById(opponentId)).thenReturn(
            GameCharacter(
                id = 2,
                name = "Opponent",
                health = 100,
                attack = 50,
                accountId = 2L,
                experience = 0,
                characterClass = "Mage",
                stamina = 100,
                mana = 100
            )
        )

        // Test WIN scenario
        val winMatchResult = GameMatchResult(
            challengerId = challengerId,
            opponentId = opponentId,
            matchOutcome = MatchOutcome.WIN,
            challengerXp = 0,
            opponentXp = 0
        )
        assertEquals(
            "Challenger vs Opponent, Winner: Challenger",
            matchService.formatMatchResult(winMatchResult)
        )

        // Test LOSS scenario
        val lossMatchResult = GameMatchResult(
            challengerId = challengerId,
            opponentId = opponentId,
            matchOutcome = MatchOutcome.LOSS,
            challengerXp = 0,
            opponentXp = 0
        )
        assertEquals(
            "Challenger vs Opponent, Winner: Opponent",
            matchService.formatMatchResult(lossMatchResult)
        )

        // Test DRAW scenario
        val drawMatchResult = GameMatchResult(
            challengerId = challengerId,
            opponentId = opponentId,
            matchOutcome = MatchOutcome.DRAW,
            challengerXp = 0,
            opponentXp = 0
        )
        assertEquals(
            "Challenger vs Opponent, Result: Draw",
            matchService.formatMatchResult(drawMatchResult)
        )
    }

    @Test
    fun `getMatchesByAccountId should return matches for given account`() {
        // Arrange
        val accountId = 1L
        val expectedMatches = listOf(
            GameMatchResult(
                challengerId = 1,
                opponentId = 2,
                matchOutcome = MatchOutcome.WIN,
                challengerXp = 20,
                opponentXp = 5
            )
        )

        `when`(matchRepository.selectByAccountId(accountId)).thenReturn(expectedMatches)

        // Act
        val result = matchService.getMatchesByAccountId(accountId)

        // Assert
        assertEquals(expectedMatches, result)
        verify(matchRepository).selectByAccountId(accountId)
    }

    @Test
    fun `enhanceMatchResult should throw exception for non-existent characters`() {
        // Arrange
        val match = GameMatchResult(
            id = 1,
            challengerId = 1,
            opponentId = 2,
            matchOutcome = MatchOutcome.WIN,
            challengerXp = 20,
            opponentXp = 5
        )

        `when`(characterService.getCharacterById(1)).thenReturn(null)
        `when`(characterService.getCharacterById(2)).thenReturn(null)

        // Assert & Act
        assertThrows(IllegalStateException::class.java) {
            matchService.enhanceMatchResult(match)
        }
    }

    @Test
    fun `calculateXp via reflection should return correct XP values`() {
        // Create test characters with different stats
        val winner = GameCharacter(
            id = 1,
            name = "Winner",
            health = 100,
            attack = 50,
            accountId = 1L,
            experience = 0,
            characterClass = "Warrior",
            stamina = 100,
            mana = 100
        )
        val loser = GameCharacter(
            id = 2,
            name = "Loser",
            health = 50,
            attack = 20,
            accountId = 2L,
            experience = 0,
            characterClass = "Mage",
            stamina = 100,
            mana = 100
        )

        // Use reflection to access private method
        val calculateXpMethod = MatchService::class.java.getDeclaredMethod(
            "calculateXp",
            GameCharacter::class.java,
            GameCharacter::class.java
        )
        calculateXpMethod.isAccessible = true

        // Invoke the private method
        val xp = calculateXpMethod.invoke(matchService, winner, loser) as Int

        // Assert XP calculation
        assertTrue(xp >= 5, "XP should be at least 5")
        assertTrue(xp <= 40, "XP should not exceed a reasonable maximum")
    }

    @Test
    fun `formatMatchResult should handle unknown characters gracefully`() {
        // Arrange
        val challengerId = 999
        val opponentId = 1000

        `when`(characterService.getCharacterById(challengerId)).thenReturn(null)
        `when`(characterService.getCharacterById(opponentId)).thenReturn(null)

        val matchResult = GameMatchResult(
            challengerId = challengerId,
            opponentId = opponentId,
            matchOutcome = MatchOutcome.WIN,
            challengerXp = 0,
            opponentXp = 0
        )

        // Act
        val formattedResult = matchService.formatMatchResult(matchResult)

        // Assert
        assertEquals(
            "Unknown Challenger vs Unknown Opponent, Winner: Unknown Challenger",
            formattedResult
        )
    }

    @Test
    fun `getMatchesByCharacterId should return correct matches`() {
        // Arrange
        val characterId = 1
        val expectedMatches = listOf(
            GameMatchResult(
                challengerId = characterId,
                opponentId = 2,
                matchOutcome = MatchOutcome.WIN,
                challengerXp = 20,
                opponentXp = 5
            )
        )

        `when`(matchRepository.selectByCharacterId(characterId)).thenReturn(expectedMatches)

        // Act
        val result = matchService.getMatchesByCharacterId(characterId)

        // Assert
        assertEquals(expectedMatches, result)
        verify(matchRepository).selectByCharacterId(characterId)
    }
}