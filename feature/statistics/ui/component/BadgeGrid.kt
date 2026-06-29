package de.engel.flashdrive.feature.statistics.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.engel.flashdrive.core.model.Badge
import de.engel.flashdrive.core.model.BadgeTier
import de.engel.flashdrive.feature.statistics.BadgeDefinitions

/**
 * Grid of badge cards showing achievement progress.
 */
@Composable
fun BadgeGrid(
    badges: List<Badge>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Abzeichen",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(badges, key = { "${it.category}_${it.tier}" }) { badge ->
                BadgeCard(badge = badge)
            }
        }
    }
}

/**
 * Individual badge card with icon, name, progress bar, and locked/unlocked state.
 */
@Composable
fun BadgeCard(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    val definition = BadgeDefinitions.forBadge(badge)
    val tierColor = getTierColor(badge.tier)
    val progress = if (badge.target > 0) {
        (badge.currentValue.toFloat() / badge.target).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (badge.isUnlocked) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            if (badge.isUnlocked) {
                Icon(
                    imageVector = getBadgeIcon(definition?.iconName),
                    contentDescription = badge.name,
                    modifier = Modifier.size(32.dp),
                    tint = tierColor
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Gesperrt",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (badge.isUnlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = tierColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Progress text
            Text(
                text = "${badge.currentValue}/${badge.target}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Returns the color associated with a badge tier.
 */
@Composable
private fun getTierColor(tier: BadgeTier): Color {
    return when (tier) {
        BadgeTier.BRONZE -> Color(0xFFCD7F32)
        BadgeTier.SILVER -> Color(0xFFC0C0C0)
        BadgeTier.GOLD -> Color(0xFFFFD700)
        BadgeTier.PLATINUM -> Color(0xFFE5E4E2)
        BadgeTier.DIAMOND -> Color(0xFFB9F2FF)
    }
}

/**
 * Maps a badge icon name to a Material Icon ImageVector.
 * Falls back to a default icon if the name is unknown.
 */
@Composable
private fun getBadgeIcon(iconName: String?): ImageVector {
    return when (iconName) {
        "local_fire_department" -> Icons.Default.Whatshot
        "whatshot" -> Icons.Default.Whatshot
        "emoji_events" -> Icons.Default.EmojiEvents
        "workspace_premium" -> Icons.Default.EmojiEvents
        "menu_book" -> Icons.Default.MenuBook
        "school" -> Icons.Default.School
        "military_tech" -> Icons.Default.EmojiEvents
        "play_circle" -> Icons.Default.PlayCircle
        "auto_mode" -> Icons.Default.Timer
        "verified" -> Icons.Default.EmojiEvents
        "gps_fixed" -> Icons.Default.MyLocation
        "my_location" -> Icons.Default.MyLocation
        "stars" -> Icons.Default.Stars
        "favorite" -> Icons.Default.Favorite
        "volunteer_activism" -> Icons.Default.Favorite
        "diamond" -> Icons.Default.Stars
        else -> Icons.Default.Stars
    }
}
