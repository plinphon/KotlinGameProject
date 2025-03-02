package com.motycka.edu.game.match

import com.motycka.edu.game.match.model.GameMatchResult
import com.motycka.edu.game.match.model.MatchOutcome
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

@Repository
class MatchRepository(
    private val jdbcTemplate: JdbcTemplate,
) {

    fun selectAllMatches(): List<GameMatchResult> {
        logger.debug { "Selecting all matches" }
        return jdbcTemplate.query(
            """
            SELECT * FROM match;
            """.trimIndent(),
            ::rowMapper
        )
    }

    fun selectByCharacterId(characterId: Int): List<GameMatchResult> {
        logger.debug { "Selecting matches for characterId=$characterId" }
        return jdbcTemplate.query(
            """
            SELECT * FROM match 
            WHERE challenger_id = ? OR opponent_id = ?;
            """.trimIndent(),
            ::rowMapper,
            characterId,
            characterId
        )
    }

    fun insertMatch(gameMatch: GameMatchResult): GameMatchResult? {
        logger.debug { "Inserting new match: ${gameMatch.challengerId} vs ${gameMatch.opponentId}" }

        // Insert the match
        jdbcTemplate.update(
            """
        INSERT INTO match (challenger_id, opponent_id, match_outcome, challenger_xp, opponent_xp)
        VALUES (?, ?, ?, ?, ?);
        """.trimIndent(),
            gameMatch.challengerId,
            gameMatch.opponentId,
            gameMatch.matchOutcome.toDbString(),
            gameMatch.challengerXp,
            gameMatch.opponentXp
        )

        // Retrieve the most recently inserted match for these players
        return jdbcTemplate.query(
            "SELECT * FROM match WHERE challenger_id = ? AND opponent_id = ? ORDER BY id DESC LIMIT 1;",
            ::rowMapper,  // Ensure you have a row mapper for GameMatchResult
            gameMatch.challengerId,
            gameMatch.opponentId
        ).firstOrNull()
    }

    fun updateMatch(match: GameMatchResult) {
        jdbcTemplate.update(
            """
        UPDATE match SET
            match_outcome = ?,
            challenger_xp = ?,
            opponent_xp = ?
        WHERE id = ?
        """.trimIndent(),
            match.matchOutcome.toDbString(),
            match.challengerXp,
            match.opponentXp,
            match.id
        )
    }

    fun selectByAccountId(accountId: Long): List<GameMatchResult> {
        logger.debug { "Selecting matches for accountId=$accountId" }
        return jdbcTemplate.query(
            """
        SELECT m.* FROM match m
        JOIN character c1 ON m.challenger_id = c1.id
        JOIN character c2 ON m.opponent_id = c2.id
        WHERE c1.account_id = ? OR c2.account_id = ?;
        """.trimIndent(),
            ::rowMapper,
            accountId,
            accountId
        )
    }
    // Define rowMapper function
    @Throws(SQLException::class)
    private fun rowMapper(rs: ResultSet, i: Int): GameMatchResult {
        val matchOutcomeString = rs.getString("match_outcome")

        val matchOutcome = MatchOutcome.fromString(matchOutcomeString)

        return GameMatchResult(
            id = rs.getInt("id"),
            challengerId = rs.getInt("challenger_id"),
            opponentId = rs.getInt("opponent_id"),
            matchOutcome = matchOutcome,
            challengerXp = rs.getInt("challenger_xp"),
            opponentXp = rs.getInt("opponent_xp")
        )
    }
}
