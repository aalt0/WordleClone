package com.example.wordleclone.domain.logic

import com.example.wordleclone.domain.model.*

internal fun submitRow(
    state: GameUiState,
    currentRow: GameRow,
    targetWord: String,
    isValidWord: (String) -> Boolean
): GameUiState {
    val guess = currentRow.entries.joinToString("") { it.char }
    val error = validateGuess(guess, isValidWord)

    if (error != null) return state.copy(errorMessage = error, status = GameState.Running)

    val evaluatedEntries = evaluateGuess(guess, targetWord)
    val updatedRows = replaceRow(state.rows, currentRow.copy(entries = evaluatedEntries, state = RowState.GUESSED))
    val updatedUsedChars = updateUsedCharacters(state.usedCharacters, evaluatedEntries)
    val newState = state.copy(rows = updatedRows, usedCharacters = updatedUsedChars)

    return when {
        guess.equals(targetWord, ignoreCase = true) -> newState.copy(status = GameState.Won)
        currentRow.rowNumber == 5 -> newState.copy(status = GameState.Lost(targetWord))
        else -> activateNextRow(newState, currentRow.rowNumber + 1)
    }
}

internal fun validateGuess(guess: String, isValidWord: (String) -> Boolean): String? {
    return when {
        guess.length != 5 -> "Word must have 5 letters"
        !isValidWord(guess) -> "Not a valid word"
        else -> null
    }
}

internal fun modifyRowEntries(
    state: GameUiState,
    row: GameRow,
    addChar: String = "",
    removeLast: Boolean = false
): GameUiState {
    val updatedEntries = when {
        removeLast -> removeLastChar(row.entries)
        addChar.isNotEmpty() -> addCharToEntries(row.entries, addChar)
        else -> row.entries
    }
    return state.copy(rows = replaceRow(state.rows, row.copy(entries = updatedEntries)))
}

internal fun removeLastChar(entries: List<Character>): List<Character> {
    val lastFilledIndex = entries.indexOfLast { it.char.isNotEmpty() }
    if (lastFilledIndex != -1) {
        return entries.toMutableList().apply { this[lastFilledIndex] = Character() }
    }
    return entries
}

internal fun addCharToEntries(entries: List<Character>, newChar: String): List<Character> {
    val firstEmptyIndex = entries.indexOfFirst { it.char.isEmpty() }
    return if (firstEmptyIndex != -1) {
        entries.toMutableList().apply { this[firstEmptyIndex] = Character(newChar) }
    } else {
        entries
    }
}

internal fun evaluateGuess(guess: String, targetWord: String): List<Character> {
    val availability = targetWord.toMutableList()
    val result = MutableList(guess.length) { Character() }

    // First pass: exact matches
    for (i in guess.indices) {
        if (guess[i].equals(targetWord[i], ignoreCase = true)) {
            result[i] = Character(guess[i].toString(), CharState.MATCH_IN_POSITION)
            availability[i] = '_'
        }
    }

    // Second pass: partial matches
    for (i in guess.indices) {
        if (result[i].charState == CharState.MATCH_IN_POSITION) continue
        val charIndex = availability.indexOfFirst { it.equals(guess[i], ignoreCase = true) }
        result[i] = if (charIndex != -1) {
            availability[charIndex] = '_'
            Character(guess[i].toString(), CharState.MATCH_IN_WORD)
        } else {
            Character(guess[i].toString(), CharState.NO_MATCH)
        }
    }
    return result
}

internal fun activateNextRow(state: GameUiState, nextRowNumber: Int): GameUiState {
    return setRowState(state, nextRowNumber, RowState.ACTIVE)
}

internal fun setRowState(state: GameUiState, rowNumber: Int, newState: RowState): GameUiState {
    val updatedRows = state.rows.map {
        if (it.rowNumber == rowNumber) it.copy(state = newState) else it
    }
    return state.copy(rows = updatedRows)
}

internal fun replaceRow(rows: List<GameRow>, newRow: GameRow): List<GameRow> {
    return rows.map { if (it.rowNumber == newRow.rowNumber) newRow else it }
}

private fun updateUsedCharacters(
    currentMap: Map<String, CharState>,
    evaluatedEntries: List<Character>
): Map<String, CharState> {
    val newMap = currentMap.toMutableMap()
    for (entry in evaluatedEntries) {
        val existingState = newMap[entry.char]
        val newState = mergeStates(existingState, entry.charState)
        newMap[entry.char] = newState
    }
    return newMap
}

private fun mergeStates(oldState: CharState?, newState: CharState): CharState {
    if (oldState == null) return newState
    // Define a priority order for states
    val priority = mapOf(
        CharState.MATCH_IN_POSITION to 3,
        CharState.MATCH_IN_WORD to 2,
        CharState.NO_MATCH to 1
    )
    return if (priority[newState]!! > priority[oldState]!!) newState else oldState
}
