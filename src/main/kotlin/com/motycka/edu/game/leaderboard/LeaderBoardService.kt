package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.character.CharacterService
import com.motycka.edu.game.leaderboard.model.LeaderBoardUpdate
import com.motycka.edu.game.leaderboard.rest.LeaderBoardResponse
import com.motycka.edu.game.leaderboard.rest.toLeaderBoardResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class LeaderBoardService(
    private val leaderBoardRepository: LeaderBoardRepository,
    private val characterService: CharacterService
) {
    fun getLeaderBoards(
        characterClass: String? = null
    ): List<LeaderBoardResponse> {
        logger.info { "Getting leaderboards" }
        val leaderBoards = leaderBoardRepository.getLeaderBoards(characterClass)

        val sortedLeaderBoards = leaderBoards.sortedBy { leaderBoard ->
            val totalMatches = leaderBoard.wins + leaderBoard.losses + leaderBoard.draws
            if (totalMatches > 0) {
                leaderBoard.losses.toDouble() / totalMatches
            } else {
                1.0
            }
        }

        return sortedLeaderBoards.mapIndexed { index, leaderBoard ->
            leaderBoard.toLeaderBoardResponse(position = index + 1, character = characterService.getCharacterById(leaderBoard.characterId))
        }
    }

    fun updateLeaderBoard(update: LeaderBoardUpdate) {
        logger.info { "Updating leaderboard for character ${update.characterId} with status ${update.matchOutcome}" }
        leaderBoardRepository.updateLeaderBoard(update)
    }
}


