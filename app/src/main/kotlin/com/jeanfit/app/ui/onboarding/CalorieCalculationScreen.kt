package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.SunsetOrange

@Composable
fun CalorieCalculationScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.calculateCalories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OnboardingProgress(currentStep = 7, totalSteps = 8)
            Spacer(Modifier.height(32.dp))
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = SunsetOrange,
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Dein tägliches Kalorienziel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "${state.calculatedCalories}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = SunsetOrange
            )
            Text(
                "kcal pro Tag",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SunsetOrange.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("Methode", "Harris-Benedict-Formel")
                    InfoRow("Aktivitätslevel", state.activityLevel.replace("_", " ").capitalize())
                    InfoRow("Ziel", "~0,5 kg Gewichtsverlust/Woche")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Basierend auf deinen Angaben haben wir deinen individuellen Energiebedarf berechnet.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { viewModel.saveProfile(onNext) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Profil erstellen", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun String.capitalize(): String = replaceFirstChar { it.uppercaseChar() }

@Preview(showBackground = true)
@Composable
private fun CalorieCalcPreview() {
    JeanFitTheme { CalorieCalculationScreen(onNext = {}, onBack = {}) }
}
