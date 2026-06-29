package de.engel.flashdrive.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.engel.flashdrive.core.ui.theme.CardDark
import de.engel.flashdrive.core.ui.theme.PurpleAccent
import de.engel.flashdrive.core.ui.theme.TextSecondary

@Composable
fun FlashCardComposable(
    front: String,
    back: String,
    isFlipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "cardFlip"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isFlipped) 1.02f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .graphicsLayer {
                rotationY = rotation
                scaleX = animatedScale
                scaleY = animatedScale
                shadowElevation = 16f
                shape = RoundedCornerShape(24.dp)
                clip = true
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (rotation > 90f) {
                        rotationY = 180f
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val displayText = if (rotation <= 90f) front else back
            val textColor = if (rotation <= 90f)
                MaterialTheme.colorScheme.onSurface
            else
                PurpleAccent

            Text(
                text = displayText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
