package com.motycka.edu.game.character

import com.motycka.edu.game.account.AccountService
import com.motycka.edu.game.character.rest.CharacterCreateRequest
import com.motycka.edu.game.character.rest.CharacterResponse
import com.motycka.edu.game.character.rest.toCharacter
import com.motycka.edu.game.character.rest.toCharacterResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/characters")
class CharacterController(
    private val characterService: CharacterService,
    private val accountService: AccountService
) {
    @GetMapping
    fun getCharacters(): List<CharacterResponse> {
        val accountId = accountService.getCurrentAccountId()
        return characterService.getCharactersByAccountId(accountId)
            .map { it.toCharacterResponse() }
    }

    @PostMapping
    fun postCharacter(
        @RequestBody character: CharacterCreateRequest
    ): ResponseEntity<CharacterResponse> {
        val accountId = accountService.getCurrentAccountId()
        val gameCharacter = character.toCharacter(accountId)
        val createdCharacter = characterService.createCharacter(gameCharacter).toCharacterResponse()

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCharacter)
    }
}
