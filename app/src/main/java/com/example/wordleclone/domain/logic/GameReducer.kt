package com.example.wordleclone.domain.logic

import com.example.wordleclone.domain.model.GameState
import com.example.wordleclone.domain.model.GameDomainState
import com.example.wordleclone.domain.model.RowState
import com.example.wordleclone.ui.keyboard.KeyboardKey

fun reduce(
    state: GameDomainState,
    action: GameAction,
    targetWord: String,
    isValidWord: (String) -> Boolean
): GameDomainState {
    return when (action) {
        is GameAction.ResetGame -> GameDomainState(hardMode = state.hardMode)
        is GameAction.WordFetched -> state.copy(gameState = GameState.Running)
        is GameAction.FetchError -> state.copy(gameState = GameState.Loading)
        is GameAction.KeyPress -> {
            val clearedState = state.copy(validationError = null)
            if (clearedState.gameState !is GameState.Running) {
                return clearedState
            }

            val currentRow = clearedState.rows.firstOrNull { it.state == RowState.ACTIVE }
            if (currentRow == null) {
                return clearedState
            }

            when (action.key) {
                KeyboardKey.ENTER -> submitRow(clearedState, currentRow, targetWord, isValidWord)
                KeyboardKey.DEL -> modifyRowEntries(clearedState, currentRow, removeLast = true)
                else -> modifyRowEntries(clearedState, currentRow, addChar = action.key.name)
            }
        }
    }
}
