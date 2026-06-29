package de.engel.flashdrive.feature.study.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.engel.flashdrive.core.model.ReviewQuality
import de.engel.flashdrive.core.ui.component.FlashCardComposable
import de.engel.flashdrive.feature.study.TtsManager
import de.engel.flashdrive.feature.study.ui.component.FlipCardAnimation
import de.engel.flashdrive.feature.study.ui.component.GradeButton
import de.engel.flashdrive.feature.study.ui.component.TtsButton

// Brand colors for grade buttons — vibrant and accessible.
private val GreenKnow = Color(0xFF22C55E)
private val OrangeAgain = Color(0xFFF59E0B)
private val RedDontKnow = Color(0xFFEF4444)

/**
 * Main Study screen — professional dark theme with improved touch targets.
 *
 * Features:
 * - Large readable card with flip animation
 * - Fixed-width grade buttons that never overflow
 * - TTS integration for hands-free learning
 * - Progress indicator showing session completion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: Long,
    navigator: (de.engel.flashdrive.app.navigation.Screen) -> Unit,
    viewModel: StudyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // TTS manager lifecycle tied to this composable.
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // When the session is finished, show the summary screen.
    if (uiState.isFinished) {
        SessionCompleteScreen(
            deckName = uiState.deckName,
            cardsStudied = uiState.cardsStudied,
            correctCount = uiState.correctCount,
            incorrectCount = uiState.incorrectCount,
            accuracy = uiState.accuracy,
            onDone = { navigator(de.engel.flashdrive.app.navigation.Screen.DeckList) },
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.deckName.ifEmpty { "Lernen" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${uiState.cardsRemaining} verbleibend",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator(de.engel.flashdrive.app.navigation.Screen.DeckList) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            // ── Progress bar ─────────────────────────────────────────────
            LinearProgressIndicator(
                progress = { uiState.sessionProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Flash card with flip animation ───────────────────────────
            val card = uiState.currentCard
            if (card != null) {
                FlipCardAnimation(
                    isFlipped = uiState.isFlipped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) { rotationY ->
                    val showBack = rotationY > 90f
                    FlashCardComposable(
                        front = card.front,
                        back = card.back,
                        isFlipped = showBack,
                        onClick = { viewModel.flipCard() },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── TTS button ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TtsButton(
                        onClick = {
                            val text = if (uiState.isFlipped) card.back else card.front
                            ttsManager.speak(text)
                        },
                        isActive = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Grade buttons (only visible after flip) ──────────────────
            // These buttons use fixed width and constrained text to prevent overflow
            if (uiState.isFlipped) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Each button gets equal width via weight
                    GradeButton(
                        label = "Wusst ich",
                        icon = Icons.Default.CheckCircle,
                        color = GreenKnow,
                        onClick = { viewModel.submitGrade(ReviewQuality.CORRECT_HESITATION) },
                        modifier = Modifier.weight(1f),
                    )
                    GradeButton(
                        label = "Wiederholen",
                        icon = Icons.Default.Refresh,
                        color = OrangeAgain,
                        onClick = { viewModel.submitGrade(ReviewQuality.CORRECT_DIFFICULT) },
                        modifier = Modifier.weight(1f),
                    )
                    GradeButton(
                        label = "Nicht gewusst",
                        icon = Icons.Default.Close,
                        color = RedDontKnow,
                        onClick = { viewModel.submitGrade(ReviewQuality.INCORRECT_REMEMBERED) },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                // Hint to flip the card.
                Text(
                    text = "Tippe auf die Karte zum Aufdecken",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 32.dp),
                )
            }
        }
    }
}
