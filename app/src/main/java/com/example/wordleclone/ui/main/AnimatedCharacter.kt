import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordleclone.domain.model.CharState
import com.example.wordleclone.domain.model.Character
import com.example.wordleclone.ui.main.CHARACTER_ANIMATION_DELAY
import com.example.wordleclone.ui.main.CHARACTER_TWEEN_DURATION
import com.example.wordleclone.ui.theme.matchInPositionColor
import com.example.wordleclone.ui.theme.matchInWordColor
import com.example.wordleclone.ui.theme.noMatchColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                delay(animationDelay * CHARACTER_ANIMATION_DELAY)
                // Phase 1: 0° to 85° (hide old state)
                rotation.animateTo(
                    targetValue = 80f,
                    animationSpec = tween(durationMillis = CHARACTER_TWEEN_DURATION)
                )
                revealed = true
                // Phase 2: 85° back to 0° (show new state right-side up)
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = CHARACTER_TWEEN_DURATION)
                )
                onCharacterAnimationEnded()
            }
            launch {
                delay(animationDelay * CHARACTER_ANIMATION_DELAY)
                alpha.animateTo(
                    targetValue = 0.70f,
                    animationSpec = tween(durationMillis = CHARACTER_TWEEN_DURATION)
                )
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = CHARACTER_TWEEN_DURATION)
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
