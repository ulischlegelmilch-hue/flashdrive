package de.engel.flashdrive.feature.import.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.engel.flashdrive.feature.import.ui.component.CsvPreviewTable
import de.engel.flashdrive.feature.import.ui.component.ImportProgress

/**
 * Screen for importing flashcards from an Anki .apkg file.
 *
 * Features:
 *  - File picker for .apkg files via SAF
 *  - Preview of extracted cards
 *  - Import button
 */
@Composable
fun AnkiImportScreen(
    deckId: Long = -1L,
    snackbarHostState: SnackbarHostState,
    viewModel: AnkiImportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Pre-select deck if provided
    LaunchedEffect(deckId) {
        if (deckId > 0) viewModel.setSelectedDeckId(deckId)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let { viewModel.loadFile(context, it) }
    }

    LaunchedEffect(uiState.importResult) {
        uiState.importResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Anki-Import",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Importiere Karten aus einer Anki-.apkg-Datei. Die Datei wird entpackt und die Notizen extrahiert.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // File picker button.
        OutlinedButton(
            onClick = {
                filePickerLauncher.launch(arrayOf(
                    "application/octet-stream",
                    "application/vnd.anki",
                    "*/*",
                ))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(".apkg-Datei auswählen")
        }

        // File info.
        if (uiState.fileName.isNotBlank()) {
            Text(
                text = "Datei: ${uiState.fileName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Loading indicator.
        if (uiState.isLoading) {
            Text(
                text = "Lade Datei…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Preview table.
        if (uiState.parsedCards.isNotEmpty()) {
            Text(
                text = "Vorschau (${uiState.parsedCards.size} Karten)",
                style = MaterialTheme.typography.titleSmall,
            )
            CsvPreviewTable(cards = uiState.parsedCards)
        }

        // Import progress / result.
        ImportProgress(
            isImporting = uiState.isImporting,
            importResult = uiState.importResult,
            errorMessage = uiState.errorMessage,
        )

        // Import button.
        Button(
            onClick = { viewModel.executeImport() },
            enabled = !uiState.isImporting && !uiState.isLoading && uiState.parsedCards.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Importieren")
        }
    }
}
