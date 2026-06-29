package de.engel.flashdrive.feature.study.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch

/**
 * Wrapper that animates a card flip around the Y-axis using [Animatable].
 *
 * The content is rendered inside a [Box] whose `rotationY` is driven by an
 * [Animatable]. When [isFlipped] changes the rotation animates between 0 and
 * 180 degrees. A high [cameraDistance] gives a natural 3D perspective.
 *
 * The content lambda receives the *current* rotation value so it can swap
 * between front / back visuals at the 90-degree midpoint.
 *
 * @param isFlipped Target flip state (true = show back).
 * @param modifier Optional [Modifier].
 * @param durationMillis Flip animation duration in milliseconds.
 * @param content Composable content that receives the live rotation value.
 */
@Composable
fun FlipCardAnimation(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    durationMillis: Int = 500,
    content: @Composable (rotationY: Float) -> Unit,
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12f * density
            },
    ) {
        content(rotation.value)
    }

    if (rotation.value != if (isFlipped) 180f else 0f) {
        scope.launch {
            rotation.animateTo(
                targetValue = if (isFlipped) 180f else 0f,
                animationSpec = tween(durationMillis = durationMillis),
            )
        }
    }
}
