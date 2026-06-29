package de.engel.flashdrive.feature.import.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.engel.flashdrive.feature.import.ui.component.CsvPreviewTable
import de.engel.flashdrive.feature.import.ui.component.ImportProgress

/**
 * Screen for importing flashcards from a CSV file.
 *
 * Features:
 *  - File picker via SAF
 *  - Column mapping (Front / Back / Tags)
 *  - Preview table
 *  - Import button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    deckId: Long = -1L,
    snackbarHostState: SnackbarHostState,
    viewModel: CsvImportViewModel = hiltViewModel(),
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
            text = "CSV-Import",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Importiere Karten aus einer CSV-Datei. Unterstützt Semikolon-getrennte Felder mit Anführungszeichen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // File picker button.
        OutlinedButton(
            onClick = {
                filePickerLauncher.launch(arrayOf("text/*", "text/comma-separated-values", "*/*"))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("CSV-Datei auswählen")
        }

        // Header toggle.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(
                checked = uiState.hasHeader,
                onCheckedChange = { viewModel.toggleHeader(it) },
            )
            Text(
                text = "Erste Zeile ist Kopfzeile",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // Column mapping section.
        if (uiState.rawHeaders.isNotEmpty()) {
            Text(
                text = "Spalten zuordnen",
                style = MaterialTheme.typography.titleSmall,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ColumnMappingDropdown(
                    label = "Vorderseite",
                    headers = uiState.rawHeaders,
                    selectedIndex = uiState.frontColumn,
                    onSelected = { front ->
                        viewModel.updateColumnMapping(
                            frontColumn = front,
                            backColumn = uiState.backColumn,
                            tagsColumn = uiState.tagsColumn,
                        )
                    },
                )

                ColumnMappingDropdown(
                    label = "Rückseite",
                    headers = uiState.rawHeaders,
                    selectedIndex = uiState.backColumn,
                    onSelected = { back ->
                        viewModel.updateColumnMapping(
                            frontColumn = uiState.frontColumn,
                            backColumn = back,
                            tagsColumn = uiState.tagsColumn,
                        )
                    },
                )

                ColumnMappingDropdown(
                    label = "Tags (optional)",
                    headers = listOf("— Keine —") + uiState.rawHeaders,
                    selectedIndex = if (uiState.tagsColumn < 0) 0 else uiState.tagsColumn + 1,
                    onSelected = { tags ->
                        val actualTags = if (tags == 0) -1 else tags - 1
                        viewModel.updateColumnMapping(
                            frontColumn = uiState.frontColumn,
                            backColumn = uiState.backColumn,
                            tagsColumn = actualTags,
                        )
                    },
                )
            }
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
            enabled = !uiState.isImporting && uiState.parsedCards.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Importieren")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnMappingDropdown(
    label: String,
    headers: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val safeIndex = selectedIndex.coerceIn(0, headers.lastIndex)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = headers.getOrElse(safeIndex) { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            headers.forEachIndexed { index, header ->
                DropdownMenuItem(
                    text = { Text(header) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}
