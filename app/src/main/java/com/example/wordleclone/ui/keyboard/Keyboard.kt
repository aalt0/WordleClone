package com.example.wordleclone.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.ui.theme.WordleCloneTheme
import com.example.wordleclone.utli.applyWhen

@Composable
fun Keyboard(onKeyPressed: (KeyboardKey) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        KeyboardRow(TOP_ROW, onKeyPressed)
        Spacer(Modifier.height(16.dp))
        KeyboardRow(MID_ROW, onKeyPressed)
        Spacer(Modifier.height(16.dp))
        KeyboardRow(BOTTOM_ROW, onKeyPressed)
    }
}

@Composable
fun KeyboardRow(
    keys: List<KeyboardKey>,
    onKeyPressed: (KeyboardKey) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemWidth = maxWidth / 10
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val buttonModifier = Modifier
                .height(56.dp)
                .width(itemWidth)
                .padding(start = 4.dp, end = 4.dp)
                .background(MaterialTheme.colorScheme.inversePrimary)

            for (key in keys) {
                val multiChar = key.name.length > 1
                Box(
                    modifier = buttonModifier.applyWhen(multiChar, block = { weight(1f) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key.name,
                        maxLines = 1,
                        minLines = 1,
                        textAlign = TextAlign.Center,
                        fontSize = if (multiChar) 12.sp else 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
//@OrientationPreviews
@Composable
private fun KeyboardPreview() {
    WordleCloneTheme { Keyboard(onKeyPressed = {}) }
}
