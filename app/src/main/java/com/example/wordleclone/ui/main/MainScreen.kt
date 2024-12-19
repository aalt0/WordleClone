package com.example.wordleclone.ui.main

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.ui.keyboard.Keyboard
import com.example.wordleclone.ui.keyboard.KeyboardKey
import com.example.wordleclone.ui.theme.WordleCloneTheme

@Composable
fun MainScreen(uiState: MainUiState, onKeyPressed: (KeyboardKey) -> Unit) {
    Surface(
        modifier = Modifier.padding(12.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val status = uiState.status) {
            is GameState.Error -> {
                status.error
                Text("error horror")
            }
            is GameState.Loading -> {
                Text("Loading ...")
            }
            is GameState.Running -> {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    uiState.rows.forEach { row -> SingleRow(row) }
                    Spacer(Modifier.height(10.dp))
                    Keyboard(onKeyPressed)
                }
            }
            is GameState.Lost -> TODO()
            is GameState.Won -> TODO()
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

    val modifier = when (char.charState) {
        CharState.NO_MATCH -> rowModifier.background(Color.LightGray)
        CharState.MATCH_IN_WORD -> rowModifier.background(Color.Blue)
        CharState.MATCH_IN_POSITION -> rowModifier.background(Color.Green)
    }

    Box(
        modifier = modifier
            .border(width = 5.dp, color = Color.DarkGray)
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char.char,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.background(Transparent),
            minLines = 1,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Landscape Mode", showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 640)
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
annotation class OrientationPreviews

//@OrientationPreviews
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
@Composable
private fun MainScreenPreview() {
    WordleCloneTheme {
        MainScreen(uiState = testMainUiState, onKeyPressed = {})
    }
}

val testMainUiState = MainUiState(
    status = GameState.Running,
    rows = listOf(
        GameRow(
            rowNumber = 0,
            rowState = RowState.GUESSED,
            entries = listOf(
                Character("T", CharState.MATCH_IN_POSITION),
                Character("R", CharState.MATCH_IN_POSITION),
                Character("U", CharState.NO_MATCH),
                Character("M", CharState.NO_MATCH),
                Character("P", CharState.NO_MATCH),
            ),
        ),
        GameRow(
            rowNumber = 1,
            rowState = RowState.GUESSED,
            entries = listOf(
                Character("T", CharState.MATCH_IN_POSITION),
                Character("R", CharState.MATCH_IN_POSITION),
                Character("A", CharState.MATCH_IN_POSITION),
                Character("C", CharState.NO_MATCH),
                Character("E", CharState.MATCH_IN_POSITION),
            )
        ),
        GameRow(
            rowNumber = 2,
            rowState = RowState.MATCH,
            entries = listOf(
                Character("T", CharState.MATCH_IN_POSITION),
                Character("R", CharState.MATCH_IN_POSITION),
                Character("A", CharState.MATCH_IN_POSITION),
                Character("D", CharState.MATCH_IN_POSITION),
                Character("E", CharState.MATCH_IN_POSITION),
            )
        ),
        GameRow(rowNumber = 3),
        GameRow(rowNumber = 4),
        GameRow(rowNumber = 5),
    )
)

class MainUiStatePreviewProvider : PreviewParameterProvider<MainUiState> {
    override val values= sequenceOf(
        testMainUiState
    )
}