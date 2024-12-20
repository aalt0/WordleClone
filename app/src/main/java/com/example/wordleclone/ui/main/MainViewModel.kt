package com.example.wordleclone.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordleclone.domain.logic.GameAction
import com.example.wordleclone.domain.logic.reduce
import com.example.wordleclone.domain.model.GameUiState
import com.example.wordleclone.domain.repo.WordListRepo
import com.example.wordleclone.ui.keyboard.KeyboardKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val wordRepo: WordListRepo) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState(hardMode = true))
    val uiState: StateFlow<GameUiState> = _uiState

    private var targetWord: String = ""

    init {
        dispatch(GameAction.ResetGame) // triggers loading state
        fetchTargetWord()
    }

    fun onResetClicked() {
        dispatch(GameAction.ResetGame)
        fetchTargetWord()
    }

    fun onKeyPress(key: KeyboardKey) {
        dispatch(GameAction.KeyPress(key))
    }

    private fun dispatch(action: GameAction) {
        val oldState = _uiState.value
        val newState = reduce(
            oldState,
            action,
            targetWord,
            isValidWord = { guess -> wordRepo.isWordInWordList(guess) }
        )
        _uiState.value = newState
    }

    private fun fetchTargetWord() {
        viewModelScope.launch(Dispatchers.IO) {
            wordRepo.getWord().fold(
                onSuccess = { word ->
                    targetWord = word
                    dispatch(GameAction.WordFetched(word))
                },
                onFailure = { dispatch(GameAction.FetchError("Failed to load word")) }
            )
        }
    }
}
