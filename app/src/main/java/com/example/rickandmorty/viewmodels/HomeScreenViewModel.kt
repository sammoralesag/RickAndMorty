package com.example.rickandmorty.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.models.domain.CharacterPage
import com.example.rickandmorty.components.repositpry.CharacterRepository
import com.example.rickandmorty.screens.HomeScreenViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {
    private val _viewState = MutableStateFlow<HomeScreenViewState>(HomeScreenViewState.Loading)
    val viewState: StateFlow<HomeScreenViewState> = _viewState.asStateFlow()

    private val fetchCharacterPages = mutableListOf<CharacterPage>()
    fun fetchInitialPage() = viewModelScope.launch {
        if (fetchCharacterPages.isNotEmpty()) return@launch
        val initialPage = characterRepository.fetchCharacterPage(1)
        initialPage.onSuccess { characterPage ->
            fetchCharacterPages.clear()
            fetchCharacterPages.add(characterPage)
            _viewState.update { return@update HomeScreenViewState.GridDisplay(characters = characterPage.characters) }
        }.onFailure {

        }
    }

    fun fetchNextPage() = viewModelScope.launch {
        val nextPageIndex = fetchCharacterPages.size + 1
        characterRepository.fetchCharacterPage(nextPageIndex)
            .onSuccess { characterPage ->
                fetchCharacterPages.add(characterPage)
                _viewState.update { currentState ->
                    val currentCharacters =
                        (currentState as? HomeScreenViewState.GridDisplay)?.characters
                            ?: emptyList()
                    return@update HomeScreenViewState.GridDisplay(characters = currentCharacters + characterPage.characters)
                }
            }.onFailure {
                //todo
            }
    }
}