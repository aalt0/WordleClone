package com.example.wordleclone.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
            for (key in keys) {
                val multiChar = key.name.length > 1
                KeyboardButton(
                    key = key,
                    modifier = Modifier
                        .height(56.dp)
                        .applyWhen(multiChar, block = { weight(1f) })
                        .width(itemWidth)
                        .padding(horizontal = 4.dp),
                    onKeyPressed = onKeyPressed
                )
            }
        }
    }
}

@Composable
fun KeyboardButton(
    key: KeyboardKey,
    modifier: Modifier = Modifier,
    onKeyPressed: (KeyboardKey) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    // Dynamic background colors based on press state
    val backgroundColor: Color = if (isPressed) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp) // Darker gray for pressed state
    } else {
        MaterialTheme.colorScheme.surfaceVariant // Default light gray
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)) // Rounded corners for modern look
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null // Avoid default ripple, using manual styling
            ) { onKeyPressed(key) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key.name,
            style = TextStyle(
                fontSize = if (key.name.length > 1) 12.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface // Text color adapts to theme
            )
        )
    }
}

@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
//@OrientationPreviews
@Composable
private fun KeyboardPreview() {
    WordleCloneTheme { Keyboard(onKeyPressed = {}) }
}
