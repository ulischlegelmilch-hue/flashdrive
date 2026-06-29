package de.engel.flashdrive.feature.import.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.engel.flashdrive.feature.import.ui.ParsedCard

/**
 * Displays a preview table of parsed CSV cards.
 *
 * Shows up to [maxRows] rows. If there are more, a summary row is appended.
 */
@Composable
fun CsvPreviewTable(
    cards: List<ParsedCard>,
    maxRows: Int = 10,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val displayCards = cards.take(maxRows)
    val hasMore = cards.size > maxRows

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.small,
            )
            .horizontalScroll(scrollState)
            .padding(8.dp),
    ) {
        // Header row.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TableHeaderCell(text = "Vorderseite", weight = 1f)
            TableHeaderCell(text = "Rückseite", weight = 1f)
            TableHeaderCell(text = "Tags", weight = 1f)
        }

        // Data rows.
        displayCards.forEachIndexed { index, card ->
            if (index > 0) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TableDataCell(text = card.front, weight = 1f)
                TableDataCell(text = card.back, weight = 1f)
                TableDataCell(text = card.tags.ifBlank { "—" }, weight = 1f)
            }
        }

        if (hasMore) {
            Text(
                text = "… und ${cards.size - maxRows} weitere Karten",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
    )
}

@Composable
private fun TableDataCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
    )
}
