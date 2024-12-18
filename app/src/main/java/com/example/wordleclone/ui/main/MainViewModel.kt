package com.example.wordleclone.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordleclone.domain.repo.WordleCloneRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val ALLOWED_CHARS = Regex("^[A-Z]?$")

data class MainUiState(
    val status: Word = Word.Loading
)

sealed class Word {
    data object Loading : Word()
    data class Success(val word: String) : Word()
    data class Error(val error: String) : Word()
}

class MainViewModel(
    wordleCloneRepo: WordleCloneRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                wordleCloneRepo.getWord()
                    .onSuccess { result ->
                        _uiState.update { it.copy(status = Word.Success(result)) }
                    }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(status = Word.Error(throwable.message ?: "no explanation")) }
                    }
            }
        }
    }
}
