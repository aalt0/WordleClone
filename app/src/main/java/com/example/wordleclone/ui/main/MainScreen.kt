package com.example.wordleclone.ui.main

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.ui.keyboard.Keyboard
import com.example.wordleclone.ui.keyboard.KeyboardKey
import com.example.wordleclone.ui.theme.WordleCloneTheme
import com.example.wordleclone.ui.theme.matchInPositionColor
import com.example.wordleclone.ui.theme.matchInWordColor
import com.example.wordleclone.ui.theme.noMatchColor

@Composable
fun MainScreen(
    uiState: GameUiState,
    onKeyPressed: (KeyboardKey) -> Unit,
    onResetClicked: () -> Unit
) {
    // The UI only works in portrait mode
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    Surface(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val message = when (val status = uiState.status) {
                is GameState.Error -> status.message
                is GameState.Won -> "Woohoo, you win!"
                is GameState.Lost -> buildAnnotatedString {
                    append("No more guesses. The word was ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(status.correctWord)
                    }
                }.toString()
                is GameState.Loading -> "Loading ..."
                else -> " " // else the game is running, return single space to use the Text() as a spacer
            }

            Text(
                text = message,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onBackground
            )

            uiState.rows.forEach { row -> SingleRow(row) }
            Spacer(Modifier.height(10.dp))
            Keyboard(onKeyPressed)
            Spacer(Modifier.height(20.dp))
            ElevatedButton(onClick = onResetClicked) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun SingleRow(row: GameRow) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        val rowModifier = Modifier.weight(1f)
        row.entries.forEach { char ->
            SingleCharacter(char, rowModifier)
        }
    }
}

@Composable
fun SingleCharacter(char: Character, rowModifier: Modifier) {

    val backgroundColor = when (char.charState) {
        CharState.NO_MATCH -> MaterialTheme.noMatchColor
        CharState.MATCH_IN_WORD -> MaterialTheme.matchInWordColor
        CharState.MATCH_IN_POSITION -> MaterialTheme.matchInPositionColor
    }

    val borderColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = rowModifier
            .background(backgroundColor)
            .border(width = 5.dp, color = borderColor)
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char.char,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            minLines = 1,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

//@Preview(name = "Landscape Mode", showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 640)
//@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
annotation class OrientationPreviews

@OrientationPreviews
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
@Composable
private fun MainScreenPreview() {
    WordleCloneTheme {
        MainScreen(uiState = previewGameUiState, onKeyPressed = {}, onResetClicked = {})
    }
}

private val dummyRows = listOf(
    GameRow(
        rowNumber = 0,
        state = RowState.GUESSED,
        entries = listOf(
            Character("T", CharState.NO_MATCH),
            Character("R", CharState.MATCH_IN_POSITION),
            Character("U", CharState.NO_MATCH),
            Character("M", CharState.NO_MATCH),
            Character("P", CharState.NO_MATCH),
        )
    ),
    GameRow(
        rowNumber = 1,
        state = RowState.GUESSED,
        entries = listOf(
            Character("B", CharState.MATCH_IN_POSITION),
            Character("O", CharState.NO_MATCH),
            Character("R", CharState.MATCH_IN_WORD),
            Character("E", CharState.MATCH_IN_WORD),
            Character("D", CharState.MATCH_IN_POSITION),
        )
    ),
    GameRow(
        rowNumber = 2,
        state = RowState.ACTIVE,
        entries = listOf(
            Character("B", CharState.MATCH_IN_POSITION),
            Character("R", CharState.MATCH_IN_POSITION),
            Character("E", CharState.MATCH_IN_POSITION),
            Character("A", CharState.MATCH_IN_POSITION),
            Character("D", CharState.MATCH_IN_POSITION),
        )
    ),
    GameRow(rowNumber = 3),
    GameRow(rowNumber = 4),
    GameRow(rowNumber = 5)
)

val previewGameUiState = GameUiState(
    status = GameState.Running,
    rows = dummyRows
)
