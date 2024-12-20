package com.example.wordleclone.domain.model

sealed class GameState {
    data object Loading : GameState()
    data object Running : GameState()
    data object Won : GameState()
    data class Lost(val correctWord: String) : GameState()
}
