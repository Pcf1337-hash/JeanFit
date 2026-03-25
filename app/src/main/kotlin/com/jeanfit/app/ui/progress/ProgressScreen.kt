package com.jeanfit.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.data.db.entities.WeightEntry
import com.jeanfit.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(
    onViewHistory: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.showAddDialog) {
        WeightEntryDialog(
            inputWeight = state.inputWeight,
            inputNote = state.inputNote,
            isSaving = state.isSaving,
            onWeightChange = viewModel::setInputWeight,
            onNoteChange = viewModel::setInputNote,
            onConfirm = viewModel::saveWeight,
            onDismiss = viewModel::hideAddDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fortschritt", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddDialog,
                containerColor = SunsetOrange
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Gewicht eintragen", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SunsetOrange)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                // Weight summary card
                WeightSummaryCard(
                    latest = state.latestWeight?.weightKg,
                    goal = state.goalWeightKg,
                    start = state.startWeightKg
                )
            }
            item {
                // Progress bar
                WeightProgressBar(
                    current = state.latestWeight?.weightKg ?: state.startWeightKg,
                    start = state.startWeightKg,
                    goal = state.goalWeightKg
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Verlauf", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onViewHistory) { Text("Alle anzeigen") }
                }
            }
            if (state.entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.MonitorWeight,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Noch kein Gewicht eingetragen",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = viewModel::showAddDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
                            ) { Text("Jetzt eintragen") }
                        }
                    }
                }
            } else {
                items(state.entries.take(10)) { entry ->
                    WeightEntryRow(entry = entry, onDelete = { viewModel.deleteEntry(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun WeightSummaryCard(latest: Float?, goal: Float, start: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeightStat("Aktuell", latest?.let { "%.1f kg".format(it) } ?: "—", SunsetOrange)
            WeightStat("Ziel", "%.1f kg".format(goal), FoodGreen)
            if (latest != null) {
                val diff = latest - goal
                WeightStat("Noch", "%.1f kg".format(diff), if (diff <= 0) FoodGreen else FoodOrange)
            }
        }
    }
}

@Composable
private fun WeightStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeightProgressBar(current: Float, start: Float, goal: Float) {
    val total = start - goal
    val done = (start - current).coerceIn(0f, total)
    val progress = if (total > 0) done / total else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gesamtfortschritt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, color = SunsetOrange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = SunsetOrange,
                trackColor = SunsetOrange.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Start: %.1f kg".format(start), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Ziel: %.1f kg".format(goal), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WeightEntryRow(entry: WeightEntry, onDelete: () -> Unit) {
    val dateStr = LocalDate.ofEpochDay(entry.dateEpochDay).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(dateStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            entry.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text("%.1f kg".format(entry.weightKg), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SunsetOrange)
                entry.trendWeightKg?.let {
                    Text("Trend: %.1f kg".format(it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}

@Composable
private fun WeightEntryDialog(
    inputWeight: String,
    inputNote: String,
    isSaving: Boolean,
    onWeightChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gewicht eintragen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = onWeightChange,
                    label = { Text("Gewicht (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = inputNote,
                    onValueChange = onNoteChange,
                    label = { Text("Notiz (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = inputWeight.toFloatOrNull() != null && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Speichern")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
fun WeightHistoryScreen(onBack: () -> Unit, viewModel: ProgressViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gewichtsverlauf", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            if (state.entries.isEmpty()) {
                item {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Noch keine Einträge", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(state.entries) { entry ->
                    WeightEntryRow(entry = entry, onDelete = { viewModel.deleteEntry(entry.id) })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview() {
    JeanFitTheme { ProgressScreen(onViewHistory = {}) }
}
