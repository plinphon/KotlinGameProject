package com.motycka.edu.game.character.rest

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.character.model.GameCharacter

fun CharacterCreateRequest.toCharacter(accountId: AccountId) = GameCharacter(
    accountId = accountId,
    name = this.name,
    health = this.health,
    attack = this.attack,
    experience = this.experience,
    characterClass = this.characterClass,
    mana = this.mana,
    healing = this.healing,
    stamina = this.stamina,
    defense = this.defense
)

fun GameCharacter.toCharacterResponse() = CharacterResponse(
    id = this.id,
    accountId = this.accountId,
    name = this.name,
    health = this.health,
    attack = this.attack,
    experience = this.experience,
    characterClass = this.characterClass,
    mana = this.mana,
    healing = this.healing,
    stamina = this.stamina,
    defense = this.defense,
)
