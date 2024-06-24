package com.example.rickandmorty.components.repositpry

import com.example.network.KtorClient
import com.example.network.models.domain.Character
import com.example.network.models.domain.CharacterPage
import javax.inject.Inject

class CharacterRepository @Inject constructor(private val ktorClient: KtorClient) {

    suspend fun fetchCharacterPage(page: Int): KtorClient.ApiOperation<CharacterPage> {
        return ktorClient.getCharacterByPage(page)
    }

    suspend fun fetchCharacter(characterId: Int): KtorClient.ApiOperation<Character> {
        return ktorClient.getCharacter(characterId)
    }
}