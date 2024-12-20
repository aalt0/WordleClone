package com.example.wordleclone.domain.logic

import com.example.wordleclone.domain.model.GameRow
import com.example.wordleclone.domain.model.GameState
import com.example.wordleclone.domain.model.GameUiState
import com.example.wordleclone.domain.model.RowState
import com.example.wordleclone.ui.keyboard.KeyboardKey

fun reduce(
    state: GameUiState,
    action: GameAction,
    targetWord: String,
    isValidWord: (String) -> Boolean
): GameUiState {
    return when (action) {
        is GameAction.ResetGame -> state.copy(
            rows = List(6) { GameRow(it, if (it == 0) RowState.ACTIVE else RowState.INACTIVE) },
            status = GameState.Loading
        )
        is GameAction.KeyPress -> {
            val clearedState = state.copy(errorMessage = null)
            if (clearedState.status !is GameState.Running) return clearedState

            val currentRow = clearedState.rows.firstOrNull { it.state == RowState.ACTIVE } ?: return clearedState

            when (action.key) {
                KeyboardKey.ENTER -> submitRow(state, currentRow, targetWord, isValidWord)
                KeyboardKey.DEL -> modifyRowEntries(state, currentRow, removeLast = true)
                else -> modifyRowEntries(state, currentRow, addChar = action.key.name)
            }
        }
        is GameAction.WordFetched -> state.copy(
            status = GameState.Running
        )
        is GameAction.FetchError -> state.copy(
            status = GameState.Loading,
            errorMessage = action.message
        )
        GameAction.SubmitActiveRow -> {
            val currentRow = state.rows.firstOrNull { it.state == RowState.ACTIVE } ?: return state
            submitRow(state, currentRow, targetWord, isValidWord)
        }
    }
}
