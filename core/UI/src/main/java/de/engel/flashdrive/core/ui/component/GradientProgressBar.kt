package de.engel.flashdrive.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.engel.flashdrive.core.ui.theme.CyanAccent
import de.engel.flashdrive.core.ui.theme.PurpleAccent
import de.engel.flashdrive.core.ui.theme.SurfaceDark

@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = SurfaceDark
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(PurpleAccent, CyanAccent)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(gradient)
        )
    }
}
