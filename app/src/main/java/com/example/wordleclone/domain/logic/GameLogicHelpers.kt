package com.example.wordleclone.domain.logic

import com.example.wordleclone.domain.model.*

internal fun submitRow(
    state: GameDomainState,
    currentRow: GameRow,
    targetWord: String,
    isValidWord: (String) -> Boolean
): GameDomainState {
    val guess = currentRow.entries.joinToString("") { it.char }
    val validationError = validateGuess(guess = guess, isValidWord = isValidWord, state = state)

    if (validationError != null) return state.copy(validationError = validationError, gameState = GameState.Running)

    val evaluatedEntries = evaluateGuess(guess, targetWord)
    val updatedRows = replaceRow(state.rows, currentRow.copy(entries = evaluatedEntries, state = RowState.GUESSED))
    val updatedUsedChars = updateUsedCharacters(state.usedCharacters, evaluatedEntries)

    var newState = state.copy(rows = updatedRows, usedCharacters = updatedUsedChars)
    if (newState.hardMode) {
        newState = updateConstraints(newState, evaluatedEntries)
    }

    return when {
        guess.equals(targetWord, ignoreCase = true) -> newState.copy(gameState = GameState.Won)
        currentRow.rowNumber == 5 -> newState.copy(gameState = GameState.Lost(targetWord))
        else -> activateNextRow(newState, currentRow.rowNumber + 1)
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

internal fun replaceRow(rows: List<GameRow>, newRow: GameRow): List<GameRow> {
    return rows.map { if (it.rowNumber == newRow.rowNumber) newRow else it }
}

private fun validateGuess(
    guess: String,
    isValidWord: (String) -> Boolean,
    state: GameDomainState
): ValidationError? {
    if (guess.length != 5) return ValidationError.WordWrongLength // "Word must have 5 letters"
    if (!isValidWord(guess)) return ValidationError.WordNotInList //"Not a valid word"

    if (state.hardMode) {
        // Check position locks
        for ((pos, requiredChar) in state.positionLocks) {
            if (guess[pos].uppercaseChar() != requiredChar) {
                return ValidationError.MissingPositionChar(requiredChar, pos) //"Must use '$requiredChar' in position ${pos + 1}"
            }
        }

        // Check required chars count
        val guessCharCounts = guess.uppercase().groupingBy { it }.eachCount()
        for ((requiredChar, requiredCount) in state.requiredChars) {
            val countInGuess = guessCharCounts[requiredChar] ?: 0
            if (countInGuess < requiredCount) {
                // "Your guess must include at least $requiredCount '$requiredChar'${if (requiredCount > 1) "s" else ""}"
                return ValidationError.MissingRequiredChar(requiredChar, requiredCount)
            }
        }
    }
    return null
}

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

private fun activateNextRow(state: GameDomainState, nextRowNumber: Int): GameDomainState {
    return setRowState(state, nextRowNumber, RowState.ACTIVE)
}

private fun setRowState(state: GameDomainState, rowNumber: Int, newState: RowState): GameDomainState {
    val updatedRows = state.rows.map {
        if (it.rowNumber == rowNumber) it.copy(state = newState) else it
    }
    return state.copy(rows = updatedRows)
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

private fun updateConstraints(
    state: GameDomainState,
    evaluatedEntries: List<Character>
): GameDomainState {
    // Make mutable copies
    val newPositionLocks = state.positionLocks.toMutableMap()
    val newRequiredChars = state.requiredChars.toMutableMap()

    val charCountInGuess = mutableMapOf<Char, Int>()
    evaluatedEntries.forEachIndexed { index, entry ->
        val ch = entry.char.firstOrNull()?.uppercaseChar() ?: return@forEachIndexed
        when (entry.charState) {
            CharState.MATCH_IN_POSITION -> {
                newPositionLocks[index] = ch
                // This also implies that character must appear at least once:
                val oldCount = newRequiredChars[ch] ?: 0
                newRequiredChars[ch] = maxOf(oldCount, 1)
            }
            CharState.MATCH_IN_WORD -> {
                charCountInGuess[ch] = (charCountInGuess[ch] ?: 0) + 1
            }
            CharState.NO_MATCH -> { /* no new info if hard mode is on, except we know this letter not here unless previously known */ }
        }
    }

    // For MATCH_IN_WORD chars, ensure we record at least one occurrence required.
    for ((ch, count) in charCountInGuess) {
        val oldCount = newRequiredChars[ch] ?: 0
        // If we discovered that this char appears at least 'count' times, update requiredChars accordingly.
        // For simplicity, assume each discovered MATCH_IN_WORD increases the minimum known occurrences.
        newRequiredChars[ch] = maxOf(oldCount, count)
    }

    return state.copy(
        positionLocks = newPositionLocks,
        requiredChars = newRequiredChars
    )
}
