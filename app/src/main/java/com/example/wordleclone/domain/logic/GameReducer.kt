package com.example.wordleclone.domain.logic

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
        is GameAction.ResetGame -> GameUiState(hardMode = state.hardMode)
        is GameAction.KeyPress -> {
            val clearedState = state.copy(errorMessage = null)
            if (clearedState.status !is GameState.Running) return clearedState

            val currentRow = clearedState.rows.firstOrNull { it.state == RowState.ACTIVE } ?: return clearedState

            when (action.key) {
                KeyboardKey.ENTER -> submitRow(clearedState, currentRow, targetWord, isValidWord)
                KeyboardKey.DEL -> modifyRowEntries(clearedState, currentRow, removeLast = true)
                else -> modifyRowEntries(clearedState, currentRow, addChar = action.key.name)
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
