package com.example.wordleclone.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordleclone.domain.logic.GameAction
import com.example.wordleclone.domain.logic.reduce
import com.example.wordleclone.domain.model.GameDomainState
import com.example.wordleclone.domain.model.RowState
import com.example.wordleclone.domain.model.ValidationError
import com.example.wordleclone.domain.repo.WordListRepo
import com.example.wordleclone.ui.keyboard.KeyboardKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val domainState: GameDomainState,
    val errorMessage: String? = null,
    val animatingRowIndex: Int? = null,
    val invalidGuessRowIndex: Int? = null
)

class MainViewModel(private val wordRepo: WordListRepo) : ViewModel() {
    private val _uiStateFlow = MutableStateFlow(GameUiState(GameDomainState()))
    val uiState: StateFlow<GameUiState> = _uiStateFlow.asStateFlow()

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

    fun onAnimationEnded() {
        _uiStateFlow.value = _uiStateFlow.value.copy(animatingRowIndex = null, invalidGuessRowIndex = null)
    }

    private fun dispatch(action: GameAction) {
        val oldDomainState = _uiStateFlow.value.domainState
        val newDomainState = reduce(
            state = oldDomainState,
            action = action,
            targetWord = targetWord,
            isValidWord = { guess -> wordRepo.isWordInWordList(guess) }
        )

        val currentRowIndex = oldDomainState.rows.firstOrNull { it.state == RowState.ACTIVE }?.rowNumber

        val isSubmitting = action is GameAction.KeyPress && action.key == KeyboardKey.ENTER
        val isValidSubmit = newDomainState.validationError == null
        val errorMessage = handleValidationError(newDomainState.validationError)

        val (animatingRowIndex, invalidGuessRowIndex) = when {
            isSubmitting && isValidSubmit -> currentRowIndex to null
            isSubmitting && !isValidSubmit -> null to currentRowIndex
            else -> null to null
        }

        val newUiState = _uiStateFlow.value.copy(
            domainState = newDomainState,
            errorMessage = errorMessage,
            animatingRowIndex = animatingRowIndex,
            invalidGuessRowIndex = invalidGuessRowIndex
        )
        _uiStateFlow.value = newUiState
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

    private fun handleValidationError(error: ValidationError?): String? {
        return when (error) {
            null -> null
            ValidationError.WordWrongLength ->
                "Word must have 5 letters"
            ValidationError.WordNotInList ->
                "Not a valid word"
            is ValidationError.MissingPositionChar ->
                "Must use '${error.char}' in position ${error.position + 1}"
            is ValidationError.MissingRequiredChar ->
                "Your guess must include at least ${error.requiredCount} '${error.char}'${if (error.requiredCount > 1) "s" else ""}"
        }
    }
}
