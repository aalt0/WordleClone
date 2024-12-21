package com.example.wordleclone.domain.logic

import com.example.wordleclone.domain.model.*

// --------------------------------------
// Constants
// --------------------------------------
private const val WORD_LENGTH = 5
private const val MAX_GUESSES = 6

private val CHAR_STATE_PRIORITY = mapOf(
    CharState.MATCH_IN_POSITION to 3,
    CharState.MATCH_IN_WORD to 2,
    CharState.NO_MATCH to 1
)

// --------------------------------------
// Public Entry Points
// --------------------------------------

internal fun submitRow(
    state: GameDomainState,
    currentRow: GameRow,
    targetWord: String,
    isValidWord: (String) -> Boolean
): GameDomainState {
    val guess = currentRow.entries.joinToString("") { it.char }
    val validationError = validateGuess(guess, isValidWord, state)

    if (validationError != null) {
        return state.copy(
            validationError = validationError,
            gameState = GameState.Running
        )
    }

    val evaluatedEntries = evaluateGuess(guess, targetWord)
    val updatedRows = replaceRow(state.rows, currentRow.copy(entries = evaluatedEntries, state = RowState.GUESSED))
    val updatedUsedChars = updateUsedCharacters(state.usedCharacters, evaluatedEntries)

    var updatedState = state.copy(rows = updatedRows, usedCharacters = updatedUsedChars)
    if (updatedState.hardMode) {
        updatedState = updateHardModeConstraints(updatedState, evaluatedEntries)
    }

    return when {
        guess.equals(targetWord, ignoreCase = true) -> updatedState.copy(gameState = GameState.Won)
        currentRow.rowNumber == (MAX_GUESSES - 1) -> updatedState.copy(gameState = GameState.Lost(targetWord))
        else -> activateNextRow(updatedState, currentRow.rowNumber + 1)
    }
}

internal fun modifyRowEntries(
    state: GameDomainState,
    row: GameRow,
    addChar: String = "",
    removeLast: Boolean = false
): GameDomainState {
    val updatedEntries = when {
        removeLast -> removeLastChar(row.entries)
        addChar.isNotEmpty() -> addCharToEntries(row.entries, addChar)
        else -> row.entries
    }
    return state.copy(rows = replaceRow(state.rows, row.copy(entries = updatedEntries)))
}

// --------------------------------------
// Validation
// --------------------------------------

private fun validateGuess(
    guess: String,
    isValidWord: (String) -> Boolean,
    state: GameDomainState
): ValidationError? {
    if (guess.length != WORD_LENGTH) return ValidationError.WordWrongLength
    if (!isValidWord(guess)) return ValidationError.WordNotInList

    if (state.hardMode) {
        // Check position locks
        for ((pos, requiredChar) in state.positionLocks) {
            if (guess[pos].uppercaseChar() != requiredChar) {
                return ValidationError.MissingPositionChar(requiredChar, pos)
            }
        }

        // Check required chars count
        val guessCharCounts = guess.uppercase().groupingBy { it }.eachCount()
        for ((requiredChar, requiredCount) in state.requiredChars) {
            val countInGuess = guessCharCounts[requiredChar] ?: 0
            if (countInGuess < requiredCount) {
                return ValidationError.MissingRequiredChar(requiredChar, requiredCount)
            }
        }
    }
    return null
}

// --------------------------------------
// Guess Evaluation and State Updates
// --------------------------------------

private fun evaluateGuess(guess: String, targetWord: String): List<Character> {
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

private fun activateNextRow(state: GameDomainState, nextRowNumber: Int): GameDomainState {
    return setRowState(state, nextRowNumber, RowState.ACTIVE)
}

private fun setRowState(state: GameDomainState, rowNumber: Int, newState: RowState): GameDomainState {
    val updatedRows = state.rows.map {
        if (it.rowNumber == rowNumber) it.copy(state = newState) else it
    }
    return state.copy(rows = updatedRows)
}

private fun updateHardModeConstraints(
    state: GameDomainState,
    evaluatedEntries: List<Character>
): GameDomainState {
    val newPositionLocks = state.positionLocks.toMutableMap()
    val newRequiredChars = state.requiredChars.toMutableMap()
    val charCountInGuess = mutableMapOf<Char, Int>()

    for ((index, entry) in evaluatedEntries.withIndex()) {
        val ch = entry.char.firstOrNull()?.uppercaseChar() ?: continue
        when (entry.charState) {
            CharState.MATCH_IN_POSITION -> {
                newPositionLocks[index] = ch
                val oldCount = newRequiredChars[ch] ?: 0
                newRequiredChars[ch] = maxOf(oldCount, 1)
            }
            CharState.MATCH_IN_WORD -> {
                charCountInGuess[ch] = (charCountInGuess[ch] ?: 0) + 1
            }
            CharState.NO_MATCH -> { /* No update needed for hard mode here */ }
        }
    }

    // For MATCH_IN_WORD chars, ensure we record at least one occurrence required.
    for ((ch, count) in charCountInGuess) {
        val oldCount = newRequiredChars[ch] ?: 0
        newRequiredChars[ch] = maxOf(oldCount, count)
    }

    return state.copy(
        positionLocks = newPositionLocks,
        requiredChars = newRequiredChars
    )
}

// --------------------------------------
// Entry Manipulation Helpers
// --------------------------------------

private fun removeLastChar(entries: List<Character>): List<Character> {
    val lastFilledIndex = entries.indexOfLast { it.char.isNotEmpty() }
    if (lastFilledIndex != -1) {
        return entries.toMutableList().apply { this[lastFilledIndex] = Character() }
    }
    return entries
}

private fun addCharToEntries(entries: List<Character>, newChar: String): List<Character> {
    val firstEmptyIndex = entries.indexOfFirst { it.char.isEmpty() }
    return if (firstEmptyIndex != -1) {
        entries.toMutableList().apply { this[firstEmptyIndex] = Character(newChar) }
    } else {
        entries
    }
}

private fun replaceRow(rows: List<GameRow>, newRow: GameRow): List<GameRow> {
    return rows.map { if (it.rowNumber == newRow.rowNumber) newRow else it }
}

// --------------------------------------
// State Merging Logic
// --------------------------------------

private fun mergeStates(oldState: CharState?, newState: CharState): CharState {
    if (oldState == null) return newState
    return if (CHAR_STATE_PRIORITY.getValue(newState) > CHAR_STATE_PRIORITY.getValue(oldState)) {
        newState
    } else {
        oldState
    }
}
