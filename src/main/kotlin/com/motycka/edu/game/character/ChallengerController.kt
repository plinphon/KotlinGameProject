package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.character.rest.toCharacterResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/characters/challengers")
class ChallengerController(
    private val characterService: CharacterService,
    private val accountService: AccountService
) {
    @GetMapping
    fun findCharacters(): List<CharacterResponse> {
        val accountId = accountService.getCurrentAccountId()
        val characters = characterService.getCharactersByAccountId(accountId)

        return characters
            .map { it.toCharacterResponse() }
    }
}