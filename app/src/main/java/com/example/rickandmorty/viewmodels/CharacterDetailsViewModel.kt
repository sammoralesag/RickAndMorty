package com.example.rickandmorty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.components.common.DataPoint
import com.example.rickandmorty.components.repositpry.CharacterRepository
import com.example.rickandmorty.screens.CharacterDetailsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {
    private val _internalStorageFlow = MutableStateFlow<CharacterDetailsViewState>(
        value = CharacterDetailsViewState.Loading
    )
    val stateFlow = _internalStorageFlow.asStateFlow()

    fun fetchCharacter(characterId: Int) = viewModelScope.launch {
        _internalStorageFlow.update { return@update CharacterDetailsViewState.Loading }
        characterRepository.fetchCharacter(characterId)
            .onSuccess { character ->
                val dataPoints = buildList {
                    add(DataPoint("Last known location", character.location.name))
                    add(DataPoint("Species", character.species))
                    add(DataPoint("Last known location", character.gender.displayName))
                    character.type.takeIf { it.isNotEmpty() }?.let { type ->
                        add(DataPoint("type", type))
                    }
                    add(DataPoint("Origin", character.origin.name))
                    add(DataPoint("Episode count", character.episodeIds.size.toString()))
                }
                _internalStorageFlow.update {
                    return@update CharacterDetailsViewState.Success(
                        character = character,
                        characterDataPoint = dataPoints
                    )
                }

            }.onFailure { exception ->
                _internalStorageFlow.update {
                    return@update CharacterDetailsViewState.Error(
                        message = exception.message ?: "Unknown Error occurred"
                    )
                }
            }
    }
}