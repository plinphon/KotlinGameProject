package com.motycka.edu.game.match.round

import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.character.model.GameCharacter
import com.motycka.edu.game.match.round.model.Round
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


private val logger = KotlinLogging.logger {}

@Service
class RoundService(
    private val roundRepository: RoundRepository,
    private val characterService: CharacterService
) {

    private val random = Random()

    @Transactional
    fun createRound(
        matchId: Int,
        round: Int,
        characterId: Int,
        healthDelta: Int,
        staminaDelta: Int,
        manaDelta: Int
    ): Round {
        val character = characterService.getCharacterById(characterId)
            ?: throw IllegalArgumentException("Character with ID $characterId not found")

        logger.debug { "Creating round $round for match $matchId with character ${character.name}" }

        val round = Round(
            matchId = matchId,
            round = round,
            characterId = characterId,
            healthDelta = healthDelta,
            staminaDelta = staminaDelta,
            manaDelta = manaDelta
        )

        return roundRepository.insertRound(round)
            ?: throw IllegalStateException("Failed to create round")
    }

    @Transactional
    fun createMatchRound(
        matchId: Int,
        round: Int,
        challenger: GameCharacter,
        opponent: GameCharacter,
        challengerHealthDelta: Int,
        challengerStaminaDelta: Int,
        challengerManaDelta: Int,
        opponentHealthDelta: Int,
        opponentStaminaDelta: Int,
        opponentManaDelta: Int
    ): Pair<Round, Round> {
        // Create challenger round
        val challengerRound = createRound(
            matchId = matchId,
            round = round,
            characterId = challenger.id,
            healthDelta = challengerHealthDelta,
            staminaDelta = challengerStaminaDelta,
            manaDelta = challengerManaDelta
        )

        // Create opponent round
        val opponentRound = createRound(
            matchId = matchId,
            round = round,
            characterId = opponent.id,
            healthDelta = opponentHealthDelta,
            staminaDelta = opponentStaminaDelta,
            manaDelta = opponentManaDelta
        )

        return Pair(challengerRound, opponentRound)
    }

    fun simulateRound(challenger: GameCharacter, opponent: GameCharacter): Pair<RoundStats, RoundStats> {
        // Calculate attack with randomness
        val challengerAttack = calculateAttack(challenger)
        val opponentAttack = calculateAttack(opponent)

        // Calculate defense with randomness
        val challengerDefense = calculateDefense(challenger)
        val opponentDefense = calculateDefense(opponent)

        // Apply critical hit chance
        val challengerCritical = rollForCriticalHit(challenger)
        val opponentCritical = rollForCriticalHit(opponent)

        // Calculate final damage with randomness
        val damageToOpponent = calculateFinalDamage(challengerAttack, opponentDefense, challengerCritical)
        val damageToChallenger = calculateFinalDamage(opponentAttack, challengerDefense, opponentCritical)

        // Add randomness to resource consumption
        val challengerStaminaCost = 5 + random.nextInt(3) - 1 // 4-7 stamina cost
        val opponentStaminaCost = 5 + random.nextInt(3) - 1 // 4-7 stamina cost

        val challengerManaCost = if (challenger.characterClass.toString().equals("SORCERER", ignoreCase = true))
            8 + random.nextInt(5) - 2 // 6-11 mana cost for sorcerer
        else
            2 + random.nextInt(3) - 1 // 1-4 mana cost for others

        val opponentManaCost = if (opponent.characterClass.toString().equals("SORCERER", ignoreCase = true))
            8 + random.nextInt(5) - 2 // 6-11 mana cost for sorcerer
        else
            2 + random.nextInt(3) - 1 // 1-4 mana cost for others

        val challengerStats = RoundStats(
            healthDelta = -damageToChallenger,
            staminaDelta = -challengerStaminaCost,
            manaDelta = -challengerManaCost
        )

        val opponentStats = RoundStats(
            healthDelta = -damageToOpponent,
            staminaDelta = -opponentStaminaCost,
            manaDelta = -opponentManaCost
        )

        return Pair(challengerStats, opponentStats)
    }

    private fun calculateAttack(character: GameCharacter): Int {
        // Base attack calculation with randomness
        val baseAttack = when (character.characterClass.toString().uppercase()) {
            "WARRIOR" -> 12 + (character.attack / 10)
            "SORCERER" -> 8 + (character.attack / 12)
            else -> 10 + (character.attack / 15)
        }

        // Add random variation to attack (±15%)
        val randomFactor = 0.85 + (random.nextDouble() * 0.3) // Between 0.85 and 1.15
        return (baseAttack * randomFactor).toInt().coerceAtLeast(1)
    }

    private fun calculateDefense(character: GameCharacter): Int {
        // Base defense calculation with randomness
        val baseDefense = when (character.characterClass.toString().uppercase()) {
            "WARRIOR" -> 8 + (character.defense ?: 0) / 8
            "SORCERER" -> 4 + (character.defense ?: 0) / 15
            else -> 6 + (character.defense ?: 0) / 10
        }

        // Add random variation to defense (±20%)
        val randomFactor = 0.8 + (random.nextDouble() * 0.4) // Between 0.8 and 1.2
        return (baseDefense * randomFactor).toInt().coerceAtLeast(1)
    }

    private fun rollForCriticalHit(character: GameCharacter): Boolean {
        // Base critical chance depends on character class
        val baseCriticalChance = when (character.characterClass.toString().uppercase()) {
            "WARRIOR" -> 10 // 10% base chance
            "SORCERER" -> 15 // 15% base chance
            else -> 12 // 12% base chance
        }

        // Roll for critical hit
        return random.nextInt(100) < baseCriticalChance
    }

    private fun calculateFinalDamage(attackValue: Int, defenseValue: Int, isCritical: Boolean): Int {
        // Calculate base damage
        var damage = maxOf(1, attackValue - defenseValue)

        // Apply critical hit multiplier
        if (isCritical) {
            damage = (damage * 1.5).toInt()
        }

        // Add minor random variation to final damage (±2)
        damage += random.nextInt(5) - 2

        // Ensure minimum damage is 1
        return damage.coerceAtLeast(1)
    }

    /**
     * Get all rounds for a specific match
     */
    @Transactional(readOnly = true)
    fun getRoundsByMatchId(matchId: Int): List<Round> {
        return roundRepository.getRoundsByMatchId(matchId)
    }

    /**
     * Get all rounds for a specific character
     */
    @Transactional(readOnly = true)
    fun getRoundsByCharacterId(characterId: Int): List<Round> {
        return roundRepository.getRoundsByCharacterId(characterId)
    }

    /**
     * Delete all rounds for a match
     */
    @Transactional
    fun deleteRoundsByMatchId(matchId: Int): Int {
        return roundRepository.deleteRoundsByMatchId(matchId)
    }
}

data class RoundStats(
    val healthDelta: Int,
    val staminaDelta: Int,
    val manaDelta: Int
)