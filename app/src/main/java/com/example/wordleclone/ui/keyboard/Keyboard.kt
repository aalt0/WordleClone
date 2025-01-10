package com.example.wordleclone.ui.keyboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.domain.model.CharState
import com.example.wordleclone.ui.theme.WordleCloneTheme
import com.example.wordleclone.ui.theme.keyboardButtonColor
import com.example.wordleclone.ui.theme.keyboardButtonNoMatchColor
import com.example.wordleclone.ui.theme.matchInPositionColor
import com.example.wordleclone.ui.theme.matchInWordColor
import com.example.wordleclone.util.applyWhen

@Composable
fun Keyboard(
    onKeyPressed: (KeyboardKey) -> Unit,
    usedCharacters: Map<String, CharState>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        KeyboardRow(TOP_ROW, onKeyPressed, usedCharacters)
        Spacer(Modifier.height(10.dp))
        KeyboardRow(MID_ROW, onKeyPressed, usedCharacters)
        Spacer(Modifier.height(10.dp))
        KeyboardRow(BOTTOM_ROW, onKeyPressed, usedCharacters)
    }
}

@Composable
fun KeyboardRow(
    keys: List<KeyboardKey>,
    onKeyPressed: (KeyboardKey) -> Unit,
    usedCharacters: Map<String, CharState>
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
                        .padding(horizontal = 3.dp),
                    onKeyPressed = onKeyPressed,
                    usedCharacters = usedCharacters
                )
            }
        }
    }
}

@Composable
fun KeyboardButton(
    key: KeyboardKey,
    modifier: Modifier = Modifier,
    onKeyPressed: (KeyboardKey) -> Unit,
    usedCharacters: Map<String, CharState>
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    val keyCharState = usedCharacters[key.name]

    val backgroundColor = when {
        isPressed -> MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        keyCharState == CharState.MATCH_IN_POSITION -> MaterialTheme.matchInPositionColor
        keyCharState == CharState.MATCH_IN_WORD -> MaterialTheme.matchInWordColor
        keyCharState == CharState.NO_MATCH -> MaterialTheme.keyboardButtonNoMatchColor
        else -> MaterialTheme.keyboardButtonColor
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

@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
annotation class OrientationPreviews
//@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
@OrientationPreviews
@Composable
private fun KeyboardPreview() {
    WordleCloneTheme {
        Keyboard(
            onKeyPressed = {},
            usedCharacters = mapOf(
                "A" to CharState.NO_MATCH,
                "D" to CharState.MATCH_IN_WORD,
                "G" to CharState.MATCH_IN_POSITION,
            )
        )
    }
}
