package com.example.wordleclone.domain.logic

import com.example.wordleclone.ui.keyboard.KeyboardKey

sealed class GameAction {
    data object ResetGame : GameAction()
    data class KeyPress(val key: KeyboardKey) : GameAction()
    data class WordFetched(val word: String) : GameAction()
    data class FetchError(val message: String) : GameAction()
}
