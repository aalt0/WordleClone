package com.example.wordleclone.domain.model

data class GameUiState(
    val rows: List<GameRow> = defaultRowsState,
    val status: GameState = GameState.Loading,
    val errorMessage: String? = null,
    val usedCharacters: Map<String, CharState> = emptyMap(),
    val hardMode: Boolean = false,
    val positionLocks: Map<Int, Char> = emptyMap(), // position -> character that must be used
    val requiredChars: Map<Char, Int> = emptyMap() // character -> minimum occurrences required
)

private val defaultRowsState = List(6) { index ->
    GameRow(
        rowNumber = index,
        state = if (index == 0) RowState.ACTIVE else RowState.INACTIVE
    )
}