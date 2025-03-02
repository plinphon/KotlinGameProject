package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.leaderboard.model.LeaderBoard
import com.motycka.edu.game.leaderboard.model.LeaderBoardUpdate
import com.motycka.edu.game.match.model.MatchOutcome
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository


private val logger = KotlinLogging.logger {}

@Repository
class LeaderBoardRepository(private val jdbcTemplate: JdbcTemplate) {
    fun updateLeaderBoard(leaderBoardUpdate: LeaderBoardUpdate) {
        // Check if character exists in leaderboard
        val exists = characterExistsInLeaderboard(leaderBoardUpdate.characterId)

        if (exists) {
            // Update existing record
            updateCharacterStats(leaderBoardUpdate)
        } else {
            // Insert new record
            insertCharacterStats(leaderBoardUpdate)
        }
    }

    private fun characterExistsInLeaderboard(characterId: Int): Boolean {
        val count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM leaderboard WHERE character_id = ?",
            Int::class.java,
            characterId
        )
        return count != null && count > 0
    }

    private fun updateCharacterStats(update: LeaderBoardUpdate) {
        logger.info { "Updating leaderboards" }

        val sql = when (update.matchOutcome) {
            MatchOutcome.WIN -> """
                UPDATE leaderboard 
                SET wins = wins + 1
                WHERE character_id = ?
            """
            MatchOutcome.LOSS -> """
                UPDATE leaderboard 
                SET losses = losses + 1
                WHERE character_id = ?
            """
            MatchOutcome.DRAW -> """
                UPDATE leaderboard 
                SET draws = draws + 1
                WHERE character_id = ?
            """
        }

        jdbcTemplate.update(sql, update.characterId)
    }

    private fun insertCharacterStats(update: LeaderBoardUpdate) {
        logger.info { "Updating leaderboards" }

        val wins = if (update.matchOutcome == MatchOutcome.WIN) 1 else 0
        val losses = if (update.matchOutcome == MatchOutcome.LOSS) 1 else 0
        val draws = if (update.matchOutcome == MatchOutcome.DRAW) 1 else 0

        jdbcTemplate.update("""
            INSERT INTO leaderboard (character_id, wins, losses, draws)
            VALUES (?, ?, ?, ?)
        """, update.characterId, wins, losses, draws)
    }

    fun getLeaderBoards(
        characterClass: String? = null,
    ): List<LeaderBoard> {
        logger.info { "Querying leaderboards" }

        val baseQuery = """
        SELECT l.character_id, l.wins, l.losses, l.draws
        FROM leaderboard l
    """

        val whereClause = if (characterClass != null) {
            """
        JOIN character c ON l.character_id = c.id
        WHERE c.class = ?
        """
        } else {
            ""
        }

        val orderAndLimit = """
        ORDER BY l.wins DESC
    """

        val fullQuery = baseQuery + whereClause + orderAndLimit

        return if (characterClass != null) {
            jdbcTemplate.query(fullQuery, { rs, _ ->
                LeaderBoard(
                    characterId = rs.getInt("character_id"),
                    wins = rs.getInt("wins"),
                    losses = rs.getInt("losses"),
                    draws = rs.getInt("draws")
                )
            }, characterClass)
        } else {
            jdbcTemplate.query(fullQuery, { rs, _ ->
                LeaderBoard(
                    characterId = rs.getInt("character_id"),
                    wins = rs.getInt("wins"),
                    losses = rs.getInt("losses"),
                    draws = rs.getInt("draws")
                )
            })
        }
    }

    fun getCharacterStats(characterId: Int): LeaderBoard? {
        val results = jdbcTemplate.query("""
            SELECT character_id, wins, losses, draws
            FROM leaderboard
            WHERE character_id = ?
        """, { rs, _ ->
            LeaderBoard(
                characterId = rs.getInt("character_id"),
                wins = rs.getInt("wins"),
                losses = rs.getInt("losses"),
                draws = rs.getInt("draws")
            )
        }, characterId)

        return results.firstOrNull()
    }
}