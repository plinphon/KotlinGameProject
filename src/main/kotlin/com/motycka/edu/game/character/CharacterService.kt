package com.motycka.edu.game.character

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.model.GameCharacter
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class CharacterService(
    private val characterRepository: CharacterRepository,
) {

    fun getCharactersByAccountId(accountId: AccountId): List<GameCharacter> {
        logger.debug { "Getting character by AccountId: $accountId" }
        return characterRepository.selectByAccountId(accountId)
    }

    fun getCharacters(): List<GameCharacter> {
        logger.debug { "Getting character" }
        return characterRepository.selectAll()
    }

    fun getCharacterById(Id : Int): GameCharacter? {
        logger.debug { "Getting character by Id" }
        return characterRepository.selectByCharacterId(Id)
    }

    fun createCharacter(character: GameCharacter): GameCharacter {
        logger.debug { "Creating new character: ${character.name}" }
        return characterRepository.insertCharacter(character)
            ?: error(CREATE_ERROR)
    }

    @Transactional
    fun addExperience(characterId: Int, experiencePoints: Int): GameCharacter {
        logger.debug { "Adding $experiencePoints XP to character $characterId" }

        // Get the current character
        val character = characterRepository.selectByCharacterId(characterId)
            ?: throw IllegalArgumentException("Character with ID $characterId not found")

        // Calculate new total XP
        val newTotalXp = character.experience + experiencePoints

        // Just update XP without changing level or stats
        val updatedCharacter = character.copy(experience = newTotalXp)

        // Save updated character to database
        return characterRepository.updateCharacter(updatedCharacter)
            ?: throw IllegalStateException("Failed to update character XP")
    }

    companion object {
        const val CREATE_ERROR = "Character could not be created."
    }
}
