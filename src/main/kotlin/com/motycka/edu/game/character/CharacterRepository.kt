package com.motycka.edu.game.character

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.model.GameCharacter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

@Repository
class CharacterRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun selectByAccountId(accountId: AccountId): List<GameCharacter> {
        logger.debug { "Selecting characters for accountId: $accountId" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE account_id = ?;",
            ::rowMapper,
            accountId
        )
    }

    fun selectAll(): List<GameCharacter> {
        logger.debug { "Selecting all characters" }
        return jdbcTemplate.query(
            "SELECT * FROM character;",
            ::rowMapper
        )
    }

    fun selectByCharacterName(name: String): GameCharacter? {
        logger.debug { "Selecting $name ***" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE name = ?;",
            ::rowMapper,
            name
        ).firstOrNull()
    }

    fun selectByCharacterId(id: Int): GameCharacter? {
        logger.debug { "Selecting character with id $id" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE id = ?;",
            ::rowMapper,
            id
        ).firstOrNull()
    }

    fun insertCharacter(character: GameCharacter): GameCharacter? {
        logger.debug { "Inserting new character: ${character.name}" }

        // First, insert the character
        jdbcTemplate.update(
            """
            INSERT INTO character (account_id, name, health, attack, experience, class, mana, healing, stamina, defense)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """.trimIndent(),
            character.accountId,
            character.name,
            character.health,
            character.attack,
            character.experience,
            character.characterClass,
            character.mana ?: 0,
            character.healing ?: 0,
            character.stamina ?: 0,
            character.defense ?: 0
        )

        // Then, retrieve the inserted character
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE name = ? AND account_id = ? ORDER BY id DESC LIMIT 1;",
            ::rowMapper,
            character.name,
            character.accountId
        ).firstOrNull()
    }

    fun updateCharacter(character: GameCharacter): GameCharacter? {
        logger.debug { "Updating character with id ${character.id}: ${character.name}" }

        // Update the character record
        val rowsAffected = jdbcTemplate.update(
            """
            UPDATE character 
            SET name = ?, 
                health = ?, 
                attack = ?, 
                experience = ?, 
                class = ?, 
                mana = ?, 
                healing = ?, 
                stamina = ?, 
                defense = ?
            WHERE id = ?;
            """.trimIndent(),
            character.name,
            character.health,
            character.attack,
            character.experience,
            character.characterClass,
            character.mana ?: 0,
            character.healing ?: 0,
            character.stamina ?: 0,
            character.defense ?: 0,
            character.id
        )

        // Check if update was successful
        if (rowsAffected == 0) {
            logger.warn { "Character update failed for id ${character.id}" }
            return null
        }

        // Retrieve and return the updated character
        return selectByCharacterId(character.id)
    }

    // Define rowMapper function
    private fun rowMapper(rs: ResultSet, rowNum: Int): GameCharacter {
        return GameCharacter(
            id = rs.getInt("id"),
            accountId = rs.getLong("account_id"),
            name = rs.getString("name"),
            characterClass = rs.getString("class"),
            health = rs.getInt("health"),
            attack = rs.getInt("attack"),
            experience = rs.getInt("experience"),
            defense = rs.getInt("defense").takeIf { !rs.wasNull() },
            stamina = rs.getInt("stamina").takeIf { !rs.wasNull() },
            healing = rs.getInt("healing").takeIf { !rs.wasNull() },
            mana = rs.getInt("mana").takeIf { !rs.wasNull() }
        )
    }

}