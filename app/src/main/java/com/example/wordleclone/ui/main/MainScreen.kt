package com.example.wordleclone.ui.main

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.domain.model.Character
import com.example.wordleclone.domain.model.CharState
import com.example.wordleclone.domain.model.GameDomainState
import com.example.wordleclone.domain.model.GameRow
import com.example.wordleclone.domain.model.GameState
import com.example.wordleclone.domain.model.RowState
import com.example.wordleclone.ui.keyboard.Keyboard
import com.example.wordleclone.ui.keyboard.KeyboardKey
import com.example.wordleclone.ui.theme.WordleCloneTheme
import com.example.wordleclone.ui.theme.matchInPositionColor
import com.example.wordleclone.ui.theme.matchInWordColor
import com.example.wordleclone.ui.theme.noMatchColor
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    uiState: GameUiState,
    onKeyPressed: (KeyboardKey) -> Unit,
    onResetClicked: () -> Unit,
    onAnimationEnded: () -> Unit
) {
    // The UI only works in portrait mode
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    Surface(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val message = when (val gameState = uiState.domainState.gameState) {
                is GameState.Won -> "Woohoo, you win!"
                is GameState.Lost -> buildAnnotatedString {
                    append("No more guesses. The word was ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(gameState.correctWord)
                    }
                }.toString()
                is GameState.Loading -> "Loading ..."
                is GameState.Running -> uiState.errorMessage ?: " "
            }

            Text(
                text = message,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onBackground
            )

            // wip
            uiState.domainState.rows.forEach { row ->
//                SingleRow(row)
                val isAnimating = uiState.animatingRowIndex == row.rowNumber
                val shouldShake = uiState.invalidGuessRowIndex == row.rowNumber

                ShakingRow(
                    row = row,
                    shake = shouldShake,
                    isAnimating = isAnimating,
                    onAnimationEnded = onAnimationEnded
                )
            }

            Spacer(Modifier.height(10.dp))
            Keyboard(onKeyPressed, uiState.domainState.usedCharacters)
            Spacer(Modifier.height(20.dp))
            ElevatedButton(onClick = onResetClicked) {
                Text("Reset")
            }
        }
    }
}

//@Composable
//fun SingleRow(row: GameRow, animating: Boolean) {
//    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
//        val rowModifier = Modifier.weight(1f)
//        row.entries.forEachIndexed { index, char ->
//            AnimatedCharacter(
//                char = char,
//                rowModifier = rowModifier,
//                isAnimating = animating,
//                animationDelay = index
//            )
//        }
//    }
//}

@Composable
fun ShakingRow(
    row: GameRow,
    shake: Boolean,
    isAnimating: Boolean,
    onAnimationEnded: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(shake) {
        if (shake) {
            // Perform a quick shake animation
            val shakeDistance = 10f
            repeat(3) {
                offsetX.animateTo(shakeDistance, tween(50))
                offsetX.animateTo(-shakeDistance, tween(50))
            }
            offsetX.animateTo(0f, tween(50))
            onAnimationEnded()
        } else {
            offsetX.snapTo(0f)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.offset { IntOffset(x = offsetX.value.dp.value.toInt(), y = 0) }
    ) {
        val rowModifier = Modifier.weight(1f)
        row.entries.forEachIndexed { index, char ->
            AnimatedCharacter(
                char = char,
                rowModifier = rowModifier,
                isAnimating = isAnimating,
                animationDelay = index
            )
        }
    }
}

@Composable
fun AnimatedCharacter(
    char: Character,
    rowModifier: Modifier,
    isAnimating: Boolean,
    animationDelay: Int = 0 // stagger flips by index
) {
    // Track rotation (0f .. 180f)
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            // Delay start to stagger animations
            delay(animationDelay * 100L)
            // Animate half flip
            rotation.animateTo(90f, animationSpec = tween(durationMillis = 250))
            // At half flip, the character state would be revealed if we were swapping char states here
            // Animate second half
            rotation.animateTo(180f, animationSpec = tween(durationMillis = 250))
        } else {
            rotation.snapTo(0f)
        }
    }

    val flipProgress = rotation.value
    // If flipProgress < 90, show old state side; if > 90, show new state side
    val shownChar = char.char
    val shownState = char.charState
    val backgroundColor = when (shownState) {
        CharState.NO_MATCH -> MaterialTheme.noMatchColor
        CharState.MATCH_IN_WORD -> MaterialTheme.matchInWordColor
        CharState.MATCH_IN_POSITION -> MaterialTheme.matchInPositionColor
    }

    // 3D Flip effect: rotate along X-axis or Y-axis
    val cameraDis = 8 * LocalDensity.current.density // Makes rotation look more realistic
    Box(
        modifier = rowModifier
            .graphicsLayer {
                rotationX = flipProgress
                cameraDistance = cameraDis
            }
            .background(backgroundColor)
            .border(width = 5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shownChar,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

/*
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
 */

//@Preview(name = "Landscape Mode", showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 640)
//@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
annotation class OrientationPreviews

@OrientationPreviews
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
@Composable
private fun MainScreenPreview() {
    WordleCloneTheme {
        MainScreen(uiState = previewGameUiState, onKeyPressed = {}, onResetClicked = {}, onAnimationEnded = {})
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
    GameDomainState(
        gameState = GameState.Running,
        rows = dummyRows,
        usedCharacters = mapOf(
            "A" to CharState.NO_MATCH,
            "D" to CharState.MATCH_IN_WORD,
            "G" to CharState.MATCH_IN_POSITION,
        )
    )
)
