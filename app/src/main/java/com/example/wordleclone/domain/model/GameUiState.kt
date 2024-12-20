package com.example.wordleclone.domain.model

data class GameUiState(
    val rows: List<GameRow> = List(6) { index ->
        GameRow(
            rowNumber = index,
            state = if (index == 0) RowState.ACTIVE else RowState.INACTIVE
        )
    },
    val status: GameState = GameState.Loading,
    val errorMessage: String? = null
)
