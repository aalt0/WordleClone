package com.example.wordleclone.domain.logic

import com.example.wordleclone.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class DomainLogicTest {

    private val targetWord = "CRANE"
    private val initialRows = List(6) { index ->
        GameRow(rowNumber = index, state = if (index == 0) RowState.ACTIVE else RowState.INACTIVE)
    }

    private fun initialState(
        hardMode: Boolean = false,
        usedCharacters: Map<String, CharState> = emptyMap(),
        positionLocks: Map<Int, Char> = emptyMap(),
        requiredChars: Map<Char, Int> = emptyMap(),
        validationError: ValidationError? = null,
        gameState: GameState = GameState.Running
    ): GameDomainState {
        return GameDomainState(
            rows = initialRows,
            gameState = gameState,
            usedCharacters = usedCharacters,
            hardMode = hardMode,
            positionLocks = positionLocks,
            requiredChars = requiredChars,
            validationError = validationError
        )
    }

    // Mocking isValidWord function:
    private val validWords = setOf("CRANE", "CRAZY", "GRAND", "TRACE", "CRACK")
    private val isValidWord: (String) -> Boolean = { guess -> validWords.contains(guess.uppercase()) }

    @Test
    fun `submitRow with too short guess returns WordWrongLength error`() {
        val state = initialState()
        val currentRow = state.rows[0].copy(entries = listOf(
            Character("C"), Character("R"), Character("A"), Character() // only 3 chars filled
        ))

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertTrue(newState.validationError is ValidationError.WordWrongLength)
        assertEquals(GameState.Running, newState.gameState)
    }

    @Test
    fun `submitRow with invalid word returns WordNotInList error`() {
        val state = initialState()
        // "CRANK" not in validWords
        val guess = listOf('C','R','A','N','K').map { Character(it.toString()) }
        val currentRow = state.rows[0].copy(entries = guess)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertTrue(newState.validationError is ValidationError.WordNotInList)
        assertEquals(GameState.Running, newState.gameState)
    }

    @Test
    fun `submitRow with correct guess results in Won state`() {
        val state = initialState()
        val guess = targetWord.map { Character(it.toString()) }
        val currentRow = state.rows[0].copy(entries = guess)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertEquals(GameState.Won, newState.gameState)
        assertNull(newState.validationError)
        // Check that row is guessed state
        assertEquals(RowState.GUESSED, newState.rows[0].state)
    }

    @Test
    fun `submitRow with incorrect guess on last row results in Lost state`() {
        val state = initialState().copy(
            rows = initialRows.mapIndexed { i, row ->
                // Make all but the last row guessed
                if (i < 5) row.copy(state = RowState.GUESSED) else row
            }
        )
        val guess = listOf('C','R','A','Z','Y').map { Character(it.toString()) }
        val currentRow = state.rows[5].copy(entries = guess, state = RowState.ACTIVE)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertTrue(newState.gameState is GameState.Lost)
        assertNull(newState.validationError)
    }

    @Test
    fun `submitRow with partial matches updates usedCharacters`() {
        val state = initialState()
        // Guess: CRACK vs Target: CRANE
        // C, R, A match in position, C is not in last position, K no match
        val guess = "CRACK".map { Character(it.toString()) }
        val currentRow = state.rows[0].copy(entries = guess)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertNull(newState.validationError)
        assertEquals(GameState.Running, newState.gameState)

        // After evaluation:
        // C,R,A are MATCH_IN_POSITION
        // The second C (4th position) is NO_MATCH because no extra C in "CRANE"
        // K is NO_MATCH
        // usedCharacters should reflect best known states:
        // C -> MATCH_IN_POSITION, R -> MATCH_IN_POSITION, A -> MATCH_IN_POSITION
        // K -> NO_MATCH

        assertEquals(CharState.MATCH_IN_POSITION, newState.usedCharacters["C"])
        assertEquals(CharState.MATCH_IN_POSITION, newState.usedCharacters["R"])
        assertEquals(CharState.MATCH_IN_POSITION, newState.usedCharacters["A"])
        assertEquals(CharState.NO_MATCH, newState.usedCharacters["K"])
    }

    @Test
    fun `modifyRowEntries adds characters to a row`() {
        val state = initialState()
        val currentRow = state.rows[0]
        val newState = modifyRowEntries(state, currentRow, addChar = "C")
        val updatedRow = newState.rows[0]
        assertEquals("C", updatedRow.entries[0].char)
    }

    @Test
    fun `modifyRowEntries removes last character from a row`() {
        val state = initialState()
        // Create a copy of the first row with all entries filled
        val rowWithChars = state.rows[0].copy(entries = listOf(
            Character("C"),
            Character("R"),
            Character("A"),
            Character("N"),
            Character("E")
        ))
        // Construct a new state that uses rowWithChars as the first row
        val modifiedState = state.copy(rows = state.rows.mapIndexed { index, row ->
            if (index == 0) rowWithChars else row
        })

        // Now use modifyRowEntries directly
        val newState = modifyRowEntries(modifiedState, rowWithChars, removeLast = true)
        val updatedRow = newState.rows[0]
        // Last character should now be empty
        assertEquals("", updatedRow.entries[4].char)
    }

    @Test
    fun `missing word in word list validation`() {
        val state = initialState()
        val guess = listOf('F','O','O','B','A').map { Character(it.toString()) }
        val currentRow = state.rows[0].copy(entries = guess)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertTrue(newState.validationError is ValidationError.WordNotInList)
    }

    @Test
    fun `hard mode position lock validation`() {
        val state = initialState(hardMode = true).copy(
            positionLocks = mapOf(0 to 'C') // Must use 'C' at position 0
        )
        val guess = listOf('G','R','A','N','D').map { Character(it.toString()) }
        val currentRow = state.rows[0].copy(entries = guess)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertTrue(newState.validationError is ValidationError.MissingPositionChar)
    }

    @Test
    fun `hard mode required char validation`() {
        val state = initialState(hardMode = true).copy(
            requiredChars = mapOf('C' to 2) // Must use 'C' at least twice
        )
        val guess = listOf('C','R','A','N','E').map { Character(it.toString()) }
        val currentRow = state.rows[0].copy(entries = guess)

        val newState = submitRow(state, currentRow, targetWord, isValidWord)
        assertTrue(newState.validationError is ValidationError.MissingRequiredChar)
    }
}
