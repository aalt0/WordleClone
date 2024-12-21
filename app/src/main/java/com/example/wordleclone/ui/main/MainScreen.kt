package com.example.wordleclone.ui.main

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.domain.model.CharState
import com.example.wordleclone.domain.model.Character
import com.example.wordleclone.domain.model.GameDomainState
import com.example.wordleclone.domain.model.GameRow
import com.example.wordleclone.domain.model.GameState
import com.example.wordleclone.domain.model.RowState
import com.example.wordleclone.ui.keyboard.Keyboard
import com.example.wordleclone.ui.keyboard.KeyboardKey
import com.example.wordleclone.ui.theme.WordleCloneTheme

@Composable
fun MainScreen(
    uiState: GameUiState,
    onKeyPressed: (KeyboardKey) -> Unit,
    onResetClicked: () -> Unit,
    onAnimationEnded: () -> Unit
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val statusMessage = statusMessage(uiState.domainState.gameState, uiState.errorMessage)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            TopBar(statusMessage, onResetClicked)
            GameBoard(
                rows = uiState.domainState.rows,
                animatingRowIndex = uiState.animatingRowIndex,
                invalidGuessRowIndex = uiState.invalidGuessRowIndex,
                onAnimationEnded = onAnimationEnded
            )
            Spacer(Modifier.height(10.dp))
            Keyboard(onKeyPressed = onKeyPressed, usedCharacters = uiState.usedCharacters)
        }
    }
}

@Composable
private fun TopBar(message: String, onResetClicked: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .clickable { onResetClicked() }
                .border(2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .height(30.dp)
                .width(60.dp)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Reset", fontSize = 12.sp)
        }
    }
}

@Composable
private fun GameBoard(
    rows: List<GameRow>,
    animatingRowIndex: Int?,
    invalidGuessRowIndex: Int?,
    onAnimationEnded: () -> Unit
) {
    rows.forEach { row ->
        val isAnimating = animatingRowIndex == row.rowNumber
        val shouldShake = invalidGuessRowIndex == row.rowNumber

        ShakingRow(
            row = row,
            shake = shouldShake,
            isAnimating = isAnimating,
            onRowAnimationEnded = onAnimationEnded,
            onCharacterAnimationEnded = onAnimationEnded
        )
    }
}

private fun statusMessage(gameState: GameState, errorMessage: String?): String {
    return when (gameState) {
        is GameState.Won -> "Woohoo, you win!"
        is GameState.Lost -> buildAnnotatedString {
            append("No more guesses. The word was ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(gameState.correctWord) }
        }.toString()
        GameState.Loading -> "Loading ..."
        GameState.Running -> errorMessage ?: " "
    }
}

@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
annotation class OrientationPreviews

@OrientationPreviews
@Composable
private fun MainScreenPreview() {
    WordleCloneTheme {
        MainScreen(uiState = previewGameUiState, onKeyPressed = {}, onResetClicked = {}, onAnimationEnded = {})
    }
}

private val dummyRows = listOf(
    GameRow(
        rowNumber = 0,
        state = RowState.ACTIVE,
        entries = listOf(
            Character("B", CharState.MATCH_IN_POSITION),
            Character("R", CharState.MATCH_IN_WORD),
            Character("E", CharState.MATCH_IN_WORD),
            Character("A", CharState.NO_MATCH),
            Character("D", CharState.MATCH_IN_POSITION),
        )
    ),
    GameRow(rowNumber = 1),
    GameRow(rowNumber = 2),
    GameRow(rowNumber = 3),
    GameRow(rowNumber = 4),
    GameRow(rowNumber = 5)
)

val previewGameUiState = GameUiState(
    usedCharacters = mapOf(
        "A" to CharState.NO_MATCH,
        "D" to CharState.MATCH_IN_WORD,
        "G" to CharState.MATCH_IN_POSITION,
    ),
    domainState = GameDomainState(
        gameState = GameState.Running,
        rows = dummyRows,
    )
)
