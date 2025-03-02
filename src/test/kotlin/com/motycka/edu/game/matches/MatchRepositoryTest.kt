package com.motycka.edu.game.match

import com.motycka.edu.game.match.model.GameMatchResult
import com.motycka.edu.game.match.model.MatchOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

class MatchRepositoryTest {

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    @Mock
    private lateinit var resultSet: ResultSet

    private lateinit var matchRepository: MatchRepository

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        matchRepository = MatchRepository(jdbcTemplate)
    }

    @Test
    fun `selectAllMatches should return list of matches`() {
        // Arrange
        val expectedMatches = listOf(
            GameMatchResult(
                id = 1,
                challengerId = 1,
                opponentId = 2,
                matchOutcome = MatchOutcome.WIN,
                challengerXp = 20,
                opponentXp = 5
            )
        )

        Mockito.`when`(jdbcTemplate.query(any<String>(), any<RowMapper<GameMatchResult>>()))
            .thenReturn(expectedMatches)

        // Act
        val result = matchRepository.selectAllMatches()

        // Assert
        assertEquals(expectedMatches, result)
        verify(jdbcTemplate).query(
            eq("SELECT * FROM match;"),
            any<RowMapper<GameMatchResult>>()
        )
    }

    @Test
    fun `selectByCharacterId should return matches for given character`() {
        // Arrange
        val characterId = 1
        val expectedMatches = listOf(
            GameMatchResult(
                id = 1,
                challengerId = characterId,
                opponentId = 2,
                matchOutcome = MatchOutcome.WIN,
                challengerXp = 20,
                opponentXp = 5
            )
        )

        // Create a custom answer to return the expected matches
        Mockito.`when`(jdbcTemplate.query(
            Mockito.anyString(),
            Mockito.any(RowMapper::class.java),
            Mockito.anyInt(),
            Mockito.anyInt()
        )).thenReturn(expectedMatches)

        // Act
        val result = matchRepository.selectByCharacterId(characterId)

        // Assert
        assertNotNull(result)
        assertFalse(result.isEmpty())
        assertEquals(expectedMatches, result)
    }
    @Test
    fun `insertMatch should save match and return inserted match`() {
        // Arrange
        val gameMatch = GameMatchResult(
            challengerId = 1,
            opponentId = 2,
            matchOutcome = MatchOutcome.WIN,
            challengerXp = 20,
            opponentXp = 5
        )

        val insertedMatch = gameMatch.copy(id = 1)

        Mockito.`when`(jdbcTemplate.update(
            any<String>(),
            eq(1),
            eq(2),
            any(),
            eq(20),
            eq(5)
        )).thenReturn(1)

        Mockito.`when`(jdbcTemplate.query(
            any<String>(),
            any<RowMapper<GameMatchResult>>(),
            eq(1),
            eq(2)
        )).thenReturn(listOf(insertedMatch))

        // Act
        val result = matchRepository.insertMatch(gameMatch)

        // Assert
        assertNotNull(result)
        assertEquals(insertedMatch, result)
        verify(jdbcTemplate).update(
            Mockito.contains("INSERT INTO match"),
            eq(1),
            eq(2),
            any(),
            eq(20),
            eq(5)
        )
    }

    @Test
    fun `updateMatch should update match details`() {
        // Arrange
        val match = GameMatchResult(
            id = 1,
            challengerId = 1,
            opponentId = 2,
            matchOutcome = MatchOutcome.WIN,
            challengerXp = 20,
            opponentXp = 5
        )

        Mockito.`when`(jdbcTemplate.update(
            any<String>(),
            any(),
            eq(20),
            eq(5),
            eq(1)
        )).thenReturn(1)

        // Act
        matchRepository.updateMatch(match)

        // Assert
        verify(jdbcTemplate).update(
            Mockito.contains("UPDATE match SET"),
            Mockito.argThat { it == match.matchOutcome.toDbString() },
            eq(20),
            eq(5),
            eq(1)
        )
    }

    @Test
    fun `selectByAccountId should return matches for given account`() {
        // Arrange
        val accountId = 1L
        val expectedMatches = listOf(
            GameMatchResult(
                id = 1,
                challengerId = 1,
                opponentId = 2,
                matchOutcome = MatchOutcome.WIN,
                challengerXp = 20,
                opponentXp = 5
            )
        )

        Mockito.`when`(jdbcTemplate.query(
            any<String>(),
            any<RowMapper<GameMatchResult>>(),
            eq(accountId),
            eq(accountId)
        )).thenReturn(expectedMatches)

        // Act
        val result = matchRepository.selectByAccountId(accountId)

        // Assert
        assertEquals(expectedMatches, result)
        verify(jdbcTemplate).query(
            Mockito.contains("JOIN character c1 ON m.challenger_id = c1.id"),
            any<RowMapper<GameMatchResult>>(),
            eq(accountId),
            eq(accountId)
        )
    }
}