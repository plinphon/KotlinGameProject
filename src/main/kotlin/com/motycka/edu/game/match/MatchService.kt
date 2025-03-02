package com.motycka.edu.game.match

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.character.model.GameCharacter
import com.motycka.edu.game.leaderboard.LeaderBoardService
import com.motycka.edu.game.leaderboard.model.LeaderBoardUpdate
import com.motycka.edu.game.match.model.EnhancedMatchResult
import com.motycka.edu.game.match.model.GameMatchResult
import com.motycka.edu.game.match.model.MatchCharacterData
import com.motycka.edu.game.match.model.MatchOutcome
import com.motycka.edu.game.match.round.RoundService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class MatchService(
    private val characterService: CharacterService,
    private val matchRepository: MatchRepository,
    private val roundService: RoundService,
    private val leaderBoardService: LeaderBoardService,
) {
    fun formatMatchResult(matchResult: GameMatchResult): String {
        val challengerName = characterService.getCharacterById(matchResult.challengerId)?.name ?: "Unknown Challenger"
        val opponentName = characterService.getCharacterById(matchResult.opponentId)?.name ?: "Unknown Opponent"

        return when (matchResult.matchOutcome) {
            MatchOutcome.WIN -> "$challengerName vs $opponentName, Winner: $challengerName"
            MatchOutcome.LOSS -> "$challengerName vs $opponentName, Winner: $opponentName"
            MatchOutcome.DRAW -> "$challengerName vs $opponentName, Result: Draw"
        }
    }

    fun getMatchesByAccountId(accountId: AccountId): List<GameMatchResult> {
        logger.debug { "Getting matches by AccountId: $accountId" }
        return matchRepository.selectByAccountId(accountId)
    }

    fun getMatchesByCharacterId(characterId: Int): List<GameMatchResult> {
        logger.debug { "Getting matches by CharacterId: $characterId" }
        return matchRepository.selectByCharacterId(characterId)
    }

    fun getAllMatches(): List<GameMatchResult> {
        logger.debug { "Getting all matches" }
        return matchRepository.selectAllMatches()
    }

    @Transactional
    fun createMatchResult(challengerId: Int, opponentId: Int): GameMatchResult {
        logger.debug { "Creating match result for challenger $challengerId vs opponent $opponentId" }

        val initialMatch = GameMatchResult(
            challengerId = challengerId,
            opponentId = opponentId,
            matchOutcome = MatchOutcome.DRAW,
            challengerXp = 0,
            opponentXp = 0
        )

        val savedMatch = matchRepository.insertMatch(initialMatch)
            ?: throw IllegalStateException("Failed to create initial match")

        val challenger = characterService.getCharacterById(challengerId)
            ?: throw IllegalArgumentException("Challenger with ID $challengerId not found")

        val opponent = characterService.getCharacterById(opponentId)
            ?: throw IllegalArgumentException("Opponent with ID $opponentId not found")

        val matchOutcome = simulateMatch(
            challenger = challenger,
            opponent = opponent,
            matchId = savedMatch.id
        )


        val (challengerXp, opponentXp) = when (matchOutcome) {
            MatchOutcome.WIN -> {
                Pair(calculateXp(challenger, opponent), 5)
            }
            MatchOutcome.LOSS -> {
                Pair(5, calculateXp(opponent, challenger))
            }
            MatchOutcome.DRAW -> {
                Pair(10, 10)
            }
        }

        val finalMatch = savedMatch.copy(
            matchOutcome = matchOutcome,
            challengerXp = challengerXp,
            opponentXp = opponentXp
        )

        matchRepository.updateMatch(finalMatch)

        // Update character XP
        characterService.addExperience(challengerId, challengerXp)
        characterService.addExperience(opponentId, opponentXp)

        val updatedChallengerLeaderBoard = LeaderBoardUpdate(
            characterId = challengerId,
            matchOutcome = matchOutcome
        )
        leaderBoardService.updateLeaderBoard(updatedChallengerLeaderBoard)

        val updatedOpponentLeaderBoard = LeaderBoardUpdate(
            characterId = opponentId,
            matchOutcome = when (matchOutcome) {
                MatchOutcome.WIN -> MatchOutcome.LOSS
                MatchOutcome.LOSS -> MatchOutcome.WIN
                MatchOutcome.DRAW -> MatchOutcome.DRAW
            }
        )
        leaderBoardService.updateLeaderBoard(updatedOpponentLeaderBoard)

        return finalMatch
    }

    private fun simulateMatch(challenger: GameCharacter, opponent: GameCharacter, matchId: Int): MatchOutcome {
        // Create mutable copies to track health, stamina and mana changes
        var challengerCurrentHealth = challenger.health
        var challengerCurrentStamina = challenger.stamina ?: 100
        var challengerCurrentMana = challenger.mana ?: 100

        var opponentCurrentHealth = opponent.health
        var opponentCurrentStamina = opponent.stamina ?: 100
        var opponentCurrentMana = opponent.mana ?: 100

        // Track match progress
        var round = 1
        val maxRounds = 10

        while (challengerCurrentHealth > 0 && opponentCurrentHealth > 0 && round <= maxRounds) {
            logger.debug { "Simulating round $round: $challenger (HP: $challengerCurrentHealth) vs $opponent (HP: $opponentCurrentHealth)" }

            val (challengerStats, opponentStats) = roundService.simulateRound(challenger, opponent)

            challengerCurrentHealth += challengerStats.healthDelta
            challengerCurrentStamina += challengerStats.staminaDelta
            challengerCurrentMana += challengerStats.manaDelta

            opponentCurrentHealth += opponentStats.healthDelta
            opponentCurrentStamina += opponentStats.staminaDelta
            opponentCurrentMana += opponentStats.manaDelta

            challengerCurrentHealth = challengerCurrentHealth.coerceAtLeast(0)
            challengerCurrentStamina = challengerCurrentStamina.coerceAtLeast(0)
            challengerCurrentMana = challengerCurrentMana.coerceAtLeast(0)

            opponentCurrentHealth = opponentCurrentHealth.coerceAtLeast(0)
            opponentCurrentStamina = opponentCurrentStamina.coerceAtLeast(0)
            opponentCurrentMana = opponentCurrentMana.coerceAtLeast(0)

            // Record round using the CORRECT matchId from parent function
            roundService.createMatchRound(
                matchId = matchId,  // Use parameter from createMatchResult()
                round = round,
                challenger = challenger,
                opponent = opponent,
                challengerHealthDelta = challengerStats.healthDelta,
                challengerStaminaDelta = challengerStats.staminaDelta,
                challengerManaDelta = challengerStats.manaDelta,
                opponentHealthDelta = opponentStats.healthDelta,
                opponentStaminaDelta = opponentStats.staminaDelta,
                opponentManaDelta = opponentStats.manaDelta
            )

            if (challengerCurrentHealth <= 0 || opponentCurrentHealth <= 0 ||
                (challengerCurrentStamina <= 0 && challengerCurrentMana <= 0) ||
                (opponentCurrentStamina <= 0 && opponentCurrentMana <= 0)) {
                break
            }

            round++
        }

        val matchOutcome = when {
            opponentCurrentHealth <= 0 || (opponentCurrentStamina <= 0 && opponentCurrentMana <= 0) ->
                MatchOutcome.WIN

            challengerCurrentHealth <= 0 || (challengerCurrentStamina <= 0 && challengerCurrentMana <= 0) ->
                MatchOutcome.LOSS

            challengerCurrentHealth.toDouble() / challenger.health.toDouble() ==
                    opponentCurrentHealth.toDouble() / opponent.health.toDouble() ->
                MatchOutcome.DRAW

            else -> {
                val challengerHealthPercentage = challengerCurrentHealth.toDouble() / challenger.health.toDouble()
                val opponentHealthPercentage = opponentCurrentHealth.toDouble() / opponent.health.toDouble()

                if (challengerHealthPercentage > opponentHealthPercentage) {
                    MatchOutcome.WIN
                } else {
                    MatchOutcome.LOSS
                }
            }
        }

        logger.debug { "Match complete after $round rounds. Match outcome: $matchOutcome" }
        return matchOutcome
    }

    private fun calculateXp(winner: GameCharacter, loser: GameCharacter): Int {
        val baseXp = 20

        // Bonus XP based on health difference and attack difference
        val xpGain = baseXp + (winner.health - loser.health) / 10 + (winner.attack - loser.attack) / 5

        return xpGain.coerceAtLeast(5)
    }

    fun getEnhancedMatches(): List<EnhancedMatchResult> {
        val matches = getAllMatches()
        return matches.map { match -> enhanceMatchResult(match) }
    }

    fun getEnhancedMatchesByAccountId(accountId: AccountId): List<EnhancedMatchResult> {
        val matches = getMatchesByAccountId(accountId)
        return matches.map { match -> enhanceMatchResult(match) }
    }

    fun getEnhancedMatchesByCharacterId(characterId: Int): List<EnhancedMatchResult> {
        val matches = getMatchesByCharacterId(characterId)
        return matches.map { match -> enhanceMatchResult(match) }
    }

    fun enhanceMatchResult(match: GameMatchResult): EnhancedMatchResult {

        // Get challenger and opponent data
        val challenger = characterService.getCharacterById(match.challengerId)
            ?: throw IllegalStateException("Challenger with ID ${match.challengerId} not found")

        val opponent = characterService.getCharacterById(match.opponentId)
            ?: throw IllegalStateException("Opponent with ID ${match.opponentId} not found")

        // Get rounds for this match
        val rounds = roundService.getRoundsByMatchId(match.id)

        // Create character data objects
        val challengerData = MatchCharacterData(
            id = challenger.id,
            name = challenger.name,
            characterClass = challenger.characterClass,
            experienceGained = match.challengerXp
        )

        val opponentData = MatchCharacterData(
            id = opponent.id,
            name = opponent.name,
            characterClass = opponent.characterClass,
            experienceGained = match.opponentXp
        )

        // Convert backend MatchOutcome to frontend-compatible string
        val matchOutcomeString = when (match.matchOutcome) {
            MatchOutcome.WIN -> "CHALLENGER_WON"
            MatchOutcome.LOSS -> "OPPONENT_WON"
            MatchOutcome.DRAW -> "DRAW"
        }

        // Create and return enhanced match result
        return EnhancedMatchResult(
            id = match.id,
            challenger = challengerData,
            opponent = opponentData,
            matchOutcome = matchOutcomeString,
            rounds = rounds
        )
    }
}