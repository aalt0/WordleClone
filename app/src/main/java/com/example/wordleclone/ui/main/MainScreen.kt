package com.example.wordleclone.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.wordleclone.ui.theme.WordleCloneTheme

@Composable
fun MainScreen(uiState: MainUiState) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(24.dp).padding(top = 24.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        val word = when (uiState.status) {
            Word.Loading -> "Loading ..."
            is Word.Error -> uiState.status.error
            is Word.Success -> uiState.status.word
        }
        Text(word)
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
        MainScreen(uiState = testMainUiState)
    }
}

val testMainUiState = MainUiState(status = Word.Success(word = "trace"))
class MainUiStatePreviewProvider : PreviewParameterProvider<MainUiState> {
    override val values= sequenceOf(
        testMainUiState
    )
}