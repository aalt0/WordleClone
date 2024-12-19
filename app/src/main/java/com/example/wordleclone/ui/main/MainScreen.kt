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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.wordleclone.ui.keyboard.Keyboard
import com.example.wordleclone.ui.theme.WordleCloneTheme
import java.util.Locale

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
            SingleRow("TRACE", false)
            SingleRow("DWERS", true)
            SingleRow("", false)
            SingleRow("", false)
            SingleRow("", false)
            SingleRow("", false)
            Spacer(Modifier.height(10.dp))
            Keyboard(onKeyPressed = {})
        }
    }
}

@Composable
fun SingleRow(word: String, focusable: Boolean) {

    val textStates = remember {
        MutableList(5) { index ->
            val char = word.getOrNull(index)?.toString() ?: ""
            mutableStateOf(TextFieldValue(char, TextRange(char.length)))
        }
    }

    val focusRequesters = if (focusable) {
        List(5) { remember { FocusRequester() } }
    } else {
        emptyList()
    }

    if (focusable) {
        val firstEmptyIndex = textStates.indexOfFirst { it.value.text.isEmpty() }
        val focusableIndex = if (firstEmptyIndex == -1) {
            4
        } else {
            firstEmptyIndex
        }
        LaunchedEffect(Unit) {
            focusRequesters[focusableIndex].requestFocus()
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        (0..4).forEach { index ->
            CharInput(
                char = textStates[index].value,
                focusable = focusable,
                focusRequester = focusRequesters.getOrNull(index),
                rowItemModifier = Modifier.weight(1f),
                onValueChange = { newChar ->
                    val c = newChar.text.uppercase(Locale.US)
                    if (c.contains(ALLOWED_CHARS)) {
                        textStates[index].value = newChar.copy(c)
                    }
                },
                onNextFocus = {
                    focusRequesters.getOrNull(index+1)?.requestFocus()
                },
                onPrevFocus = {
                    focusRequesters.getOrNull(index-1)?.requestFocus()
                }
            )
        }
    }
}

@Composable
fun CharInput(
    char: TextFieldValue,
    focusable: Boolean,
    focusRequester: FocusRequester?,
    rowItemModifier: Modifier,
    onValueChange: (TextFieldValue) -> Unit,
    onNextFocus: () -> Unit,
    onPrevFocus: () -> Unit
) {

    val textStyle = LocalTextStyle.current.copy(
        textAlign = TextAlign.Center,
        fontSize = TextUnit(40f, TextUnitType.Sp)
    )

    val transparentTextSelectionColors = TextSelectionColors(
        handleColor = Transparent,
        backgroundColor = Transparent,
    )

    Box(
        modifier = rowItemModifier
            .background(Color.LightGray)
            .border(width = 5.dp, color = Color.DarkGray)
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalTextToolbar provides EmptyTextToolbar,
            LocalTextSelectionColors provides transparentTextSelectionColors
        ) {
            BasicTextField(
                modifier = if (focusable) {
                    Modifier.focusProperties { canFocus = true }
                        .focusRequester(focusRequester!!)
                } else {
                    Modifier.focusProperties { canFocus = false }
                }
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyUp && it.key == Key.Backspace) {
                        onPrevFocus()
                        true
                    } else {
                        false
                    }
                }.background(Transparent),
                textStyle = textStyle,
                value = char,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 1) {
                        onValueChange(newValue)
                    }
                    if (newValue.text.length == 1) {
                        onNextFocus()
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
//            cursorBrush = SolidColor(Transparent)
            )
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
        MainScreen(uiState = testMainUiState)
    }
}

val testMainUiState = MainUiState(status = Word.Success(word = "trace"))
class MainUiStatePreviewProvider : PreviewParameterProvider<MainUiState> {
    override val values= sequenceOf(
        testMainUiState
    )
}