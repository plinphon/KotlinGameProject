package com.motycka.edu.game.match.round

import com.motycka.edu.game.match.round.model.Round
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet


@Repository
class RoundRepository(private val jdbcTemplate: JdbcTemplate) {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Round(
            id = rs.getInt("id"),
            matchId = rs.getInt("match_id"),
            round = rs.getInt("round_number"),
            characterId = rs.getInt("character_id"),
            healthDelta = rs.getInt("health_delta"),
            staminaDelta = rs.getInt("stamina_delta"),
            manaDelta = rs.getInt("mana_delta"),
        )
    }

    fun insertRound(round: Round): Round? {
        jdbcTemplate.update(
            """
            INSERT INTO round (
                match_id, 
                round_number, 
                character_id, 
                health_delta, 
                stamina_delta, 
                mana_delta
            ) VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            round.matchId,
            round.round,
            round.characterId,
            round.healthDelta,
            round.staminaDelta,
            round.manaDelta
        )

        return getRoundByMatchAndNumberAndCharacter(
            round.matchId,
            round.round,
            round.characterId
        )
    }

    fun getRoundByMatchAndNumberAndCharacter(
        matchId: Int,
        round: Int,
        characterId: Int
    ): Round? {
        return jdbcTemplate.query(
            """
            SELECT * FROM round
            WHERE match_id = ? 
            AND round_number = ? 
            AND character_id = ?
            LIMIT 1
            """.trimIndent(),
            rowMapper,
            matchId,
            round,
            characterId
        ).firstOrNull()
    }

    fun getRoundsByMatchId(matchId: Int): List<Round> {
        return jdbcTemplate.query(
            """
            SELECT * FROM round
            WHERE match_id = ?
            ORDER BY round_number ASC, character_id ASC
            """.trimIndent(),
            rowMapper,
            matchId
        )
    }

    fun getRoundsByCharacterId(characterId: Int): List<Round> {
        return jdbcTemplate.query(
            """
            SELECT * FROM round
            WHERE character_id = ?
            ORDER BY match_id ASC, round_number ASC
            """.trimIndent(),
            rowMapper,
            characterId
        )
    }

    fun deleteRoundsByMatchId(matchId: Int): Int {
        return jdbcTemplate.update(
            """
            DELETE FROM round
            WHERE match_id = ?
            """.trimIndent(),
            matchId
        )
    }
}