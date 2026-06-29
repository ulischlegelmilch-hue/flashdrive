package de.engel.flashdrive.feature.statistics.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.engel.flashdrive.feature.statistics.ui.HeatmapDay

/**
 * GitHub-style heatmap calendar showing study activity over the last 365 days.
 *
 * Layout: 52 weeks (columns) x 7 days (rows).
 * Color coding:
 * - 0 cards studied → gray
 * - 1-3 cards → light green
 * - 4+ cards → dark green
 */
@Composable
fun HeatmapCalendar(
    data: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    val weeksCount = 52
    val daysPerWeek = 7

    val cellColorEmpty = Color(0xFF1E293B)  // Matches surfaceVariant
    val cellColorLow = Color(0xFF22C55E).copy(alpha = 0.4f)
    val cellColorHigh = Color(0xFF22C55E)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Letzte 365 Tage",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Day labels (Mon, Wed, Fri)
            Column(
                modifier = Modifier.height((weeksCount * 14).dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("M", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("", style = MaterialTheme.typography.labelSmall)
                Text("M", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("", style = MaterialTheme.typography.labelSmall)
                Text("F", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Heatmap grid
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(weeksCount.toFloat() / daysPerWeek.toFloat())
            ) {
                val cellSize = size.width / weeksCount
                val cellPadding = 2.dp.toPx()
                val actualCellSize = cellSize - cellPadding * 2

                for (week in 0 until weeksCount) {
                    for (day in 0 until daysPerWeek) {
                        val index = (week * daysPerWeek) + day
                        val dayData = data.getOrNull(index)
                        val cardsStudied = dayData?.cardsStudied ?: 0

                        val color = when {
                            cardsStudied == 0 -> cellColorEmpty
                            cardsStudied <= 3 -> cellColorLow
                            else -> cellColorHigh
                        }

                        val x = week * cellSize + cellPadding
                        val y = day * cellSize + cellPadding

                        drawRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(actualCellSize, actualCellSize)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wenig",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))

            LegendCell(color = cellColorEmpty)
            LegendCell(color = cellColorLow)
            LegendCell(color = cellColorHigh)

            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Viel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendCell(color: Color) {
    Canvas(modifier = Modifier
        .size(12.dp)
        .padding(1.dp)
    ) {
        drawRect(color = color, size = size)
    }
}
