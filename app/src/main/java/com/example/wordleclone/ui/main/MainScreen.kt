package com.example.wordleclone.ui.main

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

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
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
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

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier.clickable { onResetClicked() }
                        .border(2.dp, color = Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .height(30.dp).width(60.dp)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Reset", fontSize = 12.sp)
                }
            }

            uiState.domainState.rows.forEach { row ->
                val isAnimating = uiState.animatingRowIndex == row.rowNumber
                val shouldShake = uiState.invalidGuessRowIndex == row.rowNumber

                ShakingRow(
                    row = row,
                    shake = shouldShake,
                    isAnimating = isAnimating,
                    onRowAnimationEnded = {
                        // This is called once the row shaking animation ends
                        onAnimationEnded()
                    },
                    onCharacterAnimationEnded = {
                        // This is called once the last character in the row finishes flipping
                        onAnimationEnded()
                    }
                )
            }

            Spacer(Modifier.height(10.dp))
            Keyboard(onKeyPressed, uiState.usedCharacters)
        }
    }
}

@Composable
fun ShakingRow(
    row: GameRow,
    shake: Boolean,
    isAnimating: Boolean,
    onRowAnimationEnded: () -> Unit,
    onCharacterAnimationEnded: () -> Unit
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
            onRowAnimationEnded()
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
                animationDelay = index,
                onCharacterAnimationEnded = {
                    // only need to signal the last character animation ending
                    if (index == row.entries.lastIndex) onCharacterAnimationEnded()
                }
            )
        }
    }
}

private const val tweenDuration = 200

@Composable
fun AnimatedCharacter(
    char: Character,
    rowModifier: Modifier,
    isAnimating: Boolean,
    animationDelay: Int = 0, // stagger flips by index
    onCharacterAnimationEnded: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            launch {
                delay(animationDelay * 75L)
                // Phase 1: 0° to 85° (hide old state)
                rotation.animateTo(
                    targetValue = 80f,
                    animationSpec = tween(
                        durationMillis = tweenDuration,
                        easing = FastOutSlowInEasing
                    )
                )
                revealed = true
                // Phase 2: 85° back to 0° (show new state right-side up)
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = tweenDuration,
                        easing = FastOutSlowInEasing
                    )
                )
                onCharacterAnimationEnded()
            }
            launch {
                delay(animationDelay * 75L)
                alpha.animateTo(
                    targetValue = 0.70f,
                    animationSpec = tween(durationMillis = tweenDuration)
                )
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = tweenDuration)
                )
            }
        } else {
            revealed = false
            rotation.snapTo(0f)
            alpha.snapTo(1f)
        }
    }

    val shownState = char.charState
    val backgroundColor = if (!isAnimating || revealed) {
        when (shownState) {
            CharState.NO_MATCH -> MaterialTheme.noMatchColor
            CharState.MATCH_IN_WORD -> MaterialTheme.matchInWordColor
            CharState.MATCH_IN_POSITION -> MaterialTheme.matchInPositionColor
        }
    } else {
        MaterialTheme.noMatchColor
    }

    val shownChar = char.char
    val flipProgress = rotation.value
    val alphaProgress = alpha.value
    // 3D Flip effect: rotate along X-axis or Y-axis
    val cameraDistance = 8 * LocalDensity.current.density // Makes rotation look more realistic
    Box(
        modifier = rowModifier
            .clip(RoundedCornerShape(4.dp))
            .graphicsLayer {
                rotationX = flipProgress
                this.cameraDistance = cameraDistance
                this.alpha = alphaProgress
            }
            .background(backgroundColor)
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

@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Portrait Mode", showBackground = true, device = Devices.PIXEL_XL)
annotation class OrientationPreviews

@OrientationPreviews
@Composable
private fun MainScreenPreview() {
    WordleCloneTheme {
        MainScreen(uiState = previewGameUiState, onKeyPressed = {}, onResetClicked = {}, onAnimationEnded = {})
    }
}

private val dummyRows = listOf(
    GameRow(
        rowNumber = 0,
        state = RowState.ACTIVE,
        entries = listOf(
            Character("B", CharState.MATCH_IN_POSITION),
            Character("R", CharState.MATCH_IN_WORD),
            Character("E", CharState.MATCH_IN_WORD),
            Character("A", CharState.NO_MATCH),
            Character("D", CharState.MATCH_IN_POSITION),
        )
    ),
    GameRow(rowNumber = 1),
    GameRow(rowNumber = 2),
    GameRow(rowNumber = 3),
    GameRow(rowNumber = 4),
    GameRow(rowNumber = 5)
)

val previewGameUiState = GameUiState(
    usedCharacters = mapOf(
        "A" to CharState.NO_MATCH,
        "D" to CharState.MATCH_IN_WORD,
        "G" to CharState.MATCH_IN_POSITION,
    ),
    domainState = GameDomainState(
        gameState = GameState.Running,
        rows = dummyRows,
    )
)
