package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.character.rest.toCharacterResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/characters/opponents")
class OpponentController(
    private val characterService: CharacterService,
    private val accountService: AccountService
) {
    @GetMapping
    fun findCharacters(): List<CharacterResponse> {
        val accountId = accountService.getCurrentAccountId()
        return characterService.getCharacters()
            .filter { it.accountId != accountId }
            .map { it.toCharacterResponse() }
    }
}