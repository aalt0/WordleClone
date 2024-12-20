package com.example.wordleclone.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordleclone.domain.repo.WordListRepo
import com.example.wordleclone.ui.keyboard.KeyboardKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Models
data class GameUiState(
    val rows: List<GameRow> = List(6) { GameRow(it, if (it == 0) RowState.ACTIVE else RowState.INACTIVE) },
    val status: GameState = GameState.Loading
)

data class GameRow(
    val rowNumber: Int,
    val state: RowState = RowState.INACTIVE,
    val entries: List<Character> = List(5) { Character() }
)

data class Character(val char: String = "", val charState: CharState = CharState.NO_MATCH)

enum class RowState { INACTIVE, ACTIVE, GUESSED }
enum class CharState { NO_MATCH, MATCH_IN_WORD, MATCH_IN_POSITION }
sealed class GameState {
    object Loading : GameState()
    object Running : GameState()
    object Won : GameState()
    data class Lost(val correctWord: String) : GameState()
    data class Error(val message: String) : GameState()
}

class MainViewModel(private val wordRepo: WordListRepo) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private var targetWord: String = ""

    init {
        fetchTargetWord()
    }

    fun onResetClicked() {
        fetchTargetWord()
    }

    fun onKeyPress(key: KeyboardKey) {
        val currentRow = _uiState.value.rows.firstOrNull { it.state == RowState.ACTIVE } ?: return

        when (key) {
            KeyboardKey.ENTER -> submitRow(currentRow)
            KeyboardKey.DEL -> updateRow(currentRow, removeLast = true)
            else -> updateRow(currentRow, addChar = key.name)
        }
    }

    private fun fetchTargetWord() {
        viewModelScope.launch(Dispatchers.IO) {
            wordRepo.getWord()
                .onSuccess { word ->
                    targetWord = word
                    _uiState.value = GameUiState(status = GameState.Running)
                }.onFailure {
                    _uiState.value = GameUiState(status = GameState.Error("Failed to load word"))
                }
        }
    }

    private fun updateRow(row: GameRow, addChar: String = "", removeLast: Boolean = false) {
        val entries = row.entries.toMutableList()
        if (removeLast) {
            val lastIndex = entries.indexOfLast { it.char.isNotEmpty() }
            if (lastIndex != -1) entries[lastIndex] = Character()
        } else if (addChar.isNotEmpty()) {
            val firstEmptyIndex = entries.indexOfFirst { it.char.isEmpty() }
            if (firstEmptyIndex != -1) entries[firstEmptyIndex] = Character(addChar)
        }
        updateRows(row.copy(entries = entries))
    }

    private fun submitRow(row: GameRow) {
        val guessedWord = row.entries.joinToString("") { it.char }

        if (guessedWord.length != 5) return updateState(GameState.Error("Word must have 5 letters"))
        if (!wordRepo.isWordInWordList(guessedWord)) return updateState(GameState.Error("Not a valid word"))

        val evaluatedEntries = evaluateGuess(guessedWord)
        updateRows(row.copy(entries = evaluatedEntries, state = RowState.GUESSED))

        when {
            guessedWord.equals(targetWord, true) -> updateState(GameState.Won)
            row.rowNumber == 5 -> updateState(GameState.Lost(targetWord))
            else -> activateNextRow(row.rowNumber + 1)
        }
    }

    private fun evaluateGuess(guess: String): List<Character> {
        val targetWordAvailability = targetWord.toMutableList() // Tracks unmatched target chars
        val result = MutableList(guess.length) { Character() }

        // First pass: Mark exact matches
        for (i in guess.indices) {
            if (guess[i].equals(targetWord[i], ignoreCase = true)) {
                result[i] = Character(
                    char = guess[i].toString(),
                    charState = CharState.MATCH_IN_POSITION
                )
                targetWordAvailability[i] = '_' // Mark as used
            }
        }

        // Second pass: Mark partial matches
        for (i in guess.indices) {
            if (result[i].charState == CharState.MATCH_IN_POSITION) continue // Already matched

            val charIndex = targetWordAvailability.indexOfFirst {
                it.equals(guess[i], ignoreCase = true)
            }

            if (charIndex != -1) {
                result[i] = Character(
                    char = guess[i].toString(),
                    charState = CharState.MATCH_IN_WORD
                )
                targetWordAvailability[charIndex] = '_' // Mark as used
            } else {
                result[i] = Character(
                    char = guess[i].toString(),
                    charState = CharState.NO_MATCH
                )
            }
        }

        return result
    }

    private fun updateRows(updatedRow: GameRow) {
        _uiState.value = _uiState.value.copy(
            rows = _uiState.value.rows
                .map { if (it.rowNumber == updatedRow.rowNumber) updatedRow else it }
        )
    }

    private fun activateNextRow(nextRowNumber: Int) {
        updateRows(_uiState.value.rows[nextRowNumber].copy(state = RowState.ACTIVE))
    }

    private fun updateState(newState: GameState) {
        _uiState.value = _uiState.value.copy(status = newState)
    }
}
