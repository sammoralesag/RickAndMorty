package com.example.rickandmorty.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.models.domain.Character
import com.example.rickandmorty.components.character.CharacterGridItem
import com.example.rickandmorty.components.character.CharacterListItem
import com.example.rickandmorty.components.common.DataPoint
import com.example.rickandmorty.components.common.LoadingState
import com.example.rickandmorty.components.repositpry.CharacterRepository
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

sealed interface CharacterDetailsViewState {
    data object Loading : CharacterDetailsViewState
    data class Error(val message: String) : CharacterDetailsViewState
    data class Success(
        val character: Character,
        val characterDataPoint: List<DataPoint>
    ) : CharacterDetailsViewState
}

@Composable
fun CharacterDetailsScreen(
    characterId: Int,
    viewModel: CharacterDetailsViewModel = hiltViewModel(),
    onEpisodeClicked: (Int) -> Unit
) {

    val state by viewModel.stateFlow.collectAsState()


    LaunchedEffect(
        key1 = Unit, block = {
            viewModel.fetchCharacter(characterId)
        })
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(all = 16.dp)
    ) {

        when (val viewState = state) {
            CharacterDetailsViewState.Loading -> {
                item { LoadingState() }
            }

            is CharacterDetailsViewState.Error -> {
                TODO()
            }

            is CharacterDetailsViewState.Success -> {

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CharacterGridItem(
                            modifier = Modifier.weight(1f),
                            character = viewState.character
                        ) {

                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        CharacterGridItem(
                            modifier = Modifier.weight(1f),
                            character = viewState.character
                        ) {

                        }
                    }
                }
                repeat(10) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item {
                        CharacterListItem(
                            character = viewState.character,
                            characterDataPoints = viewState.characterDataPoint
                        ) {

                        }
                    }
                }
//                // Name plate
//                item {
//                    CharacterDetailsNamePlateComponent(
//                        name = viewState.character.name,
//                        status = viewState.character.status
//                    )
//                }
//
//                item { Spacer(modifier = Modifier.height(8.dp)) }
//
//                // Image
//                item {
//                    CharacterImage(imageUrl = viewState.character.imageUrl)
//                }
//
//                // Data points
//                items(viewState.characterDataPoint) {
//                    Spacer(modifier = Modifier.height(32.dp))
//                    DataPointComponent(dataPoint = it)
//                }
//
//                item { Spacer(modifier = Modifier.height(32.dp)) }
//
//                // Button
//                item {
//                    Text(
//                        text = "View all episodes",
//                        color = RickAction,
//                        fontSize = 18.sp,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .padding(horizontal = 32.dp)
//                            .border(
//                                width = 1.dp,
//                                color = RickAction,
//                                shape = RoundedCornerShape(12.dp)
//                            )
//                            .clip(RoundedCornerShape(12.dp))
//                            .clickable {
//                                onEpisodeClicked(characterId)
//                            }
//                            .padding(vertical = 8.dp)
//                            .fillMaxWidth()
//                    )
//                }

                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }
}