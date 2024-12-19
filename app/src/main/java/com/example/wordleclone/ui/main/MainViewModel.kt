package com.example.wordleclone.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordleclone.domain.repo.WordListRepo
import com.example.wordleclone.ui.keyboard.KeyboardKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val status: GameState,
    val rows: List<GameRow>
)

class MainViewModel(private val wordListRepo: WordListRepo) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            status = GameState.Loading,
            rows = initialRows()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var _word: String = ""

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                wordListRepo.getWord()
                    .onSuccess { result ->
                        _word = result
                        _uiState.update { it.copy(status = GameState.Running) }
                    }
                    .onFailure { throwable ->
                        _uiState.update { it.copy(status = GameState.Error(ErrorType.ERROR_UNKNOWN)) }
                    }
            }
        }
    }

    fun onResetClicked() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                wordListRepo.getWord()
                    .onSuccess { result ->
                        _word = result
                        _uiState.update {
                            it.copy(
                                status = GameState.Running,
                                rows = initialRows()
                            )
                        }
                    }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                status = GameState.Error(ErrorType.ERROR_UNKNOWN),
                                rows = initialRows()
                            )
                        }
                    }
            }
        }
    }

    fun onKeyPressed(key: KeyboardKey) {
        val status = _uiState.value.status
        if (status is GameState.Error && status.error == ErrorType.ERROR_UNKNOWN) {
            // dunno what to do yet. other errors are recoverable
            return
        }

        when (status) {
            is GameState.Error,
            is GameState.Running -> {
                val rows = _uiState.value.rows
                val activeRow = rows.firstOrNull { it.rowState == RowState.ACTIVE }
                // sanity check
                if (activeRow == null) {
                    Log.d("--- ", "no active row")
                    _uiState.update { it.copy(status = GameState.Error(ErrorType.ERROR_UNKNOWN)) }
                    return
                }

                when (key) {
                    KeyboardKey.DEL -> handleDelete(activeRow)
                    KeyboardKey.ENTER -> handleEnter(activeRow)
                    else -> handleNewChar(key.name, activeRow)
                }
            }
            is GameState.Lost,
            is GameState.Won,
            GameState.Loading -> {
                Log.d("--- ", "game state: $status: key presses make no sense")
                return
            }
        }
    }

    private fun handleDelete(activeRow: GameRow) {
        Log.d("--- ", "handleDelete")
        val entries = activeRow.entries
        val lastNonEmptyInd = entries.indexOfLast { it.char.isNotEmpty() }
        if (lastNonEmptyInd == -1) {
            // no characters entered yet, nothing to do
            return
        }
        val newEntries = entries.toMutableList()
        newEntries[lastNonEmptyInd] = entries[lastNonEmptyInd].copy(char = "")
        val updatedRow = activeRow.copy(entries = newEntries)
        val newRows = _uiState.value.rows.toMutableList()
        newRows[activeRow.rowNumber] = updatedRow
        _uiState.update { it.copy(status = GameState.Running, rows = newRows) }
    }

    private fun handleNewChar(char: String, activeRow: GameRow) {
        Log.d("--- ", "handleNewChar: $char")
        val entries = activeRow.entries
        val firstEmptyInd = entries.indexOfFirst { it.char.isEmpty() }
        if (firstEmptyInd == -1) {
            // no empty characters, nothing to do (user needs to press enter or del)
            return
        }
        val newEntries = entries.toMutableList()
        newEntries[firstEmptyInd] = entries[firstEmptyInd].copy(char = char)
        val updatedRow = activeRow.copy(entries = newEntries)
        val newRows = _uiState.value.rows.toMutableList()
        newRows[activeRow.rowNumber] = updatedRow
        _uiState.update { it.copy(status = GameState.Running, rows = newRows ) }
    }

    private fun handleEnter(activeRow: GameRow) {
        Log.d("--- ", "handleEnter")

        // not a valid guess
        if (activeRow.entries.any { it.char.isEmpty() }) {
            _uiState.update { it.copy(status = GameState.Error(ErrorType.ERROR_NOT_5_LETTER_WORD)) }
            return
        }

        val guessedWord = activeRow.entries.map { it.char }.joinToString(separator = "")

        // words match, user won the game
        if (guessedWord.equals(_word, ignoreCase = true)) {
            val newEntries = activeRow.entries.map { it.copy(charState = CharState.MATCH_IN_POSITION) }
            val updatedRow = activeRow.copy(entries = newEntries)
            val newRows = _uiState.value.rows.toMutableList()
            newRows[activeRow.rowNumber] = updatedRow
            _uiState.update { it.copy(status = GameState.Won, rows = newRows) }
            return
        }

        // no more guesses left
        if (activeRow.rowNumber == 5) {
            _uiState.update { it.copy(status = GameState.Lost(_word)) }
            return
        }

        // invalid guess
        if (!wordListRepo.isWordInWordList(guessedWord)) {
            _uiState.update { it.copy(status = GameState.Error(ErrorType.ERROR_WORD_NOT_IN_WORDLIST)) }
            return
        }
        // check for partial matches
        val previousEntries = activeRow.entries
            .mapIndexed { index, guessedChar ->
                if (guessedChar.char.equals(_word[index].toString(), ignoreCase = true)) {
                    guessedChar.copy(charState = CharState.MATCH_IN_POSITION)
                } else if (_word.contains(guessedChar.char, ignoreCase = true)) {
                    guessedChar.copy(charState = CharState.MATCH_IN_WORD)
                } else {
                    guessedChar.copy(charState = CharState.NO_MATCH)
                }
            }
        val previousRow = activeRow.copy(rowState = RowState.GUESSED, entries = previousEntries)
        val nextActiveRow = _uiState.value.rows[activeRow.rowNumber + 1].copy(rowState = RowState.ACTIVE)

        val newRows = _uiState.value.rows.toMutableList()
        newRows[previousRow.rowNumber] = previousRow
        newRows[nextActiveRow.rowNumber] = nextActiveRow
        _uiState.update { it.copy(status = GameState.Running, rows = newRows) }
    }
}

sealed class GameState {
    data object Loading : GameState()
    data object Running : GameState()
    data object Won : GameState()
    data class Lost(val word: String) : GameState()
    // should separate the user errors and system errors
    data class Error(val error: ErrorType) : GameState()
}

enum class ErrorType {
    ERROR_WORD_NOT_IN_WORDLIST,
    ERROR_NOT_5_LETTER_WORD,
    ERROR_UNKNOWN,
}

enum class CharState {
    NO_MATCH,
    MATCH_IN_WORD,
    MATCH_IN_POSITION,
}

data class Character(
    val char: String = "",
    val charState: CharState = CharState.NO_MATCH
)

enum class RowState {
    INACTIVE,
    GUESSED,
    ACTIVE,
    MATCH
}

data class GameRow(
    val rowNumber: Int,
    val rowState: RowState = RowState.INACTIVE,
    val entries: List<Character> = List(5) { Character() }
)

private fun initialRows(): List<GameRow> = MutableList(6) { index ->
    if (index == 0) {
        GameRow(rowNumber = index, rowState = RowState.ACTIVE)
    } else {
        GameRow(index)
    }
}
