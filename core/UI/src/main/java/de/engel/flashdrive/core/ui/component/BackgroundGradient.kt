package de.engel.flashdrive.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import de.engel.flashdrive.core.ui.theme.DeepBlue
import de.engel.flashdrive.core.ui.theme.PurpleAccent
import de.engel.flashdrive.core.ui.theme.SurfaceDark

@Composable
fun BackgroundGradient(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SurfaceDark,
                        DeepBlue,
                        Color(0xFF0D0D1A)
                    ),
                    radius = 1200f
                )
            )
    ) {
        content()
    }
}
