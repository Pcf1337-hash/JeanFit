package com.jeanfit.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.jeanfit.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SunsetOrange)
        }
        return
    }

    // Success-Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) snackbarHostState.showSnackbar("Gespeichert ✓")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text("Speichern", color = SunsetOrange, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── PROFIL ─────────────────────────────────────────────
            item { SettingsSectionHeader("👤 Mein Profil") }

            item {
                SettingsCard {
                    SettingsTextField(
                        label = "Name",
                        value = state.name,
                        onValueChange = viewModel::setName,
                        leadingIcon = Icons.Filled.Person
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                    SettingsTextField(
                        label = "Körpergröße (cm)",
                        value = state.heightCm,
                        onValueChange = viewModel::setHeight,
                        keyboardType = KeyboardType.Number,
                        leadingIcon = Icons.Filled.Height
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                    SettingsTextField(
                        label = "Zielgewicht (kg)",
                        value = state.goalWeightKg,
                        onValueChange = viewModel::setGoalWeight,
                        keyboardType = KeyboardType.Decimal,
                        leadingIcon = Icons.Filled.MonitorWeight
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                    // Aktuelles Gewicht (read-only info)
                    state.profile?.let { profile ->
                        SettingsInfoRow(
                            label = "Startgewicht",
                            value = "${profile.startWeightKg.let { if (it % 1 == 0f) it.toInt().toString() else it.toString() }} kg",
                            icon = Icons.Filled.Scale
                        )
                    }
                }
            }

            // ── AKTIVITÄT & KALORIEN ──────────────────────────────
            item { SettingsSectionHeader("🔥 Aktivität & Kalorien") }

            item {
                SettingsCard {
                    // Aktivitätslevel Auswahl
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.DirectionsRun,
                                contentDescription = null,
                                tint = SunsetOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Aktivitätslevel",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        val activityOptions = listOf(
                            "sedentary" to "Sitzend (kaum Bewegung)",
                            "lightly_active" to "Leicht aktiv (1–2× Sport/Wo.)",
                            "active" to "Aktiv (3–5× Sport/Wo.)",
                            "very_active" to "Sehr aktiv (täglich Sport)"
                        )
                        activityOptions.forEach { (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = state.activityLevel == key,
                                    onClick = { viewModel.setActivityLevel(key) },
                                    colors = RadioButtonDefaults.colors(selectedColor = SunsetOrange)
                                )
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    // Abnahmeziel pro Woche
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.TrendingDown,
                                contentDescription = null,
                                tint = SunsetOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Abnahme-Ziel: ${String.format("%.1f", state.goalKgPerWeek)} kg/Woche",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Slider(
                            value = state.goalKgPerWeek,
                            onValueChange = viewModel::setGoalKgPerWeek,
                            valueRange = 0.25f..1.0f,
                            steps = 2,
                            colors = SliderDefaults.colors(thumbColor = SunsetOrange, activeTrackColor = SunsetOrange),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0,25 kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1,0 kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    // Kalorienziel
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = state.dailyCalorieGoal,
                            onValueChange = viewModel::setCalorieGoal,
                            label = { Text("Tagesziel (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Filled.LocalFireDepartment, null, tint = SunsetOrange) }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = viewModel::recalculateCalories,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = SunsetOrange.copy(alpha = 0.1f))
                        ) {
                            Text("Neu berechnen", color = SunsetOrange, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── PERSÖNLICHE ZIELE ────────────────────────────────
            item { SettingsSectionHeader("🎯 Persönliche Ziele") }

            item {
                SettingsCard {
                    SettingsTextField(
                        label = "Wasserziel (Liter/Tag)",
                        value = state.waterGoalLiter,
                        onValueChange = viewModel::setWaterGoal,
                        keyboardType = KeyboardType.Decimal,
                        leadingIcon = Icons.Filled.WaterDrop
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                    SettingsTextField(
                        label = "Schrittziel (Schritte/Tag)",
                        value = state.stepsGoal,
                        onValueChange = viewModel::setStepsGoal,
                        keyboardType = KeyboardType.Number,
                        leadingIcon = Icons.Filled.DirectionsWalk
                    )
                }
            }

            // ── PROFIL-INFO ───────────────────────────────────────
            item { SettingsSectionHeader("ℹ️ Profil-Info") }

            item {
                SettingsCard {
                    state.profile?.let { profile ->
                        val gender = when (profile.gender) {
                            "male" -> "Männlich"
                            "female" -> "Weiblich"
                            "non_binary" -> "Nicht-binär"
                            else -> "Keine Angabe"
                        }
                        val birth = java.time.LocalDate.ofEpochDay(profile.birthDateEpochDay)
                        val age = java.time.Period.between(birth, java.time.LocalDate.now()).years
                        SettingsInfoRow(label = "Geschlecht", value = gender, icon = Icons.Filled.Wc)
                        HorizontalDivider(thickness = 0.5.dp)
                        SettingsInfoRow(label = "Alter", value = "$age Jahre", icon = Icons.Filled.Cake)
                        HorizontalDivider(thickness = 0.5.dp)
                        SettingsInfoRow(
                            label = "NoomCoins",
                            value = "${profile.noomCoins} 🪙",
                            icon = Icons.Filled.MonetizationOn
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                        val start = java.time.LocalDate.ofEpochDay(profile.programStartDate)
                        val days = java.time.Period.between(start, java.time.LocalDate.now()).days +
                                java.time.Period.between(start, java.time.LocalDate.now()).months * 30
                        SettingsInfoRow(
                            label = "Programmtage",
                            value = "$days Tage",
                            icon = Icons.Filled.CalendarToday
                        )
                    }
                }
            }

            // ── APP-VERSION ───────────────────────────────────────
            item { SettingsSectionHeader("📱 App") }

            item {
                SettingsCard {
                    SettingsInfoRow(label = "Version", value = "1.2.0", icon = Icons.Filled.Info)
                    HorizontalDivider(thickness = 0.5.dp)
                    SettingsInfoRow(label = "App-Name", value = "JeanFit", icon = Icons.Filled.FitnessCenter)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Speichern Button
            item {
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Änderungen speichern", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp, end = 16.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(20.dp)) }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SunsetOrange,
            focusedLabelColor = SunsetOrange
        )
    )
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    JeanFitTheme { SettingsScreen(onBack = {}) }
}
