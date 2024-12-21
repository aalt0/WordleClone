package com.example.wordleclone.ui.main

import AnimatedCharacter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.wordleclone.domain.model.GameRow

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
            val shakeDistance = ROW_SHAKE_DISTANCE
            repeat(3) {
                offsetX.animateTo(shakeDistance, tween(ROW_SHAKE_TWEEN_DURATION))
                offsetX.animateTo(-shakeDistance, tween(ROW_SHAKE_TWEEN_DURATION))
            }
            offsetX.animateTo(0f, tween(ROW_SHAKE_TWEEN_DURATION))
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
