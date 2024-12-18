package com.example.wordleclone.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.wordleclone.ui.theme.WordleCloneTheme

@Composable
fun MainScreen(uiState: MainUiState) {
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
            SingleRow("")
            SingleRow("")
            SingleRow("")
            SingleRow("")
        }
    }
}

@Composable
fun SingleRow(word: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        (0..4).map { i ->
            val c = word.getOrNull(i)?.toString() ?: ""
            CharInput(c)
        }
    }
}

@Composable
fun RowScope.CharInput(char: String) {

    var value by remember { mutableStateOf(char) }

    val modifier = Modifier
        .background(Color.LightGray)
        .border(width = 5.dp, color = Color.DarkGray)
        .fillMaxWidth()
        .height(IntrinsicSize.Max)
        .weight(1f)
        .aspectRatio(1f)

    val textStyle = LocalTextStyle.current.copy(
        textAlign = TextAlign.Center,
        fontSize = TextUnit(40f, TextUnitType.Sp)
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            modifier = Modifier.background(Transparent),
            textStyle = textStyle,
            value = value,
            onValueChange = {
                if (it.contains(ALLOWED_CHARS)) value = it
            },
            singleLine = true,
            cursorBrush = SolidColor(Transparent)
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
        MainScreen(uiState = testMainUiState)
    }
}

val testMainUiState = MainUiState(status = Word.Success(word = "trace"))
class MainUiStatePreviewProvider : PreviewParameterProvider<MainUiState> {
    override val values= sequenceOf(
        testMainUiState
    )
}