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
        val word = when (uiState.status) {
            Word.Loading -> "Loading ..."
            is Word.Error -> uiState.status.error
            is Word.Success -> uiState.status.word
        }

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            SingleRow("TRACE")
            SingleRow("DWER")
            SingleRow("")
            SingleRow("")
            SingleRow("")
            SingleRow("")
            Spacer(Modifier.height(10.dp))
            Keyboard(onKeyPressed)
        }
    }
}

@Composable
fun SingleRow(word: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        (0..4).forEach { index ->
            val char = word.getOrNull(index)?.toString() ?: ""
            Box(
                modifier = Modifier.weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(width = 5.dp, color = Color.DarkGray)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = char,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Transparent),
                    minLines = 1,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
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

val testMainUiState = MainUiState(status = Word.Success(word = "trace"))
class MainUiStatePreviewProvider : PreviewParameterProvider<MainUiState> {
    override val values= sequenceOf(
        testMainUiState
    )
}